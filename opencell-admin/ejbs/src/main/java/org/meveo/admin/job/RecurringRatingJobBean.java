package org.meveo.admin.job;

import org.meveo.admin.async.SubListCreator;
import org.meveo.admin.job.logging.JobLoggingInterceptor;
import org.meveo.event.qualifier.Rejected;
import org.meveo.interceptor.PerformanceInterceptor;
import org.meveo.model.billing.InstanceStatusEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.shared.DateUtils;
import org.meveo.service.billing.impl.RecurringChargeInstanceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.slf4j.Logger;

import org.meveo.admin.async.RecurringChargeAsync;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Stateless
public class RecurringRatingJobBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2226065462536318643L;

    @Inject
    private RecurringChargeAsync recurringChargeAsync;

    @Inject
    private RecurringChargeInstanceService recurringChargeInstanceService;

    @Inject
    protected Logger log;

    @Inject
    @Rejected
    Event<Serializable> rejectededChargeProducer;

    @Inject
    protected CustomFieldInstanceService customFieldInstanceService;

    @SuppressWarnings("unchecked")
    @Interceptors({ JobLoggingInterceptor.class, PerformanceInterceptor.class })
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void execute(JobExecutionResultImpl result, JobInstance jobInstance) {
        log.debug("start in running with parameter={}", jobInstance.getParametres());
        try {
            Long nbRuns = new Long(1);
            Long waitingMillis = new Long(0);
            Date rateUntilDate = null;
            boolean isToTruncatedToDate = true;
            try {
                nbRuns = (Long) customFieldInstanceService.getCFValue(jobInstance, "nbRuns");
                waitingMillis = (Long) customFieldInstanceService.getCFValue(jobInstance, "waitingMillis");
                if (nbRuns == -1) {
                    nbRuns = (long) Runtime.getRuntime().availableProcessors();
                }
                rateUntilDate = (Date) customFieldInstanceService.getCFValue(jobInstance,"rateUntilDate");
            } catch (Exception e) {
                nbRuns = new Long(1);
                waitingMillis = new Long(0);
                log.warn("Cant get customFields for " + jobInstance.getJobTemplate(), e.getMessage());
            }
            if (rateUntilDate == null) {
                rateUntilDate = DateUtils.addDaysToDate(new Date(), 1);
            } else {
                isToTruncatedToDate = false;
            }
            List<Long> ids = recurringChargeInstanceService.findIdsByStatus(InstanceStatusEnum.ACTIVE, rateUntilDate, isToTruncatedToDate);
            int inputSize = ids.size();
            result.setNbItemsToProcess(inputSize);
            log.info("in job - charges to rate={}", inputSize);

            List<Future<String>> futures = new ArrayList<Future<String>>();
            SubListCreator subListCreator = new SubListCreator(ids, nbRuns.intValue());
            while (subListCreator.isHasNext()) {
                futures.add(recurringChargeAsync.launchAndForget((List<Long>) subListCreator.getNextWorkSet(), result, rateUntilDate));

                if (subListCreator.isHasNext()) {
                    try {
                        Thread.sleep(waitingMillis.longValue());
                    } catch (InterruptedException e) {
                        log.error("", e);
                    }
                }
            }

            // Wait for all async methods to finish
            for (Future<String> future : futures) {
                try {
                    future.get();

                } catch (InterruptedException e) {
                    // It was cancelled from outside - no interest

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    result.registerError(cause.getMessage());
                    log.error("Failed to execute async method", cause);
                }
            }
        } catch (Exception e) {
            log.error("Failed to run recurring rating job", e);
            result.registerError(e.getMessage());
        }
        log.debug("end running RecurringRatingJobBean!");
    }
}

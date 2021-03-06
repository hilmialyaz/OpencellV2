/**
 * 
 */
package org.meveo.admin.async;

import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.job.UnitRatedTransactionsJobBean;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.service.job.JobExecutionService;

/**
 * @author anasseh
 *
 */

@Stateless
public class RatedTransactionAsync {
	
	@Inject
	private UnitRatedTransactionsJobBean unitRatedTransactionsJobBean;
	
    @Inject
    private JobExecutionService jobExecutionService;
	
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public Future<String> launchAndForget(List<Long> ids, JobExecutionResultImpl result) {
		for (Long walletOperationId : ids) {
            if (!jobExecutionService.isJobRunningOnThis(result.getJobInstance().getId())) {
                break;
            }
			unitRatedTransactionsJobBean.execute(result, walletOperationId);
		}
		return new AsyncResult<String>("OK");
	}
}

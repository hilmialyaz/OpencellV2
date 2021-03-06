package org.meveo.admin.job;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.service.job.Job;

/**
 * The Class PaymentCardJob create payment or refund for all opened account operations.
 */
@Stateless
public class PaymentCardJob extends Job {

    /** The payment card job bean. */
    @Inject
    private PaymentCardJobBean paymentCardJobBean;

    @Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
    protected void execute(JobExecutionResultImpl result, JobInstance jobInstance) throws BusinessException {
        paymentCardJobBean.execute(result, jobInstance);
    }

    @Override
    public JobCategoryEnum getJobCategory() {
        return JobCategoryEnum.ACCOUNT_RECEIVABLES;
    }


    @Override
    public Map<String, CustomFieldTemplate> getCustomFields() {
        Map<String, CustomFieldTemplate> result = new HashMap<String, CustomFieldTemplate>();

        CustomFieldTemplate nbRuns = new CustomFieldTemplate();
        nbRuns.setCode("nbRuns");
        nbRuns.setAppliesTo("JOB_PaymentCardJob");
        nbRuns.setActive(true);
        nbRuns.setDescription(resourceMessages.getString("jobExecution.nbRuns"));
        nbRuns.setFieldType(CustomFieldTypeEnum.LONG);
        nbRuns.setValueRequired(false);
        nbRuns.setDefaultValue("1");
        result.put("nbRuns", nbRuns);

        CustomFieldTemplate waitingMillis = new CustomFieldTemplate();
        waitingMillis.setCode("waitingMillis");
        waitingMillis.setAppliesTo("JOB_PaymentCardJob");
        waitingMillis.setActive(true);
        waitingMillis.setDescription(resourceMessages.getString("jobExecution.waitingMillis"));
        waitingMillis.setFieldType(CustomFieldTypeEnum.LONG);
        waitingMillis.setValueRequired(false);
        waitingMillis.setDefaultValue("0");
        result.put("waitingMillis", waitingMillis);

        Map<String, String> lisValuesYesNo = new HashMap<String, String>();
        lisValuesYesNo.put("YES", "YES");
        lisValuesYesNo.put("NO", "NO");

        Map<String, String> lisValuesCreditDebit = new HashMap<String, String>();
        lisValuesCreditDebit.put("Credit", "Payment");
        lisValuesCreditDebit.put("Debit", "Refund");

        CustomFieldTemplate createAO = new CustomFieldTemplate();
        createAO.setCode("PaymentCardJob_createAO");
        createAO.setAppliesTo("JOB_PaymentCardJob");
        createAO.setActive(true);
        createAO.setDefaultValue("YES");
        createAO.setDescription("Create AO");
        createAO.setFieldType(CustomFieldTypeEnum.LIST);
        createAO.setValueRequired(false);
        createAO.setListValues(lisValuesYesNo);
        result.put("PaymentCardJob_createAO", createAO);

        CustomFieldTemplate matchingAO = new CustomFieldTemplate();
        matchingAO.setCode("PaymentCardJob_matchingAO");
        matchingAO.setAppliesTo("JOB_PaymentCardJob");
        matchingAO.setActive(true);
        matchingAO.setDefaultValue("YES");
        matchingAO.setDescription("Matching AO");
        matchingAO.setFieldType(CustomFieldTypeEnum.LIST);
        matchingAO.setValueRequired(false);
        matchingAO.setListValues(lisValuesYesNo);
        result.put("PaymentCardJob_matchingAO", matchingAO);

        CustomFieldTemplate creditOrDebit = new CustomFieldTemplate();
        creditOrDebit.setCode("PaymentCardJob_creditOrDebit");
        creditOrDebit.setAppliesTo("JOB_PaymentCardJob");
        creditOrDebit.setActive(true);
        creditOrDebit.setDefaultValue("Credit");
        creditOrDebit.setDescription(resourceMessages.getString("jobExecution.paymentOrRefund"));
        creditOrDebit.setFieldType(CustomFieldTypeEnum.LIST);
        creditOrDebit.setValueRequired(true);
        creditOrDebit.setListValues(lisValuesCreditDebit);
        result.put("PaymentCardJob_creditOrDebit", creditOrDebit);

        return result;
    }

}
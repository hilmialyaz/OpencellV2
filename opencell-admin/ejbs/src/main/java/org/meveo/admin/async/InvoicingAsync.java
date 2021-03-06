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

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.billing.BillingRun;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.service.billing.impl.BillingAccountService;
import org.meveo.service.billing.impl.InvoiceService;
import org.meveo.service.billing.impl.InvoicesToNumberInfo;
import org.meveo.service.job.JobExecutionService;
import org.slf4j.Logger;

/**
 * The Class InvoicingAsync.
 *
 * @author anasseh
 */

@Stateless
public class InvoicingAsync {

    /** The billing account service. */
    @Inject
    private BillingAccountService billingAccountService;

    /** The invoice service. */
    @Inject
    private InvoiceService invoiceService;

    /** The log. */
    @Inject
    protected Logger log;

    /** The JobExecution service. */
    @Inject
    private JobExecutionService jobExecutionService;

    /**
     * Update billing account total amounts async.
     *
     * @param billingAccountIds the billing account ids
     * @param billingRun the billing run
     * @param jobInstanceId the job instance id
     * @return the future
     * @throws BusinessException the business exception
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Future<Integer> updateBillingAccountTotalAmountsAsync(List<Long> billingAccountIds, BillingRun billingRun, Long jobInstanceId) throws BusinessException {
        int count = 0;
        for (Long billingAccountId : billingAccountIds) {
            if (jobInstanceId != null && !jobExecutionService.isJobRunningOnThis(jobInstanceId)) {
                break;
            }
            if (billingAccountService.updateBillingAccountTotalAmounts(billingAccountId, billingRun)) {
                count++;
            }
        }
        log.info("WorkSet billableBA:" + count);
        return new AsyncResult<Integer>(new Integer(count));
    }

    /**
     * Creates the agregates and invoice async.
     *
     * @param billingAccountIds the billing account ids
     * @param billingRun the billing run
     * @param jobInstanceId the job instance id
     * @return the future
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Future<String> createAgregatesAndInvoiceAsync(List<Long> billingAccountIds, BillingRun billingRun, Long jobInstanceId) {

        for (Long billingAccountId : billingAccountIds) {
            if (jobInstanceId != null && !jobExecutionService.isJobRunningOnThis(jobInstanceId)) {
                break;
            }
            try {
                invoiceService.createAgregatesAndInvoice(billingAccountId, billingRun, null, null, null, null, null);
            } catch (Exception e) {
                log.error("Error for BA=" + billingAccountId + " : " + e);
            }
        }
        return new AsyncResult<String>("OK");
    }

    /**
     * Assign invoice number and increment BA invoice dates async.
     *
     * @param invoiceIds the invoice ids
     * @param invoicesToNumberInfo the invoices to number info
     * @param jobInstanceId the job instance id
     * @return the future
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Future<String> assignInvoiceNumberAndIncrementBAInvoiceDatesAsync(List<Long> invoiceIds, InvoicesToNumberInfo invoicesToNumberInfo, Long jobInstanceId) {

        for (Long invoiceId : invoiceIds) {
            if (jobInstanceId != null && !jobExecutionService.isJobRunningOnThis(jobInstanceId)) {
                break;
            }
            try {
                invoiceService.assignInvoiceNumberAndIncrementBAInvoiceDate(invoiceId, invoicesToNumberInfo);
            } catch (Exception e) {
                log.error("Failed to increment invoice date for invoice {}", invoiceId, e);
            }
        }
        return new AsyncResult<String>("OK");
    }

    /**
     * Generate pdf async.
     *
     * @param invoiceIds the invoice ids
     * @param result the result
     * @return the future
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Future<String> generatePdfAsync(List<Long> invoiceIds, JobExecutionResultImpl result) {
        for (Long invoiceId : invoiceIds) {
            if (!jobExecutionService.isJobRunningOnThis(result.getJobInstance().getId())) {
                break;
            }
            try {
                invoiceService.produceInvoicePdfInNewTransaction(invoiceId);
                result.registerSucces();
            } catch (Exception e) {
                result.registerError(invoiceId, e.getMessage());
                log.error("Failed to create PDF invoice for invoice {}", invoiceId, e);
            }
        }

        return new AsyncResult<String>("OK");
    }

    /**
     * Generate xml async.
     *
     * @param invoiceIds the invoice ids
     * @param result the result
     * @return the future
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Future<Boolean> generateXmlAsync(List<Long> invoiceIds, JobExecutionResultImpl result) {

        boolean allOk = true;

        for (Long invoiceId : invoiceIds) {
            if (!jobExecutionService.isJobRunningOnThis(result.getJobInstance().getId())) {
                break;
            }
            long startDate = System.currentTimeMillis();
            try {
                invoiceService.produceInvoiceXmlInNewTransaction(invoiceId);
                result.registerSucces();
            } catch (Exception e) {
                result.registerError(invoiceId, e.getMessage());
                allOk = false;
                log.error("Failed to create XML invoice for invoice {}", invoiceId, e);
            }
            log.info("Invoice creation delay :" + (System.currentTimeMillis() - startDate));
        }

        return new AsyncResult<Boolean>(allOk);
    }
}
package org.meveo.api.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.NoAllOperationUnmatchedException;
import org.meveo.admin.exception.UnbalanceAmountException;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.payment.PayByCardDto;
import org.meveo.api.dto.payment.PayByCardResponseDto;
import org.meveo.api.dto.payment.PaymentDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.payments.AccountOperation;
import org.meveo.model.payments.AutomatedPayment;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.MatchingTypeEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.OtherCreditAndCharge;
import org.meveo.model.payments.Payment;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.model.payments.RecordedInvoice;
import org.meveo.service.payments.impl.CustomerAccountService;
import org.meveo.service.payments.impl.MatchingCodeService;
import org.meveo.service.payments.impl.OCCTemplateService;
import org.meveo.service.payments.impl.PaymentService;
import org.meveo.service.payments.impl.RecordedInvoiceService;

@Stateless
public class PaymentApi extends BaseApi {

    @Inject
    private PaymentService paymentService;

    @Inject
    private RecordedInvoiceService recordedInvoiceService;

    @Inject
    private MatchingCodeService matchingCodeService;

    @Inject
    private CustomerAccountService customerAccountService;

    @Inject
    private OCCTemplateService oCCTemplateService;

    /**
     * @param paymentDto payment object which encapsulates the input data sent by client
     * @return the id of payment if created successful otherwise null
     * @throws NoAllOperationUnmatchedException no all operation un matched exception
     * @throws UnbalanceAmountException balance amount exception
     * @throws BusinessException business exception
     * @throws MeveoApiException opencell api exception
     */
    public Long createPayment(PaymentDto paymentDto) throws NoAllOperationUnmatchedException, UnbalanceAmountException, BusinessException, MeveoApiException {
        log.info("create payment for amount:" + paymentDto.getAmount() + " paymentMethodEnum:" + paymentDto.getPaymentMethod() + " isToMatching:" + paymentDto.isToMatching()
                + "  customerAccount:" + paymentDto.getCustomerAccountCode() + "...");

        if (StringUtils.isBlank(paymentDto.getAmount())) {
            missingParameters.add("amount");
        }
        if (StringUtils.isBlank(paymentDto.getCustomerAccountCode())) {
            missingParameters.add("customerAccountCode");
        }
        if (StringUtils.isBlank(paymentDto.getOccTemplateCode())) {
            missingParameters.add("occTemplateCode");
        }
        if (StringUtils.isBlank(paymentDto.getReference())) {
            missingParameters.add("reference");
        }
        if (StringUtils.isBlank(paymentDto.getPaymentMethod())) {
            missingParameters.add("paymentMethod");
        }
        handleMissingParameters();
        CustomerAccount customerAccount = customerAccountService.findByCode(paymentDto.getCustomerAccountCode());
        if (customerAccount == null) {
            throw new BusinessException("Cannot find customer account with code=" + paymentDto.getCustomerAccountCode());
        }

        OCCTemplate occTemplate = oCCTemplateService.findByCode(paymentDto.getOccTemplateCode());
        if (occTemplate == null) {
            throw new BusinessException("Cannot find OCC Template with code=" + paymentDto.getOccTemplateCode());
        }

        Payment payment = new Payment();
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setAmount(paymentDto.getAmount());
        payment.setUnMatchingAmount(paymentDto.getAmount());
        payment.setMatchingAmount(BigDecimal.ZERO);
        payment.setAccountCode(occTemplate.getAccountCode());
        payment.setOccCode(occTemplate.getCode());
        payment.setOccDescription(StringUtils.isBlank(paymentDto.getDescription()) ? occTemplate.getDescription() : paymentDto.getDescription());
        payment.setTransactionCategory(occTemplate.getOccCategory());
        payment.setAccountCodeClientSide(occTemplate.getAccountCodeClientSide());
        payment.setCustomerAccount(customerAccount);
        payment.setReference(paymentDto.getReference());
        payment.setDueDate(paymentDto.getDueDate());
        payment.setTransactionDate(paymentDto.getTransactionDate());
        payment.setMatchingStatus(MatchingStatusEnum.O);
        payment.setPaymentOrder(paymentDto.getPaymentOrder());
        payment.setFees(paymentDto.getFees());
        payment.setComment(paymentDto.getComment());

        // populate customFields
        try {
            populateCustomFields(paymentDto.getCustomFields(), payment, true);
        } catch (MissingParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        paymentService.create(payment);

        int nbOccMatched = 0;
        if (paymentDto.isToMatching()) {
            List<Long> listReferenceToMatch = new ArrayList<Long>();
            if (paymentDto.getListOCCReferenceforMatching() != null) {
                nbOccMatched = paymentDto.getListOCCReferenceforMatching().size();
                for (int i = 0; i < nbOccMatched; i++) {
                    RecordedInvoice accountOperationToMatch = recordedInvoiceService.getRecordedInvoice(paymentDto.getListOCCReferenceforMatching().get(i));
                    if (accountOperationToMatch == null) {
                        throw new BusinessApiException("Cannot find account operation with reference:" + paymentDto.getListOCCReferenceforMatching().get(i));
                    }
                    listReferenceToMatch.add(accountOperationToMatch.getId());
                }
                listReferenceToMatch.add(payment.getId());
                matchingCodeService.matchOperations(null, customerAccount.getCode(), listReferenceToMatch, null, MatchingTypeEnum.A);
            }

        } else {
            log.info("no matching created ");
        }
        log.debug("payment created for amount:" + payment.getAmount());

        return payment.getId();

    }

    /**
     * @param customerAccountCode customer account code
     * @return list of payment dto
     * @throws Exception exception.
     */
    public List<PaymentDto> getPaymentList(String customerAccountCode) throws Exception {
        List<PaymentDto> result = new ArrayList<PaymentDto>();

        CustomerAccount customerAccount = customerAccountService.findByCode(customerAccountCode);

        if (customerAccount == null) {
            throw new EntityDoesNotExistsException(CustomerAccount.class, customerAccountCode);
        }

        customerAccountService.getEntityManager().refresh(customerAccount);

        List<AccountOperation> ops = customerAccount.getAccountOperations();
        for (AccountOperation op : ops) {
            if (op instanceof Payment) {
                Payment p = (Payment) op;
                PaymentDto paymentDto = new PaymentDto();
                paymentDto.setType(p.getType());
                paymentDto.setAmount(p.getAmount());
                paymentDto.setDueDate(p.getDueDate());
                paymentDto.setOccTemplateCode(p.getOccCode());
                paymentDto.setPaymentMethod(p.getPaymentMethod());
                paymentDto.setReference(p.getReference());
                paymentDto.setTransactionDate(p.getTransactionDate());
                paymentDto.setPaymentOrder(p.getOrderNumber());
                paymentDto.setFees(p.getFees());
                paymentDto.setComment(p.getComment());
                paymentDto.setCustomFields(entityToDtoConverter.getCustomFieldsDTO(op, true));
                if (p instanceof AutomatedPayment) {
                    AutomatedPayment ap = (AutomatedPayment) p;
                    paymentDto.setBankCollectionDate(ap.getBankCollectionDate());
                    paymentDto.setBankLot(ap.getBankLot());
                    paymentDto.setDepositDate(ap.getDepositDate());
                }
                result.add(paymentDto);
            } else if (op instanceof OtherCreditAndCharge) {
                OtherCreditAndCharge occ = (OtherCreditAndCharge) op;
                PaymentDto paymentDto = new PaymentDto();
                paymentDto.setType(occ.getType());
                paymentDto.setDescription(op.getOccDescription());
                paymentDto.setAmount(occ.getAmount());
                paymentDto.setDueDate(occ.getDueDate());
                paymentDto.setOccTemplateCode(occ.getOccCode());
                paymentDto.setReference(occ.getReference());
                paymentDto.setTransactionDate(occ.getTransactionDate());
                result.add(paymentDto);
            }
        }
        return result;
    }

    /**
     * @param customerAccountCode customer account code
     * @return balance for customer account
     * @throws BusinessException business exception
     */
    public double getBalance(String customerAccountCode) throws BusinessException {

        CustomerAccount customerAccount = customerAccountService.findByCode(customerAccountCode);

        return customerAccountService.customerAccountBalanceDue(customerAccount, new Date()).doubleValue();
    }

    /**
     * @param cardPaymentRequestDto card payment request
     * @return payment by card response
     * @throws BusinessException business exception
     * @throws NoAllOperationUnmatchedException no all operation matched exception
     * @throws UnbalanceAmountException balance exception
     * @throws MeveoApiException opencell's api exception
     */
    public PayByCardResponseDto payByCard(PayByCardDto cardPaymentRequestDto)
            throws BusinessException, NoAllOperationUnmatchedException, UnbalanceAmountException, MeveoApiException {

        if (StringUtils.isBlank(cardPaymentRequestDto.getCtsAmount())) {
            missingParameters.add("ctsAmount");
        }

        if (StringUtils.isBlank(cardPaymentRequestDto.getCustomerAccountCode())) {
            missingParameters.add("customerAccountCode");
        }
        boolean useCard = false;

        // case card payment
        if (!StringUtils.isBlank(cardPaymentRequestDto.getCardNumber())) {
            useCard = true;
            if (StringUtils.isBlank(cardPaymentRequestDto.getCvv())) {
                missingParameters.add("cvv");
            }
            if (StringUtils.isBlank(cardPaymentRequestDto.getExpiryDate()) || cardPaymentRequestDto.getExpiryDate().length() != 4
                    || !org.apache.commons.lang3.StringUtils.isNumeric(cardPaymentRequestDto.getExpiryDate())) {

                missingParameters.add("expiryDate");
            }
            if (StringUtils.isBlank(cardPaymentRequestDto.getOwnerName())) {
                missingParameters.add("ownerName");
            }
            if (StringUtils.isBlank(cardPaymentRequestDto.getCardType())) {
                missingParameters.add("cardType");
            }
        }
        if (cardPaymentRequestDto.isToMatch()) {
            if (cardPaymentRequestDto.getAoToPay() == null || cardPaymentRequestDto.getAoToPay().isEmpty()) {
                missingParameters.add("aoToPay");
            }
        }

        handleMissingParameters();

        CustomerAccount customerAccount = customerAccountService.findByCode(cardPaymentRequestDto.getCustomerAccountCode());
        if (customerAccount == null) {
            throw new EntityDoesNotExistsException(CustomerAccount.class, cardPaymentRequestDto.getCustomerAccountCode());
        }

        PaymentMethodEnum preferedMethod = customerAccount.getPreferredPaymentMethodType();
        if (preferedMethod != null && PaymentMethodEnum.CARD != preferedMethod) {
            throw new BusinessApiException("Can not process payment as prefered payment method is " + preferedMethod);
        }

        PayByCardResponseDto doPaymentResponseDto = null;
        if (useCard) {

            doPaymentResponseDto = paymentService.payByCard(customerAccount, cardPaymentRequestDto.getCtsAmount(), cardPaymentRequestDto.getCardNumber(),
                cardPaymentRequestDto.getOwnerName(), cardPaymentRequestDto.getCvv(), cardPaymentRequestDto.getExpiryDate(), cardPaymentRequestDto.getCardType(),
                cardPaymentRequestDto.getAoToPay(), cardPaymentRequestDto.isCreateAO(), cardPaymentRequestDto.isToMatch());
        } else {
            doPaymentResponseDto = paymentService.payByCardToken(customerAccount, cardPaymentRequestDto.getCtsAmount(), cardPaymentRequestDto.getAoToPay(),
                cardPaymentRequestDto.isCreateAO(), cardPaymentRequestDto.isToMatch());
        }

        return doPaymentResponseDto;
    }

}
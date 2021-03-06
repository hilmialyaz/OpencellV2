package org.meveo.api.ws;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.account.CreditCategoryDto;
import org.meveo.api.dto.payment.CardPaymentMethodDto;
import org.meveo.api.dto.payment.CardPaymentMethodTokenDto;
import org.meveo.api.dto.payment.CardPaymentMethodTokensDto;
import org.meveo.api.dto.payment.DDRequestLotOpDto;
import org.meveo.api.dto.payment.PayByCardDto;
import org.meveo.api.dto.payment.PayByCardResponseDto;
import org.meveo.api.dto.payment.PaymentDto;
import org.meveo.api.dto.payment.PaymentGatewayDto;
import org.meveo.api.dto.payment.PaymentGatewayResponseDto;
import org.meveo.api.dto.payment.PaymentMethodDto;
import org.meveo.api.dto.payment.PaymentMethodTokenDto;
import org.meveo.api.dto.payment.PaymentMethodTokensDto;
import org.meveo.api.dto.response.CustomerPaymentsResponse;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.payment.CreditCategoriesResponseDto;
import org.meveo.api.dto.response.payment.CreditCategoryResponseDto;
import org.meveo.api.dto.response.payment.DDRequestLotOpsResponseDto;
import org.meveo.model.payments.DDRequestOpStatusEnum;

// TODO: Auto-generated Javadoc
/**
 * The Interface PaymentWs.
 */
@WebService
public interface PaymentWs extends IBaseWs {

    /**
     * Creates the.
     *
     * @param postData the post data
     * @return the action status
     */
    @WebMethod
    public ActionStatus create(@WebParam(name = "PaymentDto") PaymentDto postData);

    /**
     * List.
     *
     * @param customerAccountCode the customer account code
     * @return the customer payments response
     */
    @WebMethod
    public CustomerPaymentsResponse list(@WebParam(name = "customerAccountCode") String customerAccountCode);

    /**
     * create a ddrequestLotOp by dto
     * 
     * @param ddrequestLotOp
     * @return
     */
    @WebMethod
    ActionStatus createDDRequestLotOp(@WebParam(name = "ddrequestLotOp") DDRequestLotOpDto ddrequestLotOp);

    /**
     * list ddrequestLotOps by fromDueDate,toDueDate,status
     * 
     * @param fromDueDate
     * @param toDueDate
     * @param status
     * @return
     */
    @WebMethod
    DDRequestLotOpsResponseDto listDDRequestLotops(@WebParam(name = "fromDueDate") Date fromDueDate, @WebParam(name = "toDueDate") Date toDueDate,
            @WebParam(name = "status") DDRequestOpStatusEnum status);

    /**
     * Make a payment by card. Either with a provided card information, or an existing and preferred card payment method
     * 
     * @param payByCardDto Payment by card information
     * @return Payment by card information
     */
    @WebMethod
    public PayByCardResponseDto payByCard(@WebParam(name = "payByCard") PayByCardDto payByCardDto);

    /************************************************************************************************/
    /**** Card Payment Method ****/
    /************************************************************************************************/

    /**
     * Add a new card payment method. It will be marked as preferred.
     * 
     * @param cardPaymentMethod Card payment method DTO
     * @return Card payment DTO with Token id from payment gateway
     */
    @WebMethod
    @Deprecated // Use addPaymentMthod operation
    public CardPaymentMethodTokenDto addCardPaymentMethod(@WebParam(name = "cardPaymentMethod") CardPaymentMethodDto cardPaymentMethod);

    /**
     * Update existing card payment method.
     * 
     * @param cardPaymentMethod Card payment method DTO
     * @return Action status
     */
    @Deprecated // Use updatePaymentMthod operation
    public ActionStatus updateCardPaymentMethod(@WebParam(name = "cardPaymentMethod") CardPaymentMethodDto cardPaymentMethod);

    /**
     * Remove card payment method. If it was marked as preferred, some other payment method will be marked as preferred
     * 
     * @param id Id
     * @return Action status
     */
    @WebMethod
    @Deprecated // Use removePaymentMthod operation
    public ActionStatus removeCardPaymentMethod(@WebParam(name = "id") Long id);

    /**
     * List available card payment methods for a given customer account identified either by id or by code
     * 
     * @param customerAccountId Customer account id
     * @param customerAccountCode Customer account code
     * @return A list of card payment methods
     */
    @WebMethod
    @Deprecated // Use listPaymentMthod operation
    public CardPaymentMethodTokensDto listCardPaymentMethods(@WebParam(name = "customerAccountId") Long customerAccountId,
            @WebParam(name = "customerAccountCode") String customerAccountCode);

    /**
     * Retrieve card payment method by its id
     * 
     * @param id Id
     * @return Card payment DTO
     */
    @WebMethod
    @Deprecated // Use findPaymentMthod operation
    public CardPaymentMethodTokenDto findCardPaymentMethod(@WebParam(name = "id") Long id);

    /************************************************************************************************/
    /**** Payment Methods ****/
    /************************************************************************************************/

    /**
     * Add a new payment method. It will be marked as preferred.
     * 
     * @param paymentMethod DD payment method DTO
     * @return DD payment DTO with Token id from payment gateway
     */
    @WebMethod
    public PaymentMethodTokenDto addPaymentMethod(@WebParam(name = "paymentMethod") PaymentMethodDto paymentMethod);

    /**
     * Update existing payment method.
     * 
     * @param paymentMethod DD payment method DTO
     * @return Action status
     */
    public ActionStatus updatePaymentMethod(@WebParam(name = "paymentMethod") PaymentMethodDto paymentMethod);

    /**
     * Remove payment method. If it was marked as preferred, some other payment method will be marked as preferred
     * 
     * @param id Id
     * @return Action status
     */
    @WebMethod
    public ActionStatus removePaymentMethod(@WebParam(name = "id") Long id);

    /**
     * List payment methods on searching by any payment method field in addition to paging and sorting.
     * 
     * @param pagingAndFiltering
     * @return List payment methods matching
     */
    @WebMethod
    public PaymentMethodTokensDto listPaymentMethods(@WebParam(name = "pagingAndFiltering") PagingAndFiltering pagingAndFiltering);

    /**
     * Retrieve payment method by its id
     * 
     * @param id Id
     * @return DD payment DTO
     */
    @WebMethod
    public PaymentMethodTokenDto findPaymentMethod(@WebParam(name = "id") Long id);

    /**
     * Credit Category
     */
    @WebMethod
    ActionStatus createCreditCategory(@WebParam(name = "postData") CreditCategoryDto postData);

    /**
     * Update credit category.
     *
     * @param postData the post data
     * @return the action status
     */
    @WebMethod
    ActionStatus updateCreditCategory(@WebParam(name = "postData") CreditCategoryDto postData);

    /**
     * Creates the or update credit category.
     *
     * @param postData the post data
     * @return the action status
     */
    @WebMethod
    ActionStatus createOrUpdateCreditCategory(@WebParam(name = "postData") CreditCategoryDto postData);

    /**
     * Find credit category.
     *
     * @param creditCategoryCode the credit category code
     * @return the credit category response dto
     */
    @WebMethod
    CreditCategoryResponseDto findCreditCategory(@WebParam(name = "creditCategoryCode") String creditCategoryCode);

    /**
     * List credit category.
     *
     * @return the credit categories response dto
     */
    @WebMethod
    CreditCategoriesResponseDto listCreditCategory();

    /**
     * Removes the credit category.
     *
     * @param creditCategoryCode the credit category code
     * @return the action status
     */
    @WebMethod
    ActionStatus removeCreditCategory(@WebParam(name = "creditCategoryCode") String creditCategoryCode);

    /**
     * Add a new payment gateway.
     * 
     * @param paymentGateway payment gateway DTO
     * @return the paymentGateway dto created
     */
    @WebMethod
    public PaymentGatewayResponseDto addPaymentGateway(@WebParam(name = "paymentGateway") PaymentGatewayDto paymentGateway);

    /**
     * Update existing payment gateway.
     * 
     * @param paymentGateway payment gateway DTO
     * @return Action status
     */
    @WebMethod
    public ActionStatus updatePaymentGateway(@WebParam(name = "paymentGateway") PaymentGatewayDto paymentGateway);

    /**
     * Remove payment gateway.
     * 
     * @param code code
     * @return Action status
     */
    @WebMethod
    public ActionStatus removePaymentGateway(@WebParam(name = "code") String code);

    /**
     * List payment gateways on searching by any payment gateway fields in addition to paging and sorting.
     * 
     * @return A list of payment gateways
     */
    @WebMethod
    public PaymentGatewayResponseDto listPaymentGateways(@WebParam(name = "pagingAndFiltering") PagingAndFiltering pagingAndFiltering);

    /**
     * Retrieve payment gateway by its id
     * 
     * @param code code
     * @return payment DTO
     */
    @WebMethod
    public PaymentGatewayResponseDto findPaymentGateway(@WebParam(name = "code") String code);

    /**
     * Create or update payment gateway.
     * 
     * @param paymentGateway payment gateway DTO
     * @return the paymentGateway dto created
     */
    @WebMethod
    public PaymentGatewayResponseDto createOrUpdatePaymentGateway(@WebParam(name = "paymentGateway") PaymentGatewayDto paymentGateway);

}

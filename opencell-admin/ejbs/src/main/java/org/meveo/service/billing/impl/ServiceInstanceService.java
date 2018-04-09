/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.billing.impl;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.IncorrectServiceInstanceException;
import org.meveo.admin.exception.IncorrectSusbcriptionException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.*;
import org.meveo.model.catalog.*;
import org.meveo.model.shared.DateUtils;
import org.meveo.service.base.BusinessService;
import org.meveo.service.catalog.impl.ServiceTemplateService;
import org.meveo.service.script.service.ServiceModelScriptService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * ServiceInstanceService.
 *
 * @author anasseh
 */
@Stateless
public class ServiceInstanceService extends BusinessService<ServiceInstance> {
    /**
     * ServiceModelScriptService
     */
    @Inject
    private ServiceModelScriptService serviceModelScriptService;

    /**
     * RecurringChargeInstanceService
     */
    @Inject
    private RecurringChargeInstanceService recurringChargeInstanceService;

    /**
     * OneShotChargeInstanceService
     */
    @Inject
    private OneShotChargeInstanceService oneShotChargeInstanceService;

    /**
     * UsageChargeInstanceService
     */
    @Inject
    private UsageChargeInstanceService usageChargeInstanceService;

    /**
     * WalletOperationService
     */
    @Inject
    private WalletOperationService walletOperationService;

    /**
     * ServiceTemplateService
     */
    @Inject
    ServiceTemplateService serviceTemplateService;

    /**
     * Find a service instance list by subscription entity, service template code and service instance status list.
     *
     * @param code         the service template code
     * @param subscription the subscription entity
     * @param statuses     service instance statuses
     * @return the ServiceInstance list found
     */
    @SuppressWarnings("unchecked")
    public List<ServiceInstance> findByCodeSubscriptionAndStatus(String code, Subscription subscription, InstanceStatusEnum... statuses) {
        List<ServiceInstance> serviceInstances = null;
        try {
            log.debug("start of find {} by code and subscription/status (code={}) ..", "ServiceInstance", code);
            QueryBuilder qb = new QueryBuilder(ServiceInstance.class, "c");
            qb.addCriterion("c.code", "=", code, true);
            qb.addCriterion("c.subscription", "=", subscription, true);
            qb.startOrClause();
            if (statuses != null && statuses.length > 0) {
                for (InstanceStatusEnum status : statuses) {
                    qb.addCriterionEnum("c.status", status);
                }
            }
            qb.endOrClause();

            serviceInstances = (List<ServiceInstance>) qb.getQuery(getEntityManager()).getResultList();
            log.debug("end of find {} by code and subscription/status (code={}). Result found={}.", "ServiceInstance", code,
                    serviceInstances != null && !serviceInstances.isEmpty());
        } catch (NoResultException nre) {
            log.debug("findByCodeAndSubscription : no service has been found");
        } catch (Exception e) {
            log.error("findByCodeAndSubscription error={} ", e);
        }

        return serviceInstances;
    }

    /**
     * Instantiate a service
     *
     * @param serviceInstance service instance to instantiate
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void serviceInstanciation(ServiceInstance serviceInstance) throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {
        serviceInstanciation(serviceInstance, null, null, false);
    }

    /**
     * @param serviceInstance     service instance to instantiate
     * @param descriptionOverride overridden description
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void serviceInstanciation(ServiceInstance serviceInstance, String descriptionOverride)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {
        serviceInstanciation(serviceInstance, descriptionOverride, null, null, false);
    }

    // validate service is in offer service list
    private boolean checkServiceAssociatedWithOffer(ServiceInstance serviceInstance) throws BusinessException {
        OfferTemplate offer = serviceInstance.getSubscription().getOffer();
        if (offer != null && !offer.containsServiceTemplate(serviceInstance.getServiceTemplate())) {
            throw new BusinessException("Service " + serviceInstance.getCode() + " is not associated with Offer");
        }
        log.debug("check service {} is associated with offer {}", serviceInstance.getCode(), offer.getCode());
        return true;
    }

    /**
     * @param serviceInstance    service instance
     * @param subscriptionAmount subscription amount
     * @param terminationAmount  termination amount
     * @param isVirtual          true/false
     * @throws BusinessException business exception
     */
    public void serviceInstanciation(ServiceInstance serviceInstance, BigDecimal subscriptionAmount, BigDecimal terminationAmount, boolean isVirtual) throws BusinessException {
        serviceInstanciation(serviceInstance, null, subscriptionAmount, terminationAmount, isVirtual);
    }

    /**
     * @param serviceInstance     service instance
     * @param descriptionOverride overridden description
     * @param subscriptionAmount  subscription amount
     * @param terminationAmount   termination amount
     * @param isVirtual           true/false
     * @throws BusinessException business exception
     */
    public void serviceInstanciation(ServiceInstance serviceInstance, String descriptionOverride, BigDecimal subscriptionAmount, BigDecimal terminationAmount, boolean isVirtual)
            throws BusinessException {

        log.debug("Will instantiate service {} for subscription {} quantity {}", serviceInstance.getCode(), serviceInstance.getSubscription().getCode(),
                serviceInstance.getQuantity());

        ServiceTemplate serviceTemplate = serviceInstance.getServiceTemplate();

        Subscription subscription = serviceInstance.getSubscription();

        if (subscription.getStatus() == SubscriptionStatusEnum.RESILIATED || subscription.getStatus() == SubscriptionStatusEnum.CANCELED) {
            throw new IncorrectSusbcriptionException("Subscription is not active");
        }
        if (!isVirtual) {
            if (ParamBean.ALLOW_SERVICE_MULTI_INSTANTIATION) {
                List<ServiceInstance> serviceInstances = findByCodeSubscriptionAndStatus(serviceTemplate.getCode(), subscription, InstanceStatusEnum.INACTIVE);
                if (serviceInstances != null && !serviceInstances.isEmpty()) {
                    throw new IncorrectServiceInstanceException(
                            "Service instance with code=" + serviceInstance.getCode() + ", subscription code=" + subscription.getCode() + " is already instantiated.");
                }
            } else {
                List<ServiceInstance> serviceInstances = findByCodeSubscriptionAndStatus(serviceTemplate.getCode(), subscription, InstanceStatusEnum.INACTIVE,
                        InstanceStatusEnum.ACTIVE);
                if (serviceInstances != null && !serviceInstances.isEmpty()) {
                    throw new IncorrectServiceInstanceException(
                            "Service instance with code=" + serviceInstance.getCode() + " and subscription code=" + subscription.getCode() + " is already instantiated or activated.");
                }
            }
        }
        checkServiceAssociatedWithOffer(serviceInstance);

        if (serviceInstance.getSubscriptionDate() == null) {
            serviceInstance.setSubscriptionDate(subscription.getSubscriptionDate() != null ? subscription.getSubscriptionDate() : new Date());
        }
        serviceInstance.setStatus(InstanceStatusEnum.INACTIVE);
        serviceInstance.setCode(serviceTemplate.getCode());
        if (!StringUtils.isBlank(descriptionOverride)) {
            serviceInstance.setDescription(descriptionOverride);
        } else {
            serviceInstance.setDescription(serviceTemplate.getDescription());
        }
        serviceInstance.setInvoicingCalendar(serviceInstance.getServiceTemplate().getInvoicingCalendar());

        if (!isVirtual) {
            create(serviceInstance);
        }

        subscription.getServiceInstances().add(serviceInstance);

        for (ServiceChargeTemplate<RecurringChargeTemplate> serviceChargeTemplate : serviceTemplate.getServiceRecurringCharges()) {
            RecurringChargeInstance chargeInstance = recurringChargeInstanceService.recurringChargeInstanciation(serviceInstance, serviceChargeTemplate.getChargeTemplate(),
                    isVirtual);
            serviceInstance.getRecurringChargeInstances().add(chargeInstance);
        }

        for (ServiceChargeTemplate<OneShotChargeTemplate> serviceChargeTemplate : serviceTemplate.getServiceSubscriptionCharges()) {
            OneShotChargeInstance chargeInstance = oneShotChargeInstanceService.oneShotChargeInstanciation(serviceInstance, serviceChargeTemplate.getChargeTemplate(),
                    subscriptionAmount, null, true, isVirtual);
            serviceInstance.getSubscriptionChargeInstances().add(chargeInstance);
        }

        for (ServiceChargeTemplate<OneShotChargeTemplate> serviceChargeTemplate : serviceTemplate.getServiceTerminationCharges()) {
            OneShotChargeInstance chargeInstance = oneShotChargeInstanceService.oneShotChargeInstanciation(serviceInstance, serviceChargeTemplate.getChargeTemplate(),
                    terminationAmount, null, false, isVirtual);
            serviceInstance.getTerminationChargeInstances().add(chargeInstance);
        }

        for (ServiceChargeTemplateUsage serviceUsageChargeTemplate : serviceTemplate.getServiceUsageCharges()) {
            UsageChargeInstance chargeInstance = usageChargeInstanceService.usageChargeInstanciation(serviceInstance, serviceUsageChargeTemplate, isVirtual);
            serviceInstance.getUsageChargeInstances().add(chargeInstance);
        }

        if (!isVirtual) {
            // execute instantiation script
            if (serviceInstance.getServiceTemplate().getBusinessServiceModel() != null && serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript() != null) {
                serviceModelScriptService.instantiateServiceInstance(serviceInstance, serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript().getCode());
            }
        }
    }

    /**
     * Activate a service, the subscription charges are applied.
     *
     * @param serviceInstance   service instance
     * @param amountWithoutTax  amount without tax
     * @param amountWithoutTax2 amount without tax
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void serviceActivation(ServiceInstance serviceInstance, BigDecimal amountWithoutTax, BigDecimal amountWithoutTax2)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {
        serviceActivation(serviceInstance, true, amountWithoutTax, amountWithoutTax2);
    }

    public void reactivateServiceIfSuspend(ServiceInstance serviceInstance, Date reactivationDate)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {

        String serviceCode = serviceInstance.getCode();
        if (reactivationDate == null) {
            reactivationDate = new Date();
        }

        Subscription subscription = serviceInstance.getSubscription();
        if (subscription == null) {
            throw new IncorrectSusbcriptionException("service Instance does not have subscrption . serviceCode=" + serviceInstance.getCode());
        }
        ServiceTemplate serviceTemplate = serviceInstance.getServiceTemplate();
        checkServiceAssociatedWithOffer(serviceInstance);

        serviceInstance.setStatus(InstanceStatusEnum.ACTIVE);
        serviceInstance.setSubscriptionDate(reactivationDate);
        serviceInstance.setDescription(serviceTemplate.getDescription());
        serviceInstance.setTerminationDate(null);

        String descriptionOverride = serviceTemplate.getDescriptionOverride();
        serviceTemplate = serviceTemplateService.findById(serviceTemplate.getId());


        ServiceInstance serviceInstanceNew = new ServiceInstance();
        serviceInstanceNew.setCode(serviceTemplate.getCode());

        serviceInstanceNew.setDescription(descriptionOverride);

        serviceInstanceNew.setServiceTemplate(serviceTemplate);
        serviceInstanceNew.setSubscription((Subscription) serviceInstance.getSubscription());
        if (serviceInstance.getSubscriptionDate() != null) {
            serviceInstanceNew.setSubscriptionDate(serviceInstance.getSubscriptionDate());
        } else {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            serviceInstanceNew.setSubscriptionDate(calendar.getTime());
        }
        serviceInstanceNew.setQuantity(serviceInstance.getQuantity());
        serviceInstanciation(serviceInstanceNew, descriptionOverride);

        serviceActivation(serviceInstanceNew, null, null);

        terminateService(serviceInstance, new Date(), false, false, false, "ordernumber", null);


    }


    /**
     * Activate a service, the subscription charges can be applied or not.
     *
     * @param serviceInstance          service instance
     * @param applySubscriptionCharges true/false
     * @param amountWithoutTax         amount without tax
     * @param amountWithoutTax2        amount without tax
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void serviceActivation(ServiceInstance serviceInstance, boolean applySubscriptionCharges, BigDecimal amountWithoutTax, BigDecimal amountWithoutTax2)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {

        if(serviceInstance.getStatus()== InstanceStatusEnum.SUSPENDED){
            reactivateServiceIfSuspend(serviceInstance,null);
            return;
        }

        Subscription subscription = serviceInstance.getSubscription();

        log.debug("Will activate service {} for subscription {} quantity {}", serviceInstance.getCode(), serviceInstance.getSubscription().getCode(),
                serviceInstance.getQuantity());

        // String serviceCode = serviceInstance.getCode();
        if (subscription == null) {
            throw new IncorrectSusbcriptionException("Subscription does not exist. code=" + serviceInstance.getSubscription().getCode());
        }

        if (subscription.getStatus() == SubscriptionStatusEnum.RESILIATED || subscription.getStatus() == SubscriptionStatusEnum.CANCELED) {
            throw new IncorrectServiceInstanceException("Subscription is " + subscription.getStatus());
        }

        if (serviceInstance.getStatus() == InstanceStatusEnum.ACTIVE) {
            throw new IncorrectServiceInstanceException("Can not activate a ServiceInstance that is " + serviceInstance.getStatus());
        }

        if (!ParamBean.ALLOW_SERVICE_MULTI_INSTANTIATION) {
            List<ServiceInstance> serviceInstances = findByCodeSubscriptionAndStatus(serviceInstance.getCode(), subscription, InstanceStatusEnum.ACTIVE);
            if (serviceInstances != null && !serviceInstances.isEmpty()) {
                throw new IncorrectServiceInstanceException(
                        "Service instance with code=" + serviceInstance.getCode() + ", subscription code=" + subscription.getCode() + " is already activated.");
            }
        }

        checkServiceAssociatedWithOffer(serviceInstance);

        subscription.setStatus(SubscriptionStatusEnum.ACTIVE);

        if (serviceInstance.getSubscriptionDate() == null) {
            serviceInstance.setSubscriptionDate(new Date());
        }

        int agreementMonthTerm = 0;

        // set end Agreement Date
        Date serviceEngAgreementDate = null;
        if (agreementMonthTerm > 0) {
            serviceEngAgreementDate = DateUtils.addMonthsToDate(subscription.getSubscriptionDate(), agreementMonthTerm);
        }

        if (serviceEngAgreementDate == null) {
            if (serviceInstance.getEndAgreementDate() == null) {
                serviceInstance.setEndAgreementDate(subscription.getEndAgreementDate());
            }
        } else {
            serviceInstance.setEndAgreementDate(serviceEngAgreementDate);
        }

        // apply subscription charges
        if (applySubscriptionCharges) {
            for (OneShotChargeInstance oneShotChargeInstance : serviceInstance.getSubscriptionChargeInstances()) {
                oneShotChargeInstance.setQuantity(serviceInstance.getQuantity());
                oneShotChargeInstance.setChargeDate(serviceInstance.getSubscriptionDate());

                oneShotChargeInstanceService.oneShotChargeApplication(subscription, oneShotChargeInstance, serviceInstance.getSubscriptionDate(),
                        oneShotChargeInstance.getQuantity(), serviceInstance.getOrderNumber());
                oneShotChargeInstance.setStatus(InstanceStatusEnum.CLOSED);
                oneShotChargeInstanceService.update(oneShotChargeInstance);
            }
        } else {
            log.debug("ServiceActivation: subscription charges are not applied.");
        }

        // activate recurring charges

        for (RecurringChargeInstance recurringChargeInstance : serviceInstance.getRecurringChargeInstances()) {

            // application of subscription prorata
            recurringChargeInstance.setSubscriptionDate(serviceInstance.getSubscriptionDate());
            recurringChargeInstance.setChargeDate(serviceInstance.getSubscriptionDate());
            recurringChargeInstance.setQuantity(serviceInstance.getQuantity());
            recurringChargeInstance.setStatus(InstanceStatusEnum.ACTIVE);
            recurringChargeInstanceService.update(recurringChargeInstance);
            walletOperationService.chargeSubscription(recurringChargeInstance);

            if (recurringChargeInstance.getRecurringChargeTemplate().getDurationTermInMonth() != null) {
                if (recurringChargeInstance.getRecurringChargeTemplate().getDurationTermInMonth() > agreementMonthTerm) {
                    agreementMonthTerm = recurringChargeInstance.getRecurringChargeTemplate().getDurationTermInMonth();
                }
            }
            int nbRating = recurringChargeInstanceService.applyRecurringCharge(recurringChargeInstance.getId(),
                    serviceInstance.getRateUntilDate() == null ? new Date() : serviceInstance.getRateUntilDate(), serviceInstance.getRateUntilDate() != null);
            log.debug("rated " + nbRating + " missing periods during activation");
        }
        for (UsageChargeInstance usageChargeInstance : serviceInstance.getUsageChargeInstances()) {
            usageChargeInstanceService.activateUsageChargeInstance(usageChargeInstance);
        }

        serviceInstance.setStatus(InstanceStatusEnum.ACTIVE);
        serviceInstance = update(serviceInstance);

        // execute subscription script
        if (serviceInstance.getServiceTemplate().getBusinessServiceModel() != null && serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript() != null) {
            serviceModelScriptService.activateServiceInstance(serviceInstance, serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript().getCode());
        }
    }

    /**
     * Terminate a service.
     *
     * @param serviceInstance   service instance
     * @param terminationDate   termination date
     * @param terminationReason termination reason
     * @param orderNumber       order number
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service exception
     * @throws BusinessException                 business exception
     */
    public void terminateService(ServiceInstance serviceInstance, Date terminationDate, SubscriptionTerminationReason terminationReason, String orderNumber)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {

        terminateService(serviceInstance, terminationDate, terminationReason.isApplyAgreement(), terminationReason.isApplyReimbursment(),
                terminationReason.isApplyTerminationCharges(), orderNumber, terminationReason);

    }

    /**
     * Terminate a service.
     *
     * @param serviceInstance         service instance
     * @param terminationDate         termination date
     * @param applyAgreement          apply agreement
     * @param applyReimbursment       apply reimbursement
     * @param applyTerminationCharges apply termination charges
     * @param orderNumber             order number
     * @param terminationReason       termination reason
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void terminateService(ServiceInstance serviceInstance, Date terminationDate, boolean applyAgreement, boolean applyReimbursment, boolean applyTerminationCharges,
                                 String orderNumber, SubscriptionTerminationReason terminationReason) throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {

        if (serviceInstance.getId() != null) {
            log.info("terminateService terminationDate={}, serviceInstanceId={}", terminationDate, serviceInstance.getId());
        }
        if (terminationDate == null) {
            terminationDate = new Date();
        }

        String serviceCode = serviceInstance.getCode();
        Subscription subscription = serviceInstance.getSubscription();
        if (subscription == null) {
            throw new IncorrectSusbcriptionException("service Instance does not have subscrption . serviceCode=" + serviceInstance.getCode());
        }
        if (serviceInstance.getStatus() == InstanceStatusEnum.INACTIVE) {
            throw new IncorrectServiceInstanceException("service instance is inactive. service Code=" + serviceCode + ",subscription Code" + subscription.getCode());
        }
        serviceInstance = refreshOrRetrieve(serviceInstance);

        // execute termination script
        if (serviceInstance.getServiceTemplate().getBusinessServiceModel() != null && serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript() != null) {
            serviceModelScriptService.terminateServiceInstance(serviceInstance, serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript().getCode(),
                    terminationDate, terminationReason);
        }

        for (RecurringChargeInstance recurringChargeInstance : serviceInstance.getRecurringChargeInstances()) {
            Date chargeDate = recurringChargeInstance.getChargeDate();
            Date nextChargeDate = recurringChargeInstance.getNextChargeDate();
            Date storedNextChargeDate = recurringChargeInstance.getNextChargeDate();

            if (recurringChargeInstance.getRecurringChargeTemplate().getApplyInAdvance() != null && !recurringChargeInstance.getRecurringChargeTemplate().getApplyInAdvance()) {
                nextChargeDate = recurringChargeInstance.getChargeDate();
            }

            Date endDate = terminationDate;

            if (applyAgreement && serviceInstance.getEndAgreementDate() != null && terminationDate.before(serviceInstance.getEndAgreementDate())) {
                endDate = serviceInstance.getEndAgreementDate();
            }
            log.debug("chargeDate={}, storedNextChargeDate={}, enDate {}", chargeDate, storedNextChargeDate, endDate);
            if (endDate.after(nextChargeDate)) {
                walletOperationService.applyChargeAgreement(recurringChargeInstance, recurringChargeInstance.getRecurringChargeTemplate(), endDate);
            } else if (applyReimbursment) {
                Date endAgreementDate = recurringChargeInstance.getServiceInstance().getEndAgreementDate();
                log.debug("terminationDate={}, endAgreementDate={}, nextChargeDate={}", terminationDate, endAgreementDate, nextChargeDate);
                if (applyAgreement && endAgreementDate != null && terminationDate.before(endAgreementDate)) {
                    if (endAgreementDate.before(nextChargeDate)) {
                        recurringChargeInstance.setTerminationDate(endAgreementDate);
                        walletOperationService.applyReimbursment(recurringChargeInstance);
                    }

                } else if (terminationDate.before(storedNextChargeDate)) {
                    recurringChargeInstance.setTerminationDate(terminationDate);
                    walletOperationService.applyReimbursment(recurringChargeInstance);
                }

            }
            recurringChargeInstance.setStatus(InstanceStatusEnum.TERMINATED);
            recurringChargeInstanceService.update(recurringChargeInstance);
        }

        if (applyTerminationCharges) {
            for (OneShotChargeInstance oneShotChargeInstance : serviceInstance.getTerminationChargeInstances()) {
                if (oneShotChargeInstance.getStatus() == InstanceStatusEnum.INACTIVE) {
                    log.debug("applying the termination charge {}", oneShotChargeInstance.getCode());
                    oneShotChargeInstance.setChargeDate(terminationDate);
                    oneShotChargeInstanceService.oneShotChargeApplication(subscription, oneShotChargeInstance, terminationDate, oneShotChargeInstance.getQuantity(), orderNumber);
                    oneShotChargeInstance.setStatus(InstanceStatusEnum.CLOSED);
                } else {
                    log.debug("we do not apply the termination charge because of its status {}", oneShotChargeInstance.getCode(), oneShotChargeInstance.getStatus());
                }
            }
        }

        for (UsageChargeInstance usageChargeInstance : serviceInstance.getUsageChargeInstances()) {
            usageChargeInstanceService.terminateUsageChargeInstance(usageChargeInstance, terminationDate);
        }

        serviceInstance.setTerminationDate(terminationDate);
        serviceInstance.setStatus(InstanceStatusEnum.TERMINATED);
        if (terminationReason != null) {
            serviceInstance.setSubscriptionTerminationReason(terminationReason);
        }
        update(serviceInstance);
    }

    /**
     * @param serviceInstance service instance
     * @param terminationDate termination date
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void updateTerminationMode(ServiceInstance serviceInstance, Date terminationDate)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {
        log.info("updateTerminationMode terminationDate={},serviceInstanceId={}", terminationDate, serviceInstance.getId());

        SubscriptionTerminationReason newReason = serviceInstance.getSubscriptionTerminationReason();

        log.info("updateTerminationMode terminationDate={},serviceInstanceId={},newApplyReimbursment=#2,newApplyAgreement=#3,newApplyTerminationCharges=#4", terminationDate,
                serviceInstance.getId(), newReason.isApplyReimbursment(), newReason.isApplyAgreement(), newReason.isApplyTerminationCharges());

        String serviceCode = serviceInstance.getCode();
        Subscription subscription = serviceInstance.getSubscription();
        if (subscription == null) {
            throw new IncorrectSusbcriptionException("service Instance does not have subscrption . serviceCode=" + serviceInstance.getCode());
        }

        if (serviceInstance.getStatus() != InstanceStatusEnum.TERMINATED) {
            throw new IncorrectServiceInstanceException("service instance is not terminated. service Code=" + serviceCode + ",subscription Code" + subscription.getCode());
        }

        terminateService(serviceInstance, terminationDate, newReason.isApplyAgreement(), newReason.isApplyReimbursment(), newReason.isApplyTerminationCharges(), null, newReason);

    }

    /**
     * @param serviceInstance service instance
     * @param suspensionDate  suspension date
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void serviceSuspension(ServiceInstance serviceInstance, Date suspensionDate)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {

        String serviceCode = serviceInstance.getCode();

        Subscription subscription = serviceInstance.getSubscription();
        if (subscription == null) {
            throw new IncorrectSusbcriptionException("service Instance does not have subscrption . serviceCode=" + serviceCode);
        }

        if (serviceInstance.getStatus() != InstanceStatusEnum.ACTIVE) {
            throw new IncorrectServiceInstanceException("service instance is not active. service Code=" + serviceCode + ",subscription Code" + subscription.getCode());
        }

        if (serviceInstance.getServiceTemplate().getBusinessServiceModel() != null && serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript() != null) {
            serviceModelScriptService.suspendServiceInstance(serviceInstance, serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript().getCode(), suspensionDate);
        }

        for (RecurringChargeInstance recurringChargeInstance : serviceInstance.getRecurringChargeInstances()) {
            if (recurringChargeInstance.getStatus() == InstanceStatusEnum.ACTIVE) {
                recurringChargeInstanceService.recurringChargeSuspension(recurringChargeInstance.getId(), suspensionDate);
            }

        }

        for (UsageChargeInstance usageChargeInstance : serviceInstance.getUsageChargeInstances()) {
            usageChargeInstanceService.suspendUsageChargeInstance(usageChargeInstance, suspensionDate);
        }

        serviceInstance.setStatus(InstanceStatusEnum.SUSPENDED);
        serviceInstance.setTerminationDate(suspensionDate);
        update(serviceInstance);
    }

    /**
     * @param serviceInstance  service instance
     * @param reactivationDate reactivation date
     * @throws IncorrectSusbcriptionException    incorrect subscription exception
     * @throws IncorrectServiceInstanceException incorrect service instance exception
     * @throws BusinessException                 business exception
     */
    public void serviceReactivation(ServiceInstance serviceInstance, Date reactivationDate)
            throws IncorrectSusbcriptionException, IncorrectServiceInstanceException, BusinessException {

        String serviceCode = serviceInstance.getCode();
        if (reactivationDate == null) {
            reactivationDate = new Date();
        }

        Subscription subscription = serviceInstance.getSubscription();
        if (subscription == null) {
            throw new IncorrectSusbcriptionException("service Instance does not have subscrption . serviceCode=" + serviceInstance.getCode());
        }
        ServiceTemplate serviceTemplate = serviceInstance.getServiceTemplate();
        if (serviceInstance.getStatus() != InstanceStatusEnum.SUSPENDED) {
            throw new IncorrectServiceInstanceException("service instance is not suspended. service Code=" + serviceCode + ",subscription Code" + subscription.getCode());
        }
        checkServiceAssociatedWithOffer(serviceInstance);

        serviceInstance.setStatus(InstanceStatusEnum.ACTIVE);
        serviceInstance.setSubscriptionDate(reactivationDate);
        serviceInstance.setDescription(serviceTemplate.getDescription());
        serviceInstance.setTerminationDate(null);

        for (RecurringChargeInstance recurringChargeInstance : serviceInstance.getRecurringChargeInstances()) {
            if (recurringChargeInstance.getStatus() == InstanceStatusEnum.SUSPENDED) {
                recurringChargeInstanceService.recurringChargeReactivation(serviceInstance, subscription, reactivationDate);
            }
        }

        for (UsageChargeInstance usageChargeInstance : serviceInstance.getUsageChargeInstances()) {
            if (usageChargeInstance.getStatus() == InstanceStatusEnum.SUSPENDED) {
                usageChargeInstanceService.reactivateUsageChargeInstance(usageChargeInstance, reactivationDate);
            }
        }
        update(serviceInstance);

        if (serviceInstance.getServiceTemplate().getBusinessServiceModel() != null && serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript() != null) {
            serviceModelScriptService.reactivateServiceInstance(serviceInstance, serviceInstance.getServiceTemplate().getBusinessServiceModel().getScript().getCode(),
                    reactivationDate);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ServiceInstance> findByServiceTemplate(ServiceTemplate serviceTemplate) {

        QueryBuilder qb = new QueryBuilder(ServiceInstance.class, "i");

        try {
            qb.addCriterionEntity("serviceTemplate", serviceTemplate);

            return (List<ServiceInstance>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public ServiceInstance update(ServiceInstance entity) throws BusinessException {

        boolean quantityChanged = entity.isQuantityChanged();

        entity = super.update(entity);
        // update quantity in charges

        if (entity.getStatus() == InstanceStatusEnum.INACTIVE || quantityChanged) {
            if (entity.getRecurringChargeInstances() != null) {
                for (RecurringChargeInstance chargeInstance : entity.getRecurringChargeInstances()) {
                    if (entity.getQuantity() == null || chargeInstance.getQuantity() == null || entity.getQuantity().compareTo(chargeInstance.getQuantity()) != 0) {
                        chargeInstance.setQuantity(entity.getQuantity());
                    }
                }
            }
            if (entity.getSubscriptionChargeInstances() != null) {
                for (OneShotChargeInstance chargeInstance : entity.getSubscriptionChargeInstances()) {
                    if (entity.getQuantity() == null || chargeInstance.getQuantity() == null || entity.getQuantity().compareTo(chargeInstance.getQuantity()) != 0) {
                        chargeInstance.setQuantity(entity.getQuantity());
                    }
                }
            }

            if (entity.getTerminationChargeInstances() != null) {
                for (OneShotChargeInstance chargeInstance : entity.getTerminationChargeInstances()) {
                    if (entity.getQuantity() == null || chargeInstance.getQuantity() == null || entity.getQuantity().compareTo(chargeInstance.getQuantity()) != 0) {
                        chargeInstance.setQuantity(entity.getQuantity());
                    }
                }
            }
        }

        return entity;

    }

    @SuppressWarnings("unchecked")
    public List<ServiceInstance> listServiceInstance(String subscriptionCode, String serviceInstanceCode) {
        List<ServiceInstance> serviceInstances = null;
        try {
            QueryBuilder qb = new QueryBuilder(ServiceInstance.class, "c");
            qb.addCriterion("c.code", "=", serviceInstanceCode, true);
            qb.addCriterion("c.subscription.code", "=", subscriptionCode, true);
            serviceInstances = (List<ServiceInstance>) qb.getQuery(getEntityManager()).getResultList();
            log.debug("end of find {} by code (code={}). Result found={}.", new Object[]{"ServiceInstance", serviceInstanceCode, serviceInstances != null});

        } catch (NoResultException nre) {
            log.debug("listServiceInstance : no service has been found");

        } catch (Exception e) {
            log.error("listServiceInstance error={} ", e.getMessage());
        }

        return serviceInstances;
    }
}
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
package org.meveo.service.admin.impl;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.ApiService;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.account.BusinessAccountModelDto;
import org.meveo.api.dto.catalog.BusinessOfferModelDto;
import org.meveo.api.dto.catalog.BusinessServiceModelDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.response.module.MeveoModuleDtosResponse;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.export.RemoteAuthenticationException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.admin.User;
import org.meveo.model.catalog.BusinessOfferModel;
import org.meveo.model.catalog.BusinessServiceModel;
import org.meveo.model.communication.MeveoInstance;
import org.meveo.model.crm.BusinessAccountModel;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.Provider;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.service.api.EntityToDtoConverter;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.catalog.impl.OfferTemplateService;
import org.meveo.service.catalog.impl.ServiceTemplateService;
import org.meveo.service.communication.impl.MeveoInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.script.module.ModuleScriptService;

@Stateless
public class MeveoModuleService extends BusinessService<MeveoModule> {

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    protected EntityToDtoConverter entityToDtoConverter;

    @Inject
    private ModuleScriptService moduleScriptService;

    @Inject
    private ServiceTemplateService serviceTemplateService;

    @Inject
    private OfferTemplateService offerTemplateService;

    @Inject
    private MeveoInstanceService meveoInstanceService;

    /**
     * import module from remote meveo instance
     * 
     * @param meveoInstance
     * @return
     * @throws MeveoApiException
     * @throws RemoteAuthenticationException
     */
    public List<MeveoModuleDto> downloadModulesFromMeveoInstance(MeveoInstance meveoInstance) throws BusinessException, RemoteAuthenticationException {
        List<MeveoModuleDto> result = null;
        try {
            String url = "api/rest/module/list";
            String baseurl = meveoInstance.getUrl().endsWith("/") ? meveoInstance.getUrl() : meveoInstance.getUrl() + "/";
            String username = meveoInstance.getAuthUsername() != null ? meveoInstance.getAuthUsername() : "";
            String password = meveoInstance.getAuthPassword() != null ? meveoInstance.getAuthPassword() : "";
            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(baseurl + url);
            BasicAuthentication basicAuthentication = new BasicAuthentication(username, password);
            target.register(basicAuthentication);

            Response response = target.request().get();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED || response.getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new RemoteAuthenticationException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                } else {
                    throw new BusinessException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                }
            }

            MeveoModuleDtosResponse resultDto = response.readEntity(MeveoModuleDtosResponse.class);
            log.debug("response {}", resultDto);
            if (resultDto == null || ActionStatusEnum.SUCCESS != resultDto.getActionStatus().getStatus()) {
                throw new BusinessException("Code " + resultDto.getActionStatus().getErrorCode() + ", info " + resultDto.getActionStatus().getMessage());
            }
            result = resultDto.getModules();
            if (result != null) {
                Collections.sort(result, new Comparator<MeveoModuleDto>() {
                    @Override
                    public int compare(MeveoModuleDto dto1, MeveoModuleDto dto2) {
                        return dto1.getCode().compareTo(dto2.getCode());
                    }
                });
            }
            return result;

        } catch (Exception e) {
            log.error("Failed to communicate {}. Reason {}", meveoInstance.getCode(), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            throw new BusinessException("Fail to communicate " + meveoInstance.getCode() + ". Error " + (e == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    /**
     * Publish meveo module with DTO items to remote meveo instance
     * 
     * @param module
     * @param meveoInstance
     * @throws MeveoApiException
     * @throws RemoteAuthenticationException
     */
    @SuppressWarnings("unchecked")
    public void publishModule2MeveoInstance(MeveoModule module, MeveoInstance meveoInstance, User currentUser) throws BusinessException, RemoteAuthenticationException {
        log.debug("export module {} to {}", module, meveoInstance);
        final String url = "api/rest/module/createOrUpdate";

        try {
            ApiService<MeveoModuleDto> moduleApi = (ApiService<MeveoModuleDto>) EjbUtils.getServiceInterface("ModuleApi");
            MeveoModuleDto moduleDto = moduleApi.find(module.getCode(), currentUser);
            log.debug("Export module dto {}", moduleDto);
            Response response = meveoInstanceService.publishDto2MeveoInstance(url, meveoInstance, moduleDto);
            ActionStatus actionStatus = response.readEntity(ActionStatus.class);
            log.debug("response {}", actionStatus);
            if (actionStatus == null || ActionStatusEnum.SUCCESS != actionStatus.getStatus()) {
                throw new BusinessException("Code " + actionStatus.getErrorCode() + ", info " + actionStatus.getMessage());
            }
        } catch (Exception e) {
            log.error("Error when export module {} to {}. Reason {}", module.getCode(), meveoInstance.getCode(),
                (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            throw new BusinessException("Fail to communicate " + meveoInstance.getCode() + ". Error " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    // /**
    // * Convert MeveoModule or its subclass object to DTO representation
    // *
    // * @param module Module object
    // * @param provider Provider
    // * @return MeveoModuleDto object
    // */
    // public ModuleDto moduleToDto(MeveoModule module, Provider provider) throws BusinessException {
    //
    // if (module.isDownloaded() && !module.isInstalled()) {
    // try {
    // return MeveoModuleService.moduleSourceToDto(module);
    // } catch (Exception e) {
    // log.error("Failed to load module source {}", module.getCode(), e);
    // throw new BusinessException("Failed to load module source", e);
    // }
    // }
    //
    // Class<? extends ModuleDto> dtoClass = ModuleDto.class;
    // if (module instanceof BusinessServiceModel) {
    // dtoClass = BusinessServiceModelDto.class;
    // } else if (module instanceof BusinessOfferModel) {
    // dtoClass = BusinessOfferModelDto.class;
    // } else if (module instanceof BusinessAccountModel) {
    // dtoClass = BusinessAccountModelDto.class;
    // }
    //
    // ModuleDto moduleDto = null;
    // try {
    // moduleDto = dtoClass.getConstructor(MeveoModule.class).newInstance(module);
    // } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
    // log.error("Failed to instantiate Module Dto. No reason for it to happen. ", e);
    // throw new RuntimeException("Failed to instantiate Module Dto. No reason for it to happen. ", e);
    // }
    //
    // if (StringUtils.isNotBlank(module.getLogoPicture())) {
    // try {
    // moduleDto.setLogoPictureFile(ModuleUtil.readModulePicture(module.getProvider().getCode(), module.getLogoPicture()));
    // } catch (Exception e) {
    // log.error("Failed to read module files {}, info {}", module.getLogoPicture(), e.getMessage(), e);
    // }
    // }
    //
    // List<MeveoModuleItem> moduleItems = module.getModuleItems();
    // if (moduleItems != null) {
    // for (MeveoModuleItem item : moduleItems) {
    // loadModuleItem(item, provider);
    //
    // if (item.getItemEntity() == null) {
    // continue;
    // }
    // if (item.getItemEntity() instanceof CustomEntityTemplate) {
    // CustomEntityTemplate customEntityTemplate = (CustomEntityTemplate) item.getItemEntity();
    // Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(customEntityTemplate.getAppliesTo(), module.getProvider());
    // Map<String, EntityCustomAction> customActions = entityActionScriptService.findByAppliesTo(customEntityTemplate.getAppliesTo(), module.getProvider());
    //
    // CustomEntityTemplateDto dto = CustomEntityTemplateDto.toDTO(customEntityTemplate, customFieldTemplates != null ? customFieldTemplates.values() : null,
    // customActions != null ? customActions.values() : null);
    // moduleDto.addModuleItem(dto);
    //
    // } else if (item.getItemEntity() instanceof CustomFieldTemplate) {
    // moduleDto.addModuleItem(new CustomFieldTemplateDto((CustomFieldTemplate) item.getItemEntity()));
    //
    // } else if (item.getItemEntity() instanceof Filter) {
    // moduleDto.addModuleItem(FilterDto.toDto((Filter) item.getItemEntity()));
    //
    // } else if (item.getItemEntity() instanceof JobInstance) {
    // exportJobInstance((JobInstance) item.getItemEntity(), moduleDto);
    //
    // } else if (item.getItemEntity() instanceof Notification) {
    //
    // Notification notification = (Notification) item.getItemEntity();
    // if (notification.getScriptInstance() != null) {
    // moduleDto.addModuleItem(new ScriptInstanceDto(notification.getScriptInstance()));
    // }
    //
    // if (notification.getCounterTemplate() != null) {
    // moduleDto.addModuleItem(new CounterTemplateDto(notification.getCounterTemplate()));
    // }
    //
    // if (notification instanceof EmailNotification) {
    // moduleDto.addModuleItem(new EmailNotificationDto((EmailNotification) notification));
    //
    // } else if (notification instanceof JobTrigger) {
    // exportJobInstance(((JobTrigger) notification).getJobInstance(), moduleDto);
    // moduleDto.addModuleItem(new JobTriggerDto((JobTrigger) notification));
    //
    // } else if (notification instanceof ScriptNotification) {
    // moduleDto.addModuleItem(new NotificationDto(notification));
    //
    // } else if (notification instanceof WebHook) {
    // moduleDto.addModuleItem(new WebhookNotificationDto((WebHook) notification));
    // }
    //
    // } else if (item.getItemEntity() instanceof ScriptInstance) {
    // moduleDto.addModuleItem(new ScriptInstanceDto((ScriptInstance) item.getItemEntity()));
    //
    // } else if (item.getItemEntity() instanceof PieChart) {
    // moduleDto.addModuleItem(new PieChartDto((PieChart) item.getItemEntity()));
    //
    // } else if (item.getItemEntity() instanceof LineChart) {
    // moduleDto.addModuleItem(new LineChartDto((LineChart) item.getItemEntity()));
    //
    // } else if (item.getItemEntity() instanceof BarChart) {
    // moduleDto.addModuleItem(new BarChartDto((BarChart) item.getItemEntity()));
    //
    // } else if (item.getItemEntity() instanceof MeasurableQuantity) {
    // moduleDto.addModuleItem(new MeasurableQuantityDto((MeasurableQuantity) item.getItemEntity()));
    //
    // } else if (item.getItemEntity() instanceof MeveoModule) {
    // moduleDto.addModuleItem(moduleToDto((MeveoModule) item.getItemEntity(), provider));
    //
    // }
    // }
    // }
    //
    // // Finish converting subclasses of MeveoModule class
    // if (module instanceof BusinessServiceModel) {
    // businessServiceModelToDto((BusinessServiceModel) module, (BusinessServiceModelDto) moduleDto, provider);
    //
    // } else if (module instanceof BusinessOfferModel) {
    // businessOfferModelToDto((BusinessOfferModel) module, (BusinessOfferModelDto) moduleDto, provider);
    //
    // } else if (module instanceof BusinessAccountModel) {
    // businessAccountModelToDto((BusinessAccountModel) module, (BusinessAccountModelDto) moduleDto, provider);
    // }
    //
    // return moduleDto;
    // }

    // /**
    // * export jobInstance to remote meveo instance
    // *
    // * @param meveoInstance
    // * @param jobInstance
    // * @return
    // * @throws MeveoApiException
    // */
    // private void exportJobInstance(JobInstance jobInstance, ModuleDto moduleDto) {
    // JobInstance nextJobInstance = jobInstance.getFollowingJob();
    // if (nextJobInstance != null) {
    // exportJobInstance(nextJobInstance, moduleDto);
    // }
    //
    // if (jobInstance.getTimerEntity() != null) {
    // TimerEntity timerEntity = jobInstance.getTimerEntity();
    // if (timerEntity != null) {
    // TimerEntityDto timerDto = new TimerEntityDto(timerEntity);
    // moduleDto.addModuleItem(timerDto);
    // }
    // }
    // JobInstanceDto dto = new JobInstanceDto(jobInstance, entityToDtoConverter.getCustomFieldsDTO(jobInstance));
    // moduleDto.addModuleItem(dto);
    // }

    public void loadModuleItem(MeveoModuleItem item, Provider provider) {

        BusinessEntity entity = null;
        if (CustomFieldTemplate.class.getName().equals(item.getItemClass())) {
            entity = customFieldTemplateService.findByCodeAndAppliesTo(item.getItemCode(), item.getAppliesTo(), provider);

        } else {

            String sql = "select mi from " + item.getItemClass() + " mi where mi.code=:code and mi.provider=:provider";
            TypedQuery<BusinessEntity> query = getEntityManager().createQuery(sql, BusinessEntity.class);
            query.setParameter("code", item.getItemCode());
            query.setParameter("provider", provider);
            try {
                entity = query.getSingleResult();

            } catch (NoResultException | NonUniqueResultException e) {
                log.error("Failed to find a module item {}. Reason: {}", item, e.getClass().getSimpleName());
                return;
            } catch (Exception e) {
                log.error("Failed to find a module item {}", item, e);
                return;
            }
        }
        item.setItemEntity(entity);

    }

    @SuppressWarnings("unchecked")
    public List<MeveoModuleItem> findByCodeAndItemType(String code, String className) {
        QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "m");
        qb.addCriterion("itemCode", "=", code, true);
        qb.addCriterion("itemClass", "=", className, true);

        try {
            return (List<MeveoModuleItem>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static MeveoModuleDto moduleSourceToDto(MeveoModule module) throws JAXBException {
        Class<? extends MeveoModuleDto> dtoClass = MeveoModuleDto.class;
        if (module instanceof BusinessServiceModel) {
            dtoClass = BusinessServiceModelDto.class;
        } else if (module instanceof BusinessOfferModel) {
            dtoClass = BusinessOfferModelDto.class;
        } else if (module instanceof BusinessAccountModel) {
            dtoClass = BusinessAccountModelDto.class;
        }

        MeveoModuleDto moduleDto = (MeveoModuleDto) JAXBContext.newInstance(dtoClass).createUnmarshaller().unmarshal(new StringReader(module.getModuleSource()));

        return moduleDto;
    }

    public MeveoModule uninstall(MeveoModule module, User currentUser) throws BusinessException {
        return uninstall(module, currentUser, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private MeveoModule uninstall(MeveoModule module, User currentUser, boolean childModule) throws BusinessException {

        if (!module.isInstalled()) {
            throw new BusinessException("Module is not installed");
        }

        if (module.getScript() != null) {
            moduleScriptService.preUninstallModule(module.getScript().getCode(), module, currentUser);
        }

        if (module instanceof BusinessServiceModel) {
            serviceTemplateService.disable(((BusinessServiceModel) module).getServiceTemplate(), currentUser);
        } else if (module instanceof BusinessOfferModel) {
            offerTemplateService.disable(((BusinessOfferModel) module).getOfferTemplate(), currentUser);
        }

        for (MeveoModuleItem item : module.getModuleItems()) {
            loadModuleItem(item, currentUser.getProvider());
            BusinessEntity itemEntity = item.getItemEntity();
            if (itemEntity == null) {
                continue;
            }

            try {
                if (itemEntity instanceof MeveoModule) {
                    uninstall((MeveoModule) itemEntity, currentUser, true);
                } else {

                    // Find API service class first trying with item's classname and then with its super class (a simplified version instead of trying various class
                    // superclasses)
                    Class clazz = Class.forName(item.getItemClass());
                    PersistenceService persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSimpleName() + "Service");
                    if (persistenceServiceForItem == null) {
                        persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSuperclass().getSimpleName() + "Service");
                    }
                    if (persistenceServiceForItem == null) {
                        log.error("Failed to find implementation of persistence service for class {}", item.getItemClass());
                        continue;
                    }

                    persistenceServiceForItem.disable(itemEntity, currentUser);

                }
            } catch (Exception e) {
                log.error("Failed to uninstall/disable module item. Module item {}", item, e);
            }
        }

        if (module.getScript() != null) {
            moduleScriptService.postUninstallModule(module.getScript().getCode(), module, currentUser);
        }

        // Remove if it is a child module
        if (childModule) {
            remove(module);
            return null;

            // Otherwise mark it uninstalled and clear module items
        } else {
            module.setInstalled(false);
            module.getModuleItems().clear();
            return update(module, currentUser);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MeveoModule disable(MeveoModule module, User currentUser) throws BusinessException {

        // if module is local module (was not downloaded) just disable as any other entity without iterating module items
        if (!module.isDownloaded()) {
            return super.disable(module, currentUser);
        }

        if (!module.isInstalled()) {
            // throw new BusinessException("Module is not installed");
            return module;
        }

        if (module.getScript() != null) {
            moduleScriptService.preDisableModule(module.getScript().getCode(), module, currentUser);
        }

        if (module instanceof BusinessServiceModel) {
            serviceTemplateService.disable(((BusinessServiceModel) module).getServiceTemplate(), currentUser);
        } else if (module instanceof BusinessOfferModel) {
            offerTemplateService.disable(((BusinessOfferModel) module).getOfferTemplate(), currentUser);
        }

        for (MeveoModuleItem item : module.getModuleItems()) {
            loadModuleItem(item, currentUser.getProvider());
            BusinessEntity itemEntity = item.getItemEntity();
            if (itemEntity == null) {
                continue;
            }

            try {
                // Find API service class first trying with item's classname and then with its super class (a simplified version instead of trying various class
                // superclasses)
                Class clazz = Class.forName(item.getItemClass());
                PersistenceService persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSimpleName() + "Service");
                if (persistenceServiceForItem == null) {
                    persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSuperclass().getSimpleName() + "Service");
                }
                if (persistenceServiceForItem == null) {
                    log.error("Failed to find implementation of persistence service for class {}", item.getItemClass());
                    continue;
                }

                persistenceServiceForItem.disable(itemEntity, currentUser);

            } catch (Exception e) {
                log.error("Failed to disable module item. Module item {}", item, e);
            }
        }

        if (module.getScript() != null) {
            moduleScriptService.postDisableModule(module.getScript().getCode(), module, currentUser);
        }

        return super.disable(module, currentUser);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MeveoModule enable(MeveoModule module, User currentUser) throws BusinessException {

        // if module is local module (was not downloaded) just disable as any other entity without iterating module items
        if (!module.isDownloaded()) {
            return super.enable(module, currentUser);
        }

        if (!module.isInstalled()) {
            // throw new BusinessException("Module is not installed");
            return module;
        }

        if (module.getScript() != null) {
            moduleScriptService.preEnableModule(module.getScript().getCode(), module, currentUser);
        }

        if (module instanceof BusinessServiceModel) {
            serviceTemplateService.enable(((BusinessServiceModel) module).getServiceTemplate(), currentUser);
        } else if (module instanceof BusinessOfferModel) {
            offerTemplateService.enable(((BusinessOfferModel) module).getOfferTemplate(), currentUser);
        }

        for (MeveoModuleItem item : module.getModuleItems()) {
            loadModuleItem(item, currentUser.getProvider());
            BusinessEntity itemEntity = item.getItemEntity();
            if (itemEntity == null) {
                continue;
            }

            try {
                // Find API service class first trying with item's classname and then with its super class (a simplified version instead of trying various class
                // superclasses)
                Class clazz = Class.forName(item.getItemClass());
                PersistenceService persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSimpleName() + "Service");
                if (persistenceServiceForItem == null) {
                    persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSuperclass().getSimpleName() + "Service");
                }
                if (persistenceServiceForItem == null) {
                    log.error("Failed to find implementation of persistence service for class {}", item.getItemClass());
                    continue;
                }

                persistenceServiceForItem.enable(itemEntity, currentUser);

            } catch (Exception e) {
                log.error("Failed to enable module item. Module item {}", item, e);
            }
        }

        if (module.getScript() != null) {
            moduleScriptService.postEnableModule(module.getScript().getCode(), module, currentUser);
        }

        return super.enable(module, currentUser);
    }

    @Override
    public void remove(MeveoModule module) {

        // If module was downloaded, remove all submodules as well
        if (module.isDownloaded() && module.getModuleItems() != null) {

            for (MeveoModuleItem item : module.getModuleItems()) {
                try {
                    if (MeveoModule.class.isAssignableFrom(Class.forName(item.getItemClass()))) {
                        loadModuleItem(item, module.getProvider());
                        MeveoModule itemModule = (MeveoModule) item.getItemEntity();
                        remove(itemModule);
                    }
                } catch (Exception e) {
                    log.error("Failed to delete a submodule", e);
                }
            }
        }

        super.remove(module);
    }

    @SuppressWarnings("unchecked")
    public String getRelatedModulesAsString(String itemCode, String itemClazz, String appliesTo, Provider provider) {
        QueryBuilder qb = new QueryBuilder(MeveoModule.class, "m", Arrays.asList("moduleItems as i"), provider);
        qb.addCriterion("i.itemCode", "=", itemCode, true);
        qb.addCriterion("i.itemClass", "=", itemClazz, false);
        qb.addCriterion("i.appliesTo", "=", appliesTo, false);
        List<MeveoModule> modules = qb.getQuery(getEntityManager()).getResultList();

        if (modules != null) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (MeveoModule module : modules) {
                if (i != 0) {
                    sb.append(";");
                }
                sb.append(module.getCode());
                i++;
            }
            return sb.toString();
        }
        return null;
    }
}
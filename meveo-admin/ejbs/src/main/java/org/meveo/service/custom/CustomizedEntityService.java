package org.meveo.service.custom;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.meveo.commons.utils.StringUtils;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.crm.Provider;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.jobs.JobInstance;
import org.meveo.service.job.Job;
import org.meveo.service.job.JobInstanceService;
import org.meveo.util.EntityCustomizationUtils;
import org.reflections.Reflections;

public class CustomizedEntityService implements Serializable {

    private static final long serialVersionUID = 4108034108745598588L;

    @Inject
    private JobInstanceService jobInstanceService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    /**
     * Get a list of customized/customizable entities optionally filtering by a name and custom entities only
     * 
     * @param entityName Optional filter by a name
     * @param customEntityTemplatesOnly Return custom entity templates only
     * @param sortBy Sort by. Valid values are: "description" or null to sort by entity name
     * @param sortOrder Sort order. Valid values are "DESCENDING" or "ASCENDING". By default will sort in Ascending order.
     * @param currentProvider Current provider
     * @return A list of customized/customizable entities
     */
    public List<CustomizedEntity> getCustomizedEntities(String entityName, boolean customEntityTemplatesOnly, final String sortBy, final String sortOrder, Provider currentProvider) {
        List<CustomizedEntity> entities = new ArrayList<>();

        if (entityName != null) {
            entityName = entityName.toLowerCase();
        }

        if (!customEntityTemplatesOnly) {
            entities.addAll(searchAllCustomFieldEntities(entityName, false));
            entities.addAll(searchJobs(entityName));
        }
        entities.addAll(searchCustomEntityTemplates(entityName, currentProvider));
        Collections.sort(entities, sortEntitiesBy(sortBy, sortOrder));
        return entities;
    }

    /**
     * Searches all custom entities that can be manually managed from the Custom Entities page.
     *
     * @param entityName Optional filter by a name
     * @param sortBy Sort by. Valid values are: "description" or null to sort by entity name
     * @param sortOrder Sort order. Valid values are "DESCENDING" or "ASCENDING". By default will sort in Ascending order.
     * @param currentProvider Current provider
     * @return A list of customized/customizable entities excluding any entity that is set not to be manually managed.
     */
    public List<CustomizedEntity> searchManagedCustomEntities(final String entityName, final String sortBy, final String sortOrder, Provider currentProvider) {
        List<CustomizedEntity> entities = new ArrayList<>();

        entities.addAll(searchAllCustomFieldEntities(entityName, false));
        entities.addAll(searchJobs(entityName));
        entities.addAll(searchCustomEntityTemplates(entityName, currentProvider));
        Collections.sort(entities, sortEntitiesBy(sortBy, sortOrder));

        return entities;
    }

    /**
     * List all custom entities including custom entities, jobs, and custom entity templates.
     *
     * @param sortBy Sort by. Valid values are: "description" or null to sort by entity name
     * @param sortOrder Sort order. Valid values are "DESCENDING" or "ASCENDING". By default will sort in Ascending order.
     * @param currentProvider Current provider
     * @return A list of customized/customizable entities.
     */
    public List<CustomizedEntity> listAllCustomEntities(final String sortBy, final String sortOrder, Provider currentProvider) {
        List<CustomizedEntity> entities = new ArrayList<>();

        entities.addAll(searchAllCustomFieldEntities(null, true));
        entities.addAll(searchJobs(null));
        entities.addAll(searchCustomEntityTemplates(null, currentProvider));
        Collections.sort(entities, sortEntitiesBy(sortBy, sortOrder));

        return entities;
    }

    /**
     * Searches all custom entities including custom entities, jobs, and custom entity templates.
     *
     * @param entityName Optional filter by a name
     * @param sortBy Sort by. Valid values are: "description" or null to sort by entity name
     * @param sortOrder Sort order. Valid values are "DESCENDING" or "ASCENDING". By default will sort in Ascending order.
     * @param currentProvider Current provider
     * @return A list of customized/customizable entities.
     */
    public List<CustomizedEntity> searchAllCustomEntities(final String entityName, final String sortBy, final String sortOrder, Provider currentProvider) {
        List<CustomizedEntity> entities = new ArrayList<>();

        entities.addAll(searchAllCustomFieldEntities(entityName, true));
        entities.addAll(searchJobs(entityName));
        entities.addAll(searchCustomEntityTemplates(entityName, currentProvider));
        Collections.sort(entities, sortEntitiesBy(sortBy, sortOrder));

        return entities;
    }

    /**
     * Searches all custom entities comprised of only custom entity templates.
     *
     * @param entityName Optional filter by a name
     * @param sortBy Sort by. Valid values are: "description" or null to sort by entity name
     * @param sortOrder Sort order. Valid values are "DESCENDING" or "ASCENDING". By default will sort in Ascending order.
     * @param currentProvider Current provider
     * @return A list of customized/customizable entities.
     */
    public List<CustomizedEntity> searchCustomEntityTemplates(final String entityName, final String sortBy, final String sortOrder, Provider currentProvider) {
        List<CustomizedEntity> entities = new ArrayList<>();

        entities.addAll(searchCustomEntityTemplates(entityName, currentProvider));
        Collections.sort(entities, sortEntitiesBy(sortBy, sortOrder));

        return entities;
    }

    /**
     * Searches all custom field entities.
     *
     * @param entityName Optional filter by a name
     * @param includeNonManagedEntities If true, will include all entries including those set not to appear in the Custom Entities list.
     * @return A list of customized/customizable entities.
     */
    private List<CustomizedEntity> searchAllCustomFieldEntities(final String entityName, final boolean includeNonManagedEntities) {
        List<CustomizedEntity> entities = new ArrayList<>();
        Reflections reflections = new Reflections("org.meveo.model");
        Set<Class<? extends ICustomFieldEntity>> cfClasses = reflections.getSubTypesOf(ICustomFieldEntity.class);

        // Find standard entities that implement ICustomFieldEntity interface except JobInstance
        CustomFieldEntity annotation = null;
        for (Class<? extends ICustomFieldEntity> cfClass : cfClasses) {

            annotation = cfClass.getAnnotation(CustomFieldEntity.class);
            boolean isSkipped = JobInstance.class.isAssignableFrom(cfClass)
                || Modifier.isAbstract(cfClass.getModifiers())
                || (entityName != null && !cfClass.getSimpleName().toLowerCase().contains(entityName.toLowerCase()))
                || (!includeNonManagedEntities && !annotation.isManuallyManaged());

            if(isSkipped){
                continue;
            }

            entities.add(new CustomizedEntity(cfClass));
        }
        return entities;
    }

    /**
     * Searches all custom entity templates.
     *
     * @param entityName Optional filter by a name
     * @param currentProvider Current provider
     * @return A list of custom entity templates.
     */
    private List<CustomizedEntity> searchCustomEntityTemplates(String entityName, Provider currentProvider) {
        List<CustomizedEntity> entities = new ArrayList<>();
        List<CustomEntityTemplate> customEntityTemplates = null;
        if (entityName == null || CustomEntityTemplate.class.getSimpleName().toLowerCase().contains(entityName)) {
            customEntityTemplates = customEntityTemplateService.list(currentProvider);
        } else if (entityName != null) {
            customEntityTemplates = customEntityTemplateService.findByCodeLike(entityName, currentProvider);
        }

        for (CustomEntityTemplate customEntityTemplate : customEntityTemplates) {
            entities.add(new CustomizedEntity(customEntityTemplate.getCode(), CustomEntityTemplate.class, customEntityTemplate.getId(), customEntityTemplate.getDescription()));
        }
        return entities;
    }

    /**
     * Searches all jobs.
     *
     * @param entityName Optional filter by a name
     * @return A list of jobs.
     */
    private List<CustomizedEntity> searchJobs(String entityName) {
        List<CustomizedEntity> jobs = new ArrayList<>();
        for (Job job : jobInstanceService.getJobs()) {
            if (job.getCustomFields() != null && (entityName == null || (entityName != null && job.getClass().getSimpleName().toLowerCase().contains(entityName)))) {
                jobs.add(new CustomizedEntity(job.getClass()));
            }
        }
        return jobs;
    }

    /**
     * The comparator used to sort customized entities.
     * @param sortBy Sort by. Valid values are: "description" or null to sort by entity name
     * @param sortOrder Sort order. Valid values are "DESCENDING" or "ASCENDING". By default will sort in Ascending order.
     * @return The customized entity comparator instance.
     */
    private Comparator<CustomizedEntity> sortEntitiesBy(final String sortBy, final String sortOrder) {
        return new Comparator<CustomizedEntity>() {

            @Override
            public int compare(CustomizedEntity o1, CustomizedEntity o2) {
                int order = 1;
                if ("DESCENDING".equalsIgnoreCase(sortOrder)) {
                    order = -1;
                }
                if ("description".equals(sortBy)) {
                    return StringUtils.compare(o1.getDescription(), o2.getDescription()) * order;

                } else {
                    return StringUtils.compare(o1.getEntityName(), o2.getEntityName()) * order;
                }
            }

        };
    }

    /**
     * Get a customized/customizable entity that matched a given appliesTo value as it is used in customFieldtemplate or EntityActionScript
     * 
     * @param appliesTo appliesTo value as it is used in customFieldtemplate or EntityActionScript
     * @param currentProvider Current provider
     * @return A customized/customizable entity
     */
    public CustomizedEntity getCustomizedEntity(String appliesTo, Provider currentProvider) {

        // Find standard entities that implement ICustomFieldEntity interface except JobInstance
        Reflections reflections = new Reflections("org.meveo.model");
        Set<Class<? extends ICustomFieldEntity>> cfClasses = reflections.getSubTypesOf(ICustomFieldEntity.class);

        for (Class<? extends ICustomFieldEntity> cfClass : cfClasses) {

            if (JobInstance.class.isAssignableFrom(cfClass) || Modifier.isAbstract(cfClass.getModifiers())) {
                continue;
            }

            if (appliesTo.equals(EntityCustomizationUtils.getAppliesTo(cfClass, null))) {
                return new CustomizedEntity(cfClass);
            }
        }

        // Find Jobs
        for (Job job : jobInstanceService.getJobs()) {
            if (appliesTo.equals(EntityCustomizationUtils.getAppliesTo(job.getClass(), null))) {
                return new CustomizedEntity(job.getClass());
            }
        }

        for (CustomEntityTemplate cet : customEntityTemplateService.list(currentProvider)) {
            if (appliesTo.equals(cet.getAppliesTo())) {
                return new CustomizedEntity(cet.getCode(), CustomEntityTemplate.class, cet.getId(), cet.getDescription());
            }
        }
        return null;
    }
}
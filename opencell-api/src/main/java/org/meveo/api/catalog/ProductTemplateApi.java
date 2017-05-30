package org.meveo.api.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.catalog.ProductChargeTemplateDto;
import org.meveo.api.dto.catalog.ProductTemplateDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidImageData;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.DatePeriod;
import org.meveo.model.catalog.OfferTemplate;
import org.meveo.model.catalog.ProductTemplate;
import org.meveo.service.catalog.impl.ProductTemplateService;

@Stateless
public class ProductTemplateApi extends ProductOfferingApi<ProductTemplate, ProductTemplateDto> {

    @Inject
    private ProductTemplateService productTemplateService;

    /*
     * (non-Javadoc)
     * 
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
    @Override
    public ProductTemplateDto find(String code, Date validFrom, Date validTo)
            throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("productTemplate code");
            handleMissingParameters();
        }

        ProductTemplate productTemplate = productTemplateService.findByCodeBestValidityMatch(code, validFrom, validTo);
        if (productTemplate == null) {
            throw new EntityDoesNotExistsException(ProductTemplate.class, code + " / " + validFrom + " / " + validTo);
        }

        ProductTemplateDto productTemplateDto = new ProductTemplateDto(productTemplate, entityToDtoConverter.getCustomFieldsDTO(productTemplate), false);

        processProductChargeTemplateToDto(productTemplate, productTemplateDto);

        return productTemplateDto;
    }

    public ProductTemplate createOrUpdate(ProductTemplateDto productTemplateDto) throws MeveoApiException, BusinessException {
        ProductTemplate productTemplate = productTemplateService.findByCode(productTemplateDto.getCode(), productTemplateDto.getValidFrom(), productTemplateDto.getValidTo());

        if (productTemplate == null) {
            return create(productTemplateDto);
        } else {
            return update(productTemplateDto);
        }
    }

    public ProductTemplate create(ProductTemplateDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getName())) {
            missingParameters.add("name");
        }

        if (postData.getProductChargeTemplates() != null) {
            List<ProductChargeTemplateDto> productChargeTemplateDtos = postData.getProductChargeTemplates();
            for (ProductChargeTemplateDto productChargeTemplateDto : productChargeTemplateDtos) {
                if (productChargeTemplateDto == null || StringUtils.isBlank(productChargeTemplateDto.getCode())) {
                    missingParameters.add("productChargeTemplate");
                }
            }
        } else {
            missingParameters.add("productChargeTemplates");
        }

        handleMissingParameters();

        if (productTemplateService.findByCode(postData.getCode(), postData.getValidFrom(), postData.getValidTo()) != null) {
            throw new EntityAlreadyExistsException(ProductTemplate.class, postData.getCode() + " / " + postData.getValidFrom() + " / " + postData.getValidTo());
        }

        ProductTemplate productTemplate = new ProductTemplate();
        productTemplate.setCode(postData.getCode());
        productTemplate.setDescription(postData.getDescription());
        productTemplate.setName(postData.getName());
        productTemplate.setValidity(new DatePeriod(postData.getValidFrom(), postData.getValidTo()));
        productTemplate.setLifeCycleStatus(postData.getLifeCycleStatus());
        try {
            saveImage(productTemplate, postData.getImagePath(), postData.getImageBase64());
        } catch (IOException e1) {
            log.error("Invalid image data={}", e1.getMessage());
            throw new InvalidImageData();
        }

        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), productTemplate, false);
        } catch (MissingParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        // save product template now so that they can be referenced by the
        // related entities below.
        productTemplateService.create(productTemplate);

        if (postData.getProductChargeTemplates() != null) {
            processProductChargeTemplate(postData, productTemplate);
        }
        if (postData.getAttachments() != null) {
            processDigitalResources(postData, productTemplate);
        }
        if (postData.getOfferTemplateCategories() != null) {
            processOfferTemplateCategories(postData, productTemplate);
        }

        productTemplateService.update(productTemplate);

        return productTemplate;
    }

    public ProductTemplate update(ProductTemplateDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getName())) {
            missingParameters.add("name");
        }
        handleMissingParameters();

        ProductTemplate productTemplate = productTemplateService.findByCode(postData.getCode(), postData.getValidFrom(), postData.getValidTo());
        if (productTemplate == null) {
            throw new EntityDoesNotExistsException(OfferTemplate.class, postData.getCode() + " / " + postData.getValidFrom() + " / " + postData.getValidTo());
        }

        productTemplate.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());
        productTemplate.setDescription(postData.getDescription());
        productTemplate.setName(postData.getName());
        productTemplate.setValidity(new DatePeriod(postData.getValidFrom(), postData.getValidTo()));
        productTemplate.setLifeCycleStatus(postData.getLifeCycleStatus());
        try {
            saveImage(productTemplate, postData.getImagePath(), postData.getImageBase64());
        } catch (IOException e1) {
            log.error("Invalid image data={}", e1.getMessage());
            throw new InvalidImageData();
        }

        if (postData.getProductChargeTemplates() != null) {
            processProductChargeTemplate(postData, productTemplate);
        }
        if (postData.getOfferTemplateCategories() != null) {
            processOfferTemplateCategories(postData, productTemplate);
        }
        if (postData.getAttachments() != null) {
            processDigitalResources(postData, productTemplate);
        }

        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), productTemplate, false);
        } catch (MissingParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        productTemplate = productTemplateService.update(productTemplate);

        return productTemplate;
    }

    public void remove(String code, Date validFrom, Date validTo) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("productTemplate code");
            handleMissingParameters();
        }

        ProductTemplate productTemplate = productTemplateService.findByCodeBestValidityMatch(code, validFrom, validTo);
        if (productTemplate == null) {
            throw new EntityDoesNotExistsException(ProductTemplate.class, code + " / " + validFrom + " / " + validTo);
        }

        // deleteImage(productTemplate);
        productTemplateService.remove(productTemplate);
    }

    public List<ProductTemplateDto> list() {
        List<ProductTemplate> listProductTemplate = productTemplateService.list();
        List<ProductTemplateDto> dtos = new ArrayList<ProductTemplateDto>();
        if (listProductTemplate != null) {
            for (ProductTemplate productTemplate : listProductTemplate) {
                dtos.add(new ProductTemplateDto(productTemplate, null, false));
            }
        }
        return dtos;
    }

}

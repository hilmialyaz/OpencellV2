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
package org.meveo.service.catalog.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.IteratorUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.RatingCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.Auditable;
import org.meveo.model.admin.Seller;
import org.meveo.model.admin.User;
import org.meveo.model.billing.TradingCountry;
import org.meveo.model.billing.TradingCurrency;
import org.meveo.model.catalog.Calendar;
import org.meveo.model.catalog.ChargeTemplate;
import org.meveo.model.catalog.OfferTemplate;
import org.meveo.model.catalog.PricePlanMatrix;
import org.meveo.model.crm.Provider;
import org.meveo.service.base.MultilanguageEntityService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;

@Stateless
public class PricePlanMatrixService extends MultilanguageEntityService<PricePlanMatrix> {

	@Inject
	private RatingCacheContainerProvider ratingCacheContainerProvider;
	
	@Inject
    private CustomFieldInstanceService customFieldInstanceService;

	ParamBean param = ParamBean.getInstance();

	SimpleDateFormat sdf = new SimpleDateFormat(param.getProperty("excelImport.dateFormat", "dd/MM/yyyy"));

	@Override
	public void create(PricePlanMatrix pricePlan, User creator) throws BusinessException {
		super.create(pricePlan, creator);
		ratingCacheContainerProvider.addPricePlanToCache(pricePlan);
	}

	@Override
	public PricePlanMatrix disable(PricePlanMatrix pricePlan, User currentUser) throws BusinessException {
		pricePlan = super.disable(pricePlan, currentUser);
		ratingCacheContainerProvider.removePricePlanFromCache(pricePlan);
		return pricePlan;
	}

	@Override
	public PricePlanMatrix enable(PricePlanMatrix pricePlan, User currentUser) throws BusinessException {
		pricePlan = super.enable(pricePlan, currentUser);
		ratingCacheContainerProvider.addPricePlanToCache(pricePlan);
		return pricePlan;
	}

	@Override
	public void remove(PricePlanMatrix pricePlan) {
		super.remove(pricePlan);
		ratingCacheContainerProvider.removePricePlanFromCache(pricePlan);
	}

	@Override
	public PricePlanMatrix update(PricePlanMatrix pricePlan, User updater) throws BusinessException {
		pricePlan = super.update(pricePlan, updater);
		ratingCacheContainerProvider.updatePricePlanInCache(pricePlan);
		return pricePlan;
	}

	@SuppressWarnings("unchecked")
	public void removeByPrefix(EntityManager em, String prefix, Provider provider) {
		Query query = em.createQuery(
				"select m from PricePlanMatrix m WHERE m.eventCode LIKE '" + prefix + "%' AND m.provider=:provider");
		query.setParameter("provider", provider);
		List<PricePlanMatrix> pricePlans = query.getResultList();
		for (PricePlanMatrix pricePlan : pricePlans) {
			remove(pricePlan);
		}
	}

	@SuppressWarnings("unchecked")
	public void removeByCode(EntityManager em, String code, Provider provider) {
		Query query = em.createQuery("select m PricePlanMatrix m WHERE m.eventCode=:code AND m.provider=:provider");
		query.setParameter("code", code);
		query.setParameter("provider", provider);
		List<PricePlanMatrix> pricePlans = query.getResultList();
		for (PricePlanMatrix pricePlan : pricePlans) {
			remove(pricePlan);
		}
	}

	private String getCellAsString(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue() + "";
		case Cell.CELL_TYPE_ERROR:
		case Cell.CELL_TYPE_BLANK:
		case Cell.CELL_TYPE_FORMULA:
			return null;
		case Cell.CELL_TYPE_NUMERIC:
			return "" + cell.getNumericCellValue();
		default:
			return cell.getStringCellValue();
		}
	}

	private Date getCellAsDate(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_ERROR:
		case Cell.CELL_TYPE_BLANK:
		case Cell.CELL_TYPE_FORMULA:
			return null;
		case Cell.CELL_TYPE_NUMERIC:
			return DateUtil.getJavaDate(cell.getNumericCellValue());
		default:
			try {
				return cell.getDateCellValue();
			} catch (Exception e) {
				try {
					return sdf.parse(cell.getStringCellValue());
				} catch (ParseException e1) {
					return null;
				}
			}
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void importExcelLine(Row row, User user, Provider provider) throws BusinessException {
		EntityManager em = getEntityManager();
		Object[] cellsObj = IteratorUtils.toArray(row.cellIterator());
		int rowIndex = row.getRowNum();
		int i = 0;
		String pricePlanCode = getCellAsString((Cell) cellsObj[i++]);

		PricePlanMatrix pricePlan = null;
		QueryBuilder qb = new QueryBuilder(PricePlanMatrix.class, "p");
		qb.addCriterion("code", "=", pricePlanCode, false);
		qb.addCriterionEntity("provider", provider);
		@SuppressWarnings("unchecked")
		List<PricePlanMatrix> pricePlans = qb.getQuery(em).getResultList();

		if (pricePlans == null || pricePlans.size() == 0) {
			pricePlan = new PricePlanMatrix();
			pricePlan.setProvider(provider);
			pricePlan.setAuditable(new Auditable());
			pricePlan.getAuditable().setCreated(new Date());
			pricePlan.getAuditable().setCreator(user);
		} else if (pricePlans.size() == 1) {
			pricePlan = pricePlans.get(0);
		} else {
			throw new BusinessException("More than one priceplan in line=" + rowIndex + "with code=" + pricePlanCode);
		}

		String pricePlanDescription = getCellAsString((Cell) cellsObj[i++]);
		String eventCode = getCellAsString((Cell) cellsObj[i++]);
		String sellerCode = getCellAsString((Cell) cellsObj[i++]);
		String countryCode = getCellAsString((Cell) cellsObj[i++]);
		String currencyCode = getCellAsString((Cell) cellsObj[i++]);
		try {
			pricePlan.setStartSubscriptionDate(getCellAsDate((Cell) cellsObj[i++]));
		} catch (Exception e) {
			throw new BusinessException("Invalid startAppli in line=" + rowIndex + " expected format:"
					+ param.getProperty("excelImport.dateFormat", "dd/MM/yyyy")
					+ ", you may change the property excelImport.dateFormat.");
		}
		try {
			pricePlan.setEndSubscriptionDate(getCellAsDate((Cell) cellsObj[i++]));
		} catch (Exception e) {
			throw new BusinessException("Invalid endAppli in line=" + rowIndex + " expected format:"
					+ param.getProperty("excelImport.dateFormat", "dd/MM/yyyy")
					+ ", you may change the property excelImport.dateFormat.");
		}
		String offerCode = getCellAsString((Cell) cellsObj[i++]);
		String priority = getCellAsString((Cell) cellsObj[i++]);
		String amountWOTax = getCellAsString((Cell) cellsObj[i++]);
		String amountWithTax = getCellAsString((Cell) cellsObj[i++]);
		String amountWOTaxEL = getCellAsString((Cell) cellsObj[i++]);
		String amountWithTaxEL = getCellAsString((Cell) cellsObj[i++]);
		String minQuantity = getCellAsString((Cell) cellsObj[i++]);
		String maxQuantity = getCellAsString((Cell) cellsObj[i++]);
		String criteria1 = getCellAsString((Cell) cellsObj[i++]);
		String criteria2 = getCellAsString((Cell) cellsObj[i++]);
		String criteria3 = getCellAsString((Cell) cellsObj[i++]);
		String criteriaEL = getCellAsString((Cell) cellsObj[i++]);
		try {
			pricePlan.setStartRatingDate(getCellAsDate((Cell) cellsObj[i++]));
		} catch (Exception e) {
			throw new BusinessException("Invalid startRating in line=" + rowIndex + " expected format:"
					+ param.getProperty("excelImport.dateFormat", "dd/MM/yyyy")
					+ ", you may change the property excelImport.dateFormat.");
		}
		try {
			pricePlan.setEndRatingDate(getCellAsDate((Cell) cellsObj[i++]));
		} catch (Exception e) {
			throw new BusinessException("Invalid endRating in line=" + rowIndex + " expected format:"
					+ param.getProperty("excelImport.dateFormat", "dd/MM/yyyy")
					+ ", you may change the property excelImport.dateFormat.");
		}
		String minSubAge = getCellAsString((Cell) cellsObj[i++]);
		String maxSubAge = getCellAsString((Cell) cellsObj[i++]);
		String validityCalendarCode = getCellAsString((Cell) cellsObj[i++]);
		log.debug(
				"priceplanCode={}, priceplanDescription= {}, chargeCode={} sellerCode={}, countryCode={}, currencyCode={},"
						+ " startSub={}, endSub={}, offerCode={}, priority={}, amountWOTax={}, amountWithTax={},amountWOTaxEL={}, amountWithTaxEL={},"
						+ " minQuantity={}, maxQuantity={}, criteria1={}, criteria2={}, criteria3={}, criteriaEL={},"
						+ " startRating={}, endRating={}, minSubAge={}, maxSubAge={}, validityCalendarCode={}",
				new Object[] { pricePlanCode, pricePlanDescription, eventCode, sellerCode, countryCode, currencyCode,
						pricePlan.getStartSubscriptionDate(), pricePlan.getEndSubscriptionDate(), offerCode, priority,
						amountWOTax, amountWithTax, amountWOTaxEL, amountWithTaxEL, minQuantity, maxQuantity, criteria1,
						criteria2, criteria3, criteriaEL, pricePlan.getStartRatingDate(), pricePlan.getEndRatingDate(),
						minSubAge, maxSubAge, validityCalendarCode });

		if (!StringUtils.isBlank(eventCode)) {
			qb = new QueryBuilder(ChargeTemplate.class, "p");
			qb.addCriterion("code", "=", eventCode, false);
			qb.addCriterionEntity("provider", provider);
			@SuppressWarnings("unchecked")
			List<Seller> charges = qb.getQuery(em).getResultList();
			if (charges.size() == 0) {
				throw new BusinessException("cannot find charge in line=" + rowIndex + " with code=" + eventCode);
			} else if (charges.size() > 1) {
				throw new BusinessException("more than one charge in line=" + rowIndex + " with code=" + eventCode);
			}
			pricePlan.setEventCode(eventCode);
		} else {
			throw new BusinessException("Empty chargeCode in line=" + rowIndex + ", code=" + eventCode);
		}

		// Seller
		if (!StringUtils.isBlank(sellerCode)) {
			qb = new QueryBuilder(Seller.class, "p");
			qb.addCriterion("code", "=", sellerCode, false);
			qb.addCriterionEntity("provider", provider);
			@SuppressWarnings("unchecked")
			List<Seller> sellers = qb.getQuery(em).getResultList();
			Seller seller = null;

			if (sellers == null || sellers.size() == 0) {
				throw new BusinessException("Invalid seller in line=" + rowIndex + ", code=" + sellerCode);
			}

			seller = sellers.get(0);
			pricePlan.setSeller(seller);
		} else {
			pricePlan.setSeller(null);
		}

		// Country
		if (!StringUtils.isBlank(countryCode)) {
			qb = new QueryBuilder(TradingCountry.class, "p");
			qb.addCriterion("p.country.countryCode", "=", countryCode, false);
			qb.addCriterionEntity("provider", provider);
			@SuppressWarnings("unchecked")
			List<TradingCountry> countries = qb.getQuery(em).getResultList();
			TradingCountry tradingCountry = null;

			if (countries == null || countries.size() == 0) {
				throw new BusinessException("Invalid country in line=" + rowIndex + ", code=" + countryCode);
			}

			tradingCountry = countries.get(0);
			pricePlan.setTradingCountry(tradingCountry);
		} else {
			pricePlan.setTradingCountry(null);
		}

		// Currency
		if (!StringUtils.isBlank(currencyCode)) {
			qb = new QueryBuilder(TradingCurrency.class, "p");
			qb.addCriterion("p.currency.currencyCode", "=", currencyCode, false);
			qb.addCriterionEntity("provider", provider);
			@SuppressWarnings("unchecked")
			List<TradingCurrency> currencies = qb.getQuery(em).getResultList();
			TradingCurrency tradingCurrency = null;

			if (currencies == null || currencies.size() == 0) {
				throw new BusinessException("Invalid currency in line=" + rowIndex + ", code=" + countryCode);
			}

			tradingCurrency = currencies.get(0);
			pricePlan.setTradingCurrency(tradingCurrency);
		} else {
			pricePlan.setTradingCurrency(null);
		}

		if (!StringUtils.isBlank(pricePlanCode)) {
			pricePlan.setCode(pricePlanCode);
		} else {
			throw new BusinessException("Invalid priceplan code in line=" + rowIndex + ", code=" + offerCode);
		}

		if (!StringUtils.isBlank(pricePlanDescription)) {
			pricePlan.setDescription(pricePlanDescription);
		} else {
			pricePlan.setDescription(pricePlanCode);
		}

		// OfferCode
		if (!StringUtils.isBlank(offerCode)) {
			qb = new QueryBuilder(OfferTemplate.class, "p");
			qb.addCriterion("code", "=", offerCode, false);
			qb.addCriterionEntity("provider", provider);
			@SuppressWarnings("unchecked")
			List<OfferTemplate> offers = qb.getQuery(em).getResultList();
			OfferTemplate offer = null;

			if (offers == null || offers.size() == 0) {
				throw new BusinessException("Invalid offer code in line=" + rowIndex + ", code=" + offerCode);
			}

			offer = offers.get(0);
			pricePlan.setOfferTemplate(offer);
		} else {
			pricePlan.setOfferTemplate(null);
		}

		if (!StringUtils.isBlank(validityCalendarCode)) {
			qb = new QueryBuilder(Calendar.class, "p");
			qb.addCriterion("code", "=", validityCalendarCode, false);
			qb.addCriterionEntity("provider", provider);
			@SuppressWarnings("unchecked")
			List<Calendar> calendars = qb.getQuery(em).getResultList();
			Calendar calendar = null;

			if (calendars == null || calendars.size() == 0) {
				throw new BusinessException(
						"Invalid calendars code in line=" + rowIndex + ", code=" + validityCalendarCode);
			}

			calendar = calendars.get(0);
			pricePlan.setValidityCalendar(calendar);
		} else {
			pricePlan.setValidityCalendar(null);
		}

		// Priority
		if (!StringUtils.isBlank(priority)) {
			try {
				pricePlan.setPriority(Integer.parseInt(priority));
			} catch (Exception e) {
				throw new BusinessException("Invalid priority in line=" + rowIndex + ", priority=" + priority);
			}
		} else {
			pricePlan.setPriority(1);
		}

		// AmountWOTax
		if (!StringUtils.isBlank(amountWOTax)) {
			try {
				pricePlan.setAmountWithoutTax(new BigDecimal(amountWOTax));
			} catch (Exception e) {
				throw new BusinessException(
						"Invalid amount wo tax in line=" + rowIndex + ", amountWOTax=" + amountWOTax);
			}
		} else {
			throw new BusinessException("Amount wo tax in line=" + rowIndex + " should not be empty");
		}

		// AmountWithTax
		if (!StringUtils.isBlank(amountWithTax)) {
			try {
				pricePlan.setAmountWithTax(new BigDecimal(amountWithTax));
			} catch (Exception e) {
				throw new BusinessException(
						"Invalid amount wo tax in line=" + rowIndex + ", amountWithTax=" + amountWithTax);
			}
		} else {
			pricePlan.setAmountWithTax(null);
		}

		if (!StringUtils.isBlank(amountWOTaxEL)) {
			pricePlan.setAmountWithoutTaxEL(amountWOTaxEL);
		} else {
			pricePlan.setAmountWithoutTaxEL(null);
		}

		if (!StringUtils.isBlank(amountWithTaxEL)) {
			pricePlan.setAmountWithTaxEL(amountWithTaxEL);
		} else {
			pricePlan.setAmountWithTaxEL(null);
		}
		// minQuantity
		if (!StringUtils.isBlank(minQuantity)) {
			try {
				pricePlan.setMinQuantity(new BigDecimal(minQuantity));
			} catch (Exception e) {
				throw new BusinessException("Invalid minQuantity in line=" + rowIndex + ", minQuantity=" + minQuantity);
			}
		} else {
			pricePlan.setMinQuantity(null);
		}

		// maxQuantity
		if (!StringUtils.isBlank(maxQuantity)) {
			try {
				pricePlan.setMaxQuantity(new BigDecimal(maxSubAge));
			} catch (Exception e) {
				throw new BusinessException("Invalid maxQuantity in line=" + rowIndex + ", maxQuantity=" + maxQuantity);
			}
		} else {
			pricePlan.setMaxQuantity(null);
		}

		// Criteria1
		if (!StringUtils.isBlank(criteria1)) {
			try {
				pricePlan.setCriteria1Value(criteria1);
			} catch (Exception e) {
				throw new BusinessException("Invalid criteria1 in line=" + rowIndex + ", criteria1=" + criteria1);
			}
		} else {
			pricePlan.setCriteria1Value(null);
		}

		// Criteria2
		if (!StringUtils.isBlank(criteria2)) {
			try {
				pricePlan.setCriteria2Value(criteria2);
			} catch (Exception e) {
				throw new BusinessException("Invalid criteria2 in line=" + rowIndex + ", criteria2=" + criteria2);
			}
		} else {
			pricePlan.setCriteria2Value(null);
		}

		// Criteria3
		if (!StringUtils.isBlank(criteria3)) {
			try {
				pricePlan.setCriteria3Value(criteria3);
			} catch (Exception e) {
				throw new BusinessException("Invalid criteria3 in line=" + rowIndex + ", criteria3=" + criteria3);
			}
		} else {
			pricePlan.setCriteria3Value(null);
		}

		// CriteriaEL
		if (!StringUtils.isBlank(criteriaEL)) {
			try {
				pricePlan.setCriteriaEL(criteriaEL);
				;
			} catch (Exception e) {
				throw new BusinessException("Invalid criteriaEL in line=" + rowIndex + ", criteriaEL=" + criteriaEL);
			}
		} else {
			pricePlan.setCriteriaEL(null);
		}

		// minSubAge
		if (!StringUtils.isBlank(minSubAge)) {
			try {
				pricePlan.setMinSubscriptionAgeInMonth(Long.parseLong(minSubAge));
			} catch (Exception e) {
				throw new BusinessException("Invalid minSubAge in line=" + rowIndex + ", minSubAge=" + minSubAge);
			}
		} else {
			pricePlan.setMinSubscriptionAgeInMonth(0L);
		}

		// maxSubAge
		if (!StringUtils.isBlank(maxSubAge)) {
			try {
				pricePlan.setMaxSubscriptionAgeInMonth(Long.parseLong(maxSubAge));
			} catch (Exception e) {
				throw new BusinessException("Invalid maxSubAge in line=" + rowIndex + ", maxSubAge=" + maxSubAge);
			}
		} else {
			pricePlan.setMaxSubscriptionAgeInMonth(9999L);
		}

		if (pricePlan.getId() == null) {
			create(pricePlan, user);
		} else {
			pricePlan.updateAudit(user);
			updateNoCheck(pricePlan);
		}
	}

	@Override
	public PricePlanMatrix findByCode(String code, Provider provider) {
		QueryBuilder qb = new QueryBuilder(PricePlanMatrix.class, "m", null, provider);
		qb.addCriterion("code", "=", code, true);

		try {
			return (PricePlanMatrix) qb.getQuery(getEntityManager()).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Get a list of priceplans to populate a cache
	 * 
	 * @return A list of active priceplans
	 */
	public List<PricePlanMatrix> getPricePlansForCache() {
		return getEntityManager().createNamedQuery("PricePlanMatrix.getPricePlansForCache", PricePlanMatrix.class)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<PricePlanMatrix> findByOfferTemplate(OfferTemplate offerTemplate) {
		QueryBuilder qb = new QueryBuilder(PricePlanMatrix.class, "p");
		qb.addCriterionEntity("offerTemplate", offerTemplate);

		try {
			return (List<PricePlanMatrix>) qb.getQuery(getEntityManager()).getResultList();
		} catch (NoResultException e) {
			log.warn("failed to find pricePlanMatrix By offerTemplate", e);
			return null;
		}
	}

	public List<PricePlanMatrix> findByOfferTemplateAndEventCode(String offerTemplateCode, String chargeCode,
			Provider provider) {

		List<PricePlanMatrix> priceplansByOffer = new ArrayList<>();

		List<PricePlanMatrix> priceplans = ratingCacheContainerProvider.getPricePlansByChargeCode(provider.getId(),
				chargeCode);
		if (priceplans == null) {
			return priceplansByOffer;
		}
		for (PricePlanMatrix pricePlan : priceplans) {
			if (offerTemplateCode == null) {
				if (pricePlan.getOfferTemplate() == null) {
					priceplansByOffer.add(pricePlan);
				}

			} else if (pricePlan.getOfferTemplate() != null
					&& pricePlan.getOfferTemplate().getCode().equals(offerTemplateCode)) {
				priceplansByOffer.add(pricePlan);
			}
		}

		return priceplansByOffer;
	}

	@SuppressWarnings("unchecked")
	public List<PricePlanMatrix> listByEventCode(String eventCode, Provider provider) {
		QueryBuilder qb = new QueryBuilder(PricePlanMatrix.class, "m", null, provider);
		qb.addCriterion("eventCode", "=", eventCode, true);

		try {
			return (List<PricePlanMatrix>) qb.getQuery(getEntityManager()).getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Long getLastPricePlanByCharge(String eventCode, Provider provider) {
		QueryBuilder qb = new QueryBuilder("select max(sequence) from PricePlanMatrix m");
		qb.addCriterion("m.eventCode", "=", eventCode, true);
		qb.addCriterionEntity("m.provider", provider);
		try {
			Long result = (Long) qb.getQuery(getEntityManager()).getSingleResult();
			return result == null ? 0L : result;
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean updateCellEdit(PricePlanMatrix entity, User currentUser) throws BusinessException {
		boolean result = false;
		PricePlanMatrix pricePlanMatrix = findById(entity.getId());
		if (pricePlanMatrix != null) {
			if (!equal(entity.getCode(), pricePlanMatrix.getCode())) {
				PricePlanMatrix existed = findByCode(entity.getCode(), getCurrentProvider());
				if (existed != null) {
					throw new BusinessException("Price plan " + entity.getCode() + " is existed!");
				} else {
					pricePlanMatrix.setCode(entity.getCode());
					result = true;
				}
			}
			if (!equal(entity.getDescription(), pricePlanMatrix.getDescription())) {
				pricePlanMatrix.setDescription(entity.getDescription());
				result = true;
			}
			if (!equal(entity.getEventCode(), pricePlanMatrix.getEventCode())) {
				pricePlanMatrix.setEventCode(entity.getEventCode());
				result = true;
			}
			if (!(equal(entity.getOfferTemplate(), pricePlanMatrix.getOfferTemplate()))) {
				pricePlanMatrix.setOfferTemplate(entity.getOfferTemplate());
				result = true;
			}
			if (!equal(entity.getSeller(), pricePlanMatrix.getSeller())) {
				pricePlanMatrix.setSeller(entity.getSeller());
				result = true;
			}
			if (!equal(entity.getAmountWithTax(), pricePlanMatrix.getAmountWithTax())) {
				pricePlanMatrix.setAmountWithTax(entity.getAmountWithTax());
				result = true;
			}
			if (!equal(entity.getAmountWithoutTax(), pricePlanMatrix.getAmountWithoutTax())) {
				pricePlanMatrix.setAmountWithoutTax(entity.getAmountWithoutTax());
				result = true;
			}
			if (!equal(entity.getAmountWithoutTaxEL(), pricePlanMatrix.getAmountWithoutTaxEL())) {
				pricePlanMatrix.setAmountWithoutTaxEL(entity.getAmountWithoutTaxEL());
				result = true;
			}
			if (!equal(entity.getAmountWithTaxEL(), pricePlanMatrix.getAmountWithTaxEL())) {
				pricePlanMatrix.setAmountWithTaxEL(entity.getAmountWithTaxEL());
				result = true;
			}
			if (!equal(entity.getStartRatingDate(), pricePlanMatrix.getStartRatingDate())) {
				pricePlanMatrix.setStartRatingDate(entity.getStartRatingDate());
				result = true;
			}
			if (!equal(entity.getEndRatingDate(), pricePlanMatrix.getEndRatingDate())) {
				pricePlanMatrix.setEndRatingDate(entity.getEndRatingDate());
				result = true;
			}
			if (!equal(entity.getCriteriaEL(), pricePlanMatrix.getCriteriaEL())) {
				pricePlanMatrix.setCriteriaEL(entity.getCriteriaEL());
				result = true;
			}
			if (!equal(entity.getTradingCountry(), pricePlanMatrix.getTradingCountry())) {
				pricePlanMatrix.setTradingCountry(entity.getTradingCountry());
				result = true;
			}
			if (!equal(entity.getTradingCurrency(), pricePlanMatrix.getTradingCurrency())) {
				pricePlanMatrix.setTradingCurrency(entity.getTradingCurrency());
				result = true;
			}
			if (!equal(entity.getCriteria1Value(), pricePlanMatrix.getCriteria1Value())) {
				pricePlanMatrix.setCriteria1Value(entity.getCriteria1Value());
				result = true;
			}
			if (!equal(entity.getCriteria2Value(), pricePlanMatrix.getCriteria2Value())) {
				pricePlanMatrix.setCriteria2Value(entity.getCriteria2Value());
				result = true;
			}
			if (!equal(entity.getCriteria3Value(), pricePlanMatrix.getCriteria3Value())) {
				pricePlanMatrix.setCriteria3Value(entity.getCriteria3Value());
				result = true;
			}
			if (!equal(entity.getPriority(), pricePlanMatrix.getPriority())) {
				pricePlanMatrix.setPriority(entity.getPriority());
				result = true;
			}
			if (!equal(entity.getMinQuantity(), pricePlanMatrix.getMinQuantity())) {
				pricePlanMatrix.setMinQuantity(entity.getMinQuantity());
				result = true;
			}
			if (!equal(entity.getMaxQuantity(), pricePlanMatrix.getMaxQuantity())) {
				pricePlanMatrix.setMaxQuantity(entity.getMaxQuantity());
				result = true;
			}
			if (!equal(entity.getStartSubscriptionDate(), pricePlanMatrix.getStartSubscriptionDate())) {
				pricePlanMatrix.setStartSubscriptionDate(entity.getStartSubscriptionDate());
				result = true;
			}
			if (!equal(entity.getEndSubscriptionDate(), pricePlanMatrix.getEndSubscriptionDate())) {
				pricePlanMatrix.setEndSubscriptionDate(entity.getEndSubscriptionDate());
				result = true;
			}
			if (!equal(entity.getMaxSubscriptionAgeInMonth(), pricePlanMatrix.getMaxSubscriptionAgeInMonth())) {
				pricePlanMatrix.setMaxSubscriptionAgeInMonth(entity.getMaxSubscriptionAgeInMonth());
				result = true;
			}
			if (!equal(entity.getMinSubscriptionAgeInMonth(), pricePlanMatrix.getMinSubscriptionAgeInMonth())) {
				pricePlanMatrix.setMinSubscriptionAgeInMonth(entity.getMinSubscriptionAgeInMonth());
				result = true;
			}
			if (!equal(entity.getValidityCalendar(), pricePlanMatrix.getValidityCalendar())) {
				pricePlanMatrix.setValidityCalendar(entity.getValidityCalendar());
				result = true;
			}
			if (result) {
				update(pricePlanMatrix, currentUser);
				this.ratingCacheContainerProvider.updatePricePlanInCache(pricePlanMatrix);
			}
		}
		return result;
	}

	public boolean equal(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return true;
		}
		return obj1 != null ? obj1.equals(obj2) : (obj2 != null ? false : true);
	}
	
	public synchronized void duplicate(PricePlanMatrix entity,User currentUser) throws BusinessException{
		entity=refreshOrRetrieve(entity);
		String code=findDuplicateCode(entity, currentUser);
		detach(entity);
		entity.setId(null);
		String sourceAppliesToEntity=entity.clearUuid();
		entity.setCode(code);
		create(entity, getCurrentUser());
		customFieldInstanceService.duplicateCfValues(sourceAppliesToEntity, entity, getCurrentUser());
	}

}

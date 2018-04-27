package org.meveo.service.custombilling;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.billing.CalculatedTerminationFee;
import org.meveo.service.base.PersistenceService;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Entity account service implementation.
 * 
 */

@Stateless
public class CustomBillingService extends PersistenceService<CalculatedTerminationFee> {


    public List<CalculatedTerminationFee> findByFeeCode(String feeCode) {
        final Class<CalculatedTerminationFee> entityClass = getEntityClass();
        QueryBuilder queryBuilder = new QueryBuilder(entityClass, "a", null);

        queryBuilder.addCriterion("feeCode", "like", "%" + feeCode + "%", true);
        return queryBuilder.getQuery(getEntityManager()).getResultList();
    }

    public CalculatedTerminationFee updateFee(String  feeCode) throws BusinessException {
        List<CalculatedTerminationFee> fee = this.findByFeeCode(feeCode);
        if(fee==null || fee.isEmpty())
            return null;

        return fee.get(0);
    }

}

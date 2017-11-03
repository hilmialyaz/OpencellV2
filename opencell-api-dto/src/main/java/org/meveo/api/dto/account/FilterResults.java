package org.meveo.api.dto.account;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specified how and on what criteria to filter API results
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface FilterResults {

    /**
     * Identifies the DTO property that returns the child entities to be iterated for filtering. e.g. if "customerAccounts.customerAccount" is passed into this attribute, then the
     * value of "dto.customerAccounts.customerAccount" will be parsed and filtered.
     * 
     * @return
     */
    String propertyToFilter();

    /**
     * Identifies the filtering rule to apply to items selected for filtering
     * 
     * @return
     */
    FilterProperty[] itemPropertiesToFilter();

}
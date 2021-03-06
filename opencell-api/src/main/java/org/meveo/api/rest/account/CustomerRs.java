package org.meveo.api.rest.account;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.account.CustomerBrandDto;
import org.meveo.api.dto.account.CustomerCategoryDto;
import org.meveo.api.dto.account.CustomerDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.PagingAndFiltering.SortOrder;
import org.meveo.api.dto.response.account.CustomersResponseDto;
import org.meveo.api.dto.response.account.GetCustomerResponseDto;
import org.meveo.api.rest.IBaseRs;

/**
 * @author Edward P. Legaspi
 **/
@Path("/account/customer")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface CustomerRs extends IBaseRs {

    /**
     * Create a new customer
     * 
     * @param postData The customer's data
     * @return Request processing status
     */
    @POST
    @Path("/")
    ActionStatus create(CustomerDto postData);

    /**
     * Update an existing customer
     * 
     * @param postData The customer's data
     * @return Request processing status
     */
    @PUT
    @Path("/")
    ActionStatus update(CustomerDto postData);

    /**
     * Search for a customer with a given code
     * 
     * @param customerCode The customer's code
     * @return The customer's data
     */
    @GET
    @Path("/")
    GetCustomerResponseDto find(@QueryParam("customerCode") String customerCode);

    /**
     * Remove customer with a given code
     * 
     * @param customerCode The customer's code
     * @return Request processing status
     */
    @DELETE
    @Path("/{customerCode}")
    ActionStatus remove(@PathParam("customerCode") String customerCode);

    /**
     * Filters are: category, seller, brand and provider.
     * 
     * @param postData The customer's data
     * @param firstRow Pagination - from record number. Deprecated in v.4.7, use "from" instead
     * @param numberOfRows Pagination - number of records to retrieve. Deprecated in v.4.7, use "limit" instead
     * @param offset Pagination - from record number
     * @param limit Pagination - number of records to retrieve
     * @param sortBy sortBy field
     * @param sortOrder ASC/DESC
     * @return list of customers
     */
    @POST
    @Path("/list47")
    public CustomersResponseDto list47(@Deprecated CustomerDto postData, @QueryParam("firstRow") @Deprecated Integer firstRow,
            @QueryParam("numberOfRows") @Deprecated Integer numberOfRows, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit,
            @DefaultValue("c.code") @QueryParam("sortBy") String sortBy, @DefaultValue("ASCENDING") @QueryParam("sortOrder") SortOrder sortOrder);

    /**
     * List customers matching a given criteria
     * 
     * @param query Search criteria
     * @param fields Data retrieval options/fieldnames separated by a comma
     * @param offset Pagination - from record number
     * @param limit Pagination - number of records to retrieve
     * @param sortBy Sorting - field to sort by - a field from a main entity being searched. See Data model for a list of fields.
     * @param sortOrder Sorting - sort order.
     * @return List of customers
     */
    @GET
    @Path("/list")
    public CustomersResponseDto listGet(@QueryParam("query") String query, @QueryParam("fields") String fields, @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit, @DefaultValue("code") @QueryParam("sortBy") String sortBy, @DefaultValue("ASCENDING") @QueryParam("sortOrder") SortOrder sortOrder);

    /**
     * List customers matching a given criteria
     * 
     * @param pagingAndFiltering Pagination and filtering criteria
     * @return List of customers
     */
    @POST
    @Path("/list")
    public CustomersResponseDto listPost(PagingAndFiltering pagingAndFiltering);

    /**
     * Create a new customer brand
     * 
     * @param postData The customer brand's data
     * @return Request processing status
     */
    @POST
    @Path("/createBrand")
    ActionStatus createBrand(CustomerBrandDto postData);

    /**
     * Update an existing customer brand
     * 
     * @param postData The customer brand's data
     * @return Request processing status
     */
    @PUT
    @Path("/updateBrand")
    ActionStatus updateBrand(CustomerBrandDto postData);

    /**
     * Create new or update an existing customer brand
     * 
     * @param postData The customer brand's data
     * @return Request processing status
     */
    @POST
    @Path("/createOrUpdateBrand")
    ActionStatus createOrUpdateBrand(CustomerBrandDto postData);

    /**
     * Create a new customer category
     * 
     * @param postData The customer category's data
     * @return Request processing status
     */
    @POST
    @Path("/createCategory")
    ActionStatus createCategory(CustomerCategoryDto postData);

    /**
     * Update an existing customer category
     * 
     * @param postData The customer category's data
     * @return Request processing status
     */
    @PUT
    @Path("/updateCategory")
    ActionStatus updateCategory(CustomerCategoryDto postData);

    /**
     * Create new or update an existing customer category
     * 
     * @param postData The customer category's data
     * @return Request processing status
     */
    @POST
    @Path("/createOrUpdateCategory")
    ActionStatus createOrUpdateCategory(CustomerCategoryDto postData);

    /**
     * Remove existing customer brand with a given brand code
     * 
     * @param brandCode The brand's code
     * @return Request processing status
     */
    @DELETE
    @Path("/removeBrand/{brandCode}")
    ActionStatus removeBrand(@PathParam("brandCode") String brandCode);

    /**
     * Remove an existing customer category with a given category code
     * 
     * @param categoryCode The category's code
     * @return Request processing status
     */
    @DELETE
    @Path("/removeCategory/{categoryCode}")
    ActionStatus removeCategory(@PathParam("categoryCode") String categoryCode);

    /**
     * Create new or update existing customer
     * 
     * @param postData The customer's data
     * @return Request processing status
     */
    @POST
    @Path("/createOrUpdate")
    ActionStatus createOrUpdate(CustomerDto postData);

}

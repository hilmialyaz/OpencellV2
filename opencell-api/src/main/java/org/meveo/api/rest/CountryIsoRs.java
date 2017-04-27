package org.meveo.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CountryIsoDto;
import org.meveo.api.dto.response.GetCountryIsoResponse;

/**
 * Web service for managing {@link org.meveo.model.billing.Country}
 * 
 * @author Edward P. Legaspi
 **/
@Path("/countryIso")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface CountryIsoRs extends IBaseRs {

    /**
     * Creates a tradingCountry base from the supplied country code. If the country code does not exists, a country and tradingCountry records are created
     * 
     * @param countryIsoDto
     * @return
     */
    @POST
    @Path("/")
    public ActionStatus create(CountryIsoDto countryIsoDto);

    /**
     * Search country with a given country code.
     * 
     * @param countryCode
     * @return {@link org.meveo.api.dto.response.GetCountryIsoResponse}.
     */
    @GET
    @Path("/")
    public GetCountryIsoResponse find(@QueryParam("countryCode") String countryCode);

    /**
     * Does not delete a country but the tradingCountry associated to it.
     * 
     * @param countryCode
     * @return
     */
    @DELETE
    @Path("/")
    public ActionStatus remove(@PathParam("countryCode") String countryCode);

    /**
     * Modify a country. Same input parameter as create. The country and tradingCountry are created if they don't exists. The operation fails if the tradingCountry is null.
     * 
     * @param countryIsoDto
     * @return
     */
    @PUT
    @Path("/")
    public ActionStatus update(CountryIsoDto countryIsoDto);

    @POST
    @Path("/createOrUpdate")
    public ActionStatus createOrUpdate(CountryIsoDto countryIsoDto);

}

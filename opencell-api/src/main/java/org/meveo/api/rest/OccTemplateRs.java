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
import org.meveo.api.dto.OccTemplateDto;
import org.meveo.api.dto.response.GetOccTemplateResponseDto;
import org.meveo.api.dto.response.GetOccTemplatesResponseDto;

@Path("/occTemplate")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface OccTemplateRs extends IBaseRs {

    /**
     * Create OccTemplate.
     * 
     * @param postData posted data to API (account operation template)
     * @return action status.
     */
    @Path("/")
    @POST
    ActionStatus create(OccTemplateDto postData);

    /**
     * Update OccTemplate.
     * 
     * @param postData posted data to API
     * @return action status.
     */
    @Path("/")
    @PUT
    ActionStatus update(OccTemplateDto postData);

    /**
     * Search OccTemplate with a given code.
     * 
     * @param occtemplateCode  code of account operation template
     * @return account operation template
     */
    @Path("/")
    @GET
    GetOccTemplateResponseDto find(@QueryParam("occTemplateCode") String occtemplateCode);

    /**
     * Remove OccTemplate with a given code.
     * 
     * @param occTemplateCode code of account operation template
     * @return action status.
     */
    @Path("/{occTemplateCode}")
    @DELETE
    ActionStatus remove(@PathParam("occTemplateCode") String occTemplateCode);

    /**
     * Create or update OccTemplate.
     * 
     * @param postData posted data
     * @return action status.
     */
    @Path("/createOrUpdate")
    @POST
    ActionStatus createOrUpdate(OccTemplateDto postData);

    /**
     * Get List of OccTemplates.
     *
     * @return list of all account operation template.
     */
    @Path("/list")
    @GET
    GetOccTemplatesResponseDto list();

}

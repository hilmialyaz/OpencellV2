package org.meveo.api.rest.impl;

import io.swagger.annotations.ApiOperation;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.LoginException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.rest.security.RSUser;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.admin.User;
import org.meveo.util.MeveoParamBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi
 **/
public abstract class BaseRs implements IBaseRs {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	@MeveoParamBean
	protected ParamBean paramBean;

	@Inject
	@RSUser
	private Instance<User> currentUserInstance;

	@Context
	protected HttpServletRequest httpServletRequest;

	// one way to get HttpServletResponse
	@Context
	protected HttpServletResponse httpServletResponse;

	protected final String RESPONSE_DELIMITER = " - ";

	@GET
	@Path("/version")
	@ApiOperation(value = "Returns the current MEVEO API version")
	public ActionStatus index() {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "MEVEO API Rest Web Service V4.2");
		return result;
	}

	@GET
	@Path("/user")
	@ApiOperation(value = "Returns the authenticated user")
	public ActionStatus user() {
		ActionStatus result = new ActionStatus();

		try {
			result = new ActionStatus(ActionStatusEnum.SUCCESS, "WS User is=" + getCurrentUser().toString());
		} catch (MeveoApiException e) {

		}

		return result;
	}

	public User getCurrentUser() throws MeveoApiException {

		if (currentUserInstance.isUnsatisfied() || currentUserInstance.get() == null) {
			throw new LoginException("Authentication failed! User does not exists!");
		}

		User currentUser = currentUserInstance.get();

		return currentUser;
	}

	protected Response.ResponseBuilder createResponseFromMeveoApiException(MeveoApiException e, ActionStatus result) {
		Response.ResponseBuilder responseBuilder = null;

		if (e instanceof EntityDoesNotExistsException) {
			responseBuilder = Response.status(Response.Status.NOT_FOUND).entity(result);
		} else if (e instanceof EntityAlreadyExistsException) {
			responseBuilder = Response.status(Response.Status.FOUND).entity(result);
		} else if (e instanceof MissingParameterException) {
			responseBuilder = Response.status(Response.Status.PRECONDITION_FAILED).entity(result);
		} else {
			responseBuilder = Response.status(Response.Status.BAD_REQUEST).entity(result);
		}

		return responseBuilder;
	}

	protected ResponseBuilder createResponseFromMeveoApiException(MeveoApiException e, BaseResponse result) {
		Response.ResponseBuilder responseBuilder = null;

		if (e instanceof EntityDoesNotExistsException) {
			responseBuilder = Response.status(Response.Status.NOT_FOUND).entity(result);
		} else if (e instanceof EntityAlreadyExistsException) {
			responseBuilder = Response.status(Response.Status.FOUND).entity(result);
		} else if (e instanceof MissingParameterException) {
			responseBuilder = Response.status(Response.Status.PRECONDITION_FAILED).entity(result);
		} else {
			responseBuilder = Response.status(Response.Status.BAD_REQUEST).entity(result);
		}

		return responseBuilder;
	}

}
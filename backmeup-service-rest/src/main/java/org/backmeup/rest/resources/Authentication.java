package org.backmeup.rest.resources;

import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.model.exceptions.UserNotActivatedException;
import org.backmeup.rest.auth.AuthInfo;

/**
 * Authenticate users and manage access tokens.
 */
@Path("/authenticate")
public class Authentication extends Base {	

	@PermitAll
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public AuthInfo authenticate(@QueryParam("username") String username, @QueryParam("password") String password) {
		try {
			BackMeUpUser user = getLogic().authorize(username, password);
			String accessToken = user.getUserId() + ";" + password;
			Date issueDate = new Date();
			return new AuthInfo(accessToken, issueDate);
		} catch (InvalidCredentialsException | UnknownUserException | UserNotActivatedException ex) {
		    LOGGER.info("", ex);
			throw new WebApplicationException(Status.UNAUTHORIZED);
		} catch (Exception ex) {
	        LOGGER.info("", ex);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}
}

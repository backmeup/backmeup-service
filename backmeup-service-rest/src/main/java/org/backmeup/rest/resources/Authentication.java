package org.backmeup.rest.resources;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.backmeup.model.Token;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.model.exceptions.UserNotActivatedException;
import org.backmeup.rest.auth.AuthInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticate users and manage access tokens.
 */
@Path("/authenticate")
public class Authentication extends Base {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authentication.class);

    @PermitAll
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthInfo authenticate(@QueryParam("username") String username, @QueryParam("password") String password) {
        try {
            Token token = getLogic().authorize(username, password);
            return new AuthInfo(token.getToken(), token.getTtl());
        } catch (InvalidCredentialsException | UnknownUserException | UserNotActivatedException ex) {
            LOGGER.info("", ex);
            throw new WebApplicationException(Status.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.info("", ex);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PermitAll
    @GET
    @Path("/anonymous")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthInfo authenticateAnonymousUser(@QueryParam("activationCode") String activationCode) {
        try {
            Token token = getLogic().authorize(activationCode);
            return new AuthInfo(token.getToken(), token.getTtl());
        } catch (InvalidCredentialsException | UnknownUserException | UserNotActivatedException ex) {
            LOGGER.info("", ex);
            throw new WebApplicationException(Status.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.info("", ex);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PermitAll
    @GET
    @Path("/worker")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthInfo authenticateWorker(@QueryParam("workerId") String workerId, @QueryParam("workerSecret") String workerSecret) {
        try {
            Token token = getLogic().authorizeWorker(workerId, workerSecret);
            return new AuthInfo(token.getToken(), token.getTtl());
        } catch (InvalidCredentialsException | UnknownUserException | UserNotActivatedException ex) {
            LOGGER.info("", ex);
            throw new WebApplicationException(Status.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.info("", ex);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
}

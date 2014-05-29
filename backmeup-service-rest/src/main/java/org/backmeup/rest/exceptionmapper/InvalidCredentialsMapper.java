package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.InvalidCredentialsException;

@Provider
public class InvalidCredentialsMapper extends
		CommonExceptionMapper<InvalidCredentialsException> {

	public InvalidCredentialsMapper() {
		super(Response.Status.UNAUTHORIZED);
	}
}

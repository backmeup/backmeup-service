package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.UnknownUserPropertyException;

@Provider
public class UnknownUserPropertyExceptionMapper extends
		CommonExceptionMapper<UnknownUserPropertyException> implements
		ExceptionMapper<UnknownUserPropertyException> {

	public UnknownUserPropertyExceptionMapper() {
		super(Response.Status.NOT_FOUND);
	}

}

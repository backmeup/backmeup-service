package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper extends
		CommonExceptionMapper<IllegalArgumentException> implements
		ExceptionMapper<IllegalArgumentException> {

	public IllegalArgumentExceptionMapper() {
		super(Response.Status.BAD_REQUEST);
	}

}

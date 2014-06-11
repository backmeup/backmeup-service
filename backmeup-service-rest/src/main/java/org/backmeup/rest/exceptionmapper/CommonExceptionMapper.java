package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.rest.data.ErrorEntity;

// There is a documented error when ExceptionMapper implement statement is in abstract class.
// The mapping of the exceptions does not work correctly. A workaround is proposed by adding 
// 'implements ExceptionMapper<ConcreteException>' to the concrete classes. 
// Also an upgrade to RESTEasy version 3.x should solve the problem.
public abstract class CommonExceptionMapper<T extends Exception> implements ExceptionMapper<T> {

	private final Status status;

    public CommonExceptionMapper(Status status) {
        this.status = status;
    }

    @Override
    public Response toResponse(T ex) {
        ErrorEntity error = new ErrorEntity(ex.getClass().getName(), ex);
        return Response.status(status).entity(error).build();
    }

}

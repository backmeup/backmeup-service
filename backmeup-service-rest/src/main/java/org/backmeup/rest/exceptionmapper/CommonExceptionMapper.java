package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.rest.data.ErrorEntity;

public abstract class CommonExceptionMapper<T extends Exception> implements ExceptionMapper<T> {

    private Status status;

    public CommonExceptionMapper(Status status) {
        this.status = status;
    }

    @Override
    public Response toResponse(T ex) {
        ErrorEntity error = new ErrorEntity(ex.getClass().getName(), ex);
        return Response.status(status).entity(error).build();
    }

}

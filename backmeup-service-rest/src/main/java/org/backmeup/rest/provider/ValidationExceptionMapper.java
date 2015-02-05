package org.backmeup.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.ValidationException;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {    
    public Response toResponse(ValidationException exception)  {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.getNotes()).build();
    }
}

package org.backmeup.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ValidationExceptionType;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {    
    public Response toResponse(ValidationException exception)  {
        if (exception.getType() == ValidationExceptionType.ConfigException && exception.getNotes() != null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.getNotes()).build();
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception).build();
        }
    }
}

package org.backmeup.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ValidationExceptionType;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {    
    public Response toResponse(ValidationException exception)  {
        switch(exception.getType()) {
        case ConfigException:
            if (exception.getNotes() != null && exception.getNotes().hasEntries()) {
                return Response.status(Status.BAD_REQUEST).entity(exception.getNotes()).build();
            } else {
                return Response.status(Status.BAD_REQUEST).entity(exception).build();
            }
        case APIException:
        case AuthException:
            return Response.status(Status.BAD_REQUEST).entity(exception).build();
        default:
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception).build();
        }
    }
}

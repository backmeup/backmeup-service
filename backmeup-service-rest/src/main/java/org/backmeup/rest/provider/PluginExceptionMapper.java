package org.backmeup.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.ValidationException;

public class PluginExceptionMapper implements ExceptionMapper<PluginException> {    
    public Response toResponse(PluginException exception)  {
        if (exception.getCause() instanceof ValidationException) {
            return new ValidationExceptionMapper().toResponse((ValidationException) exception.getCause());
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception).build();
        
    }
}
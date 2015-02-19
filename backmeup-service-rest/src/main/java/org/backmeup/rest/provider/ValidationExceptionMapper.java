package org.backmeup.rest.provider;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.ValidationException;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {    
    public Response toResponse(ValidationException exception)  {
        return Response.status(Status.BAD_REQUEST).entity(mapExceptionInformation(exception)).build();
    }
    
    public static Map<String, Object> mapExceptionInformation(ValidationException e) {
        Map<String, Object> info = BackMeUpExceptionMapper.mapExceptionInformation(e);
        
        info.put("type", e.getType());
        if (e.getNotes() != null && e.getNotes().hasEntries()) {
            info.put("validationEntries", e.getNotes());
        }
        
        return info;
    }
}

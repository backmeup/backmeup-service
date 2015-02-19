package org.backmeup.rest.provider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.ValidationException;

public class BackMeUpExceptionMapper implements ExceptionMapper<BackMeUpException> {    
    public Response toResponse(BackMeUpException exception)  {
        Throwable cause = exception;
        while(cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof ValidationException) {
                return new ValidationExceptionMapper().toResponse((ValidationException) cause);
            }
        }
        
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(mapExceptionInformation(exception)).build();
        
    }
    
    public static Map<String, Object> mapExceptionInformation(Throwable e) {
        Map<String, Object> info = new HashMap<>();
        
        info.put("exceptionClass", e.getClass().getName());
        info.put("message", e.getMessage());
        StringWriter errorStackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(errorStackTrace));
        info.put("stacktrace", errorStackTrace.toString());
        info.put("cause", mapExceptionInformation(e.getCause()));
        
        return info;
    }
    
    public static Map<String, Object> mapExceptionInformation(PluginException e) {
        Map<String, Object> info = mapExceptionInformation(e);
        info.put("pluginId", e.getPluginId());
        return info;
    }
}
package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.BackMeUpException;

@Provider
public class BackMeUpExceptionMapper extends CommonExceptionMapper<BackMeUpException> {

    public BackMeUpExceptionMapper() {
        super(Status.BAD_REQUEST);
    }

}

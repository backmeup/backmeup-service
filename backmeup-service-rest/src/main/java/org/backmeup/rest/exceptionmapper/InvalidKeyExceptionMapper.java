package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.InvalidKeyException;

@Provider
public class InvalidKeyExceptionMapper extends CommonExceptionMapper<InvalidKeyException> {

    public InvalidKeyExceptionMapper() {
        super(Response.Status.UNAUTHORIZED);
    }

}

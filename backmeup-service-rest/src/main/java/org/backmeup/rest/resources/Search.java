package org.backmeup.rest.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.index.model.SearchResponse;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.dto.SearchResponseDTO;
import org.backmeup.rest.auth.BackmeupPrincipal;

/**
 * This class contains search specific operations.
 * 
 * @author Peter Kofler
 */
@Path("search")
public class Search extends SecureBase {

    @Context
    private SecurityContext securityContext;

    @RolesAllowed("user")
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResponseDTO query(//
            @QueryParam("query") String query, //
            @QueryParam("source") String source, //
            @QueryParam("type") String type, //
            @QueryParam("job") String job,//
            @QueryParam("owner") String owner,//
            @QueryParam("tag") String tag, //
            @QueryParam("offset") Long offSetStart, //
            @QueryParam("maxresults") Long maxResults) {

        mandatory("query", query);
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        SearchResponse sr = getLogic().queryBackup(activeUser, query, source, type, job, owner, tag, offSetStart, maxResults);

        return getMapper().map(sr, SearchResponseDTO.class);
    }

    private void mandatory(String name, String value) {
        if (value == null) {
            badRequestMissingParameter(name);
        }
    }

    private void badRequestMissingParameter(String name) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                entity(name + " parameter is mandatory"). //
                build());
    }
}

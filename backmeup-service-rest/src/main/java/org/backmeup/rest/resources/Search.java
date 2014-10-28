package org.backmeup.rest.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.backmeup.index.model.SearchResponse;
import org.backmeup.model.dto.SearchResponseDTO;

/**
 * This class contains search specific operations.
 * 
 * @author fschoeppl
 */
@Path("search")
public class Search extends SecureBase {

    @Context
    private UriInfo info;

    @RolesAllowed("user")
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResponseDTO query(//
            @PathParam("userId") Long userId, // 
            @QueryParam("query") String query, //
            @QueryParam("source") String source, //
            @QueryParam("type") String type, //
            @QueryParam("job") String job) {

        canOnlyWorkWithMyData(userId);

        SearchResponse sr = getLogic().queryBackup(userId, query, source, type, job);

        return getMapper().map(sr, SearchResponseDTO.class);
    }

}

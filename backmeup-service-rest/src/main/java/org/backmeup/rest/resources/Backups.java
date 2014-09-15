package org.backmeup.rest.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.dto.BackupSearchDTO;
import org.backmeup.model.dto.SearchResponseDTO;
import org.backmeup.rest.auth.BackmeupPrincipal;

/**
 * This class contains search specific operations.
 * 
 * @author fschoeppl
 */
@Path("backups")
public class Backups extends Base {

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo info;

    @RolesAllowed("user")
    @PUT
    @Path("/{userId}/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSearch( //
            @PathParam("userId") Long userId, // 
            @FormParam("query") String query) throws URISyntaxException {

        canOnlyWorkWithMyData(userId);

        long searchId = getLogic().searchBackup(userId, query);

        URI u = new URI(String.format("%sbackups/%d/%d/query", info.getBaseUri().toString(), userId, searchId));
        return Response.status(Status.ACCEPTED).location(u).entity(new BackupSearchDTO(searchId)).build();
    }

    private void canOnlyWorkWithMyData(Long userId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal) securityContext.getUserPrincipal()).getUser();
        if (!activeUser.getUserId().equals(userId)) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
    }

    @RolesAllowed("user")
    @GET
    @Path("/{userId}/{searchId}/query")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResponseDTO query(//
            @PathParam("userId") Long userId, //
            @PathParam("searchId") Long searchId, //
            @QueryParam("source") String source, //
            @QueryParam("type") String type, //
            @QueryParam("job") String job) {
        canOnlyWorkWithMyData(userId);

        SearchResponse sr = null;

        Map<String, List<String>> filters = createFiltersFor(source, type, job);

        sr = getLogic().queryBackup(userId, searchId, filters);

        return getMapper().map(sr, SearchResponseDTO.class);
    }

    private Map<String, List<String>> createFiltersFor(String source, String type, String job) {
        Map<String, List<String>> filters = null;

        if (source != null || type != null || job != null) {
            filters = new HashMap<>();
        }

        if (source != null) {
            List<String> filtervalue = new LinkedList<>();
            filtervalue.add(source);
            filters.put("source", filtervalue);
        }

        if (type != null) {
            List<String> filtervalue = new LinkedList<>();
            filtervalue.add(type);
            filters.put("type", filtervalue);
        }

        if (job != null) {
            List<String> filtervalue = new LinkedList<>();
            filtervalue.add(job);
            filters.put("job", filtervalue);
        }

        return filters;
    }
}

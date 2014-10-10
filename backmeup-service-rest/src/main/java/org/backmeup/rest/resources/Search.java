package org.backmeup.rest.resources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

        Map<String, List<String>> filters = createFiltersFor(source, type, job);
        SearchResponse sr = getLogic().queryBackup(userId, query, filters);

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

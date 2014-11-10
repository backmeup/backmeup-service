package org.backmeup.rest.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

		mandatory("query", query);
		canOnlyWorkWithMyData(userId);

		SearchResponse sr = getLogic().queryBackup(userId, query, source, type,
				job);

		return getMapper().map(sr, SearchResponseDTO.class);
	}

	private void mandatory(String name, String value) {
		if (value == null) {
			badRequestMissingParameter(name);
		}
	}

	private void badRequestMissingParameter(String name) {
		throw new WebApplicationException(Response
				.status(Response.Status.BAD_REQUEST). //
				entity(name + " parameter is mandatory"). //
				build());
	}

	private Response status(Response.Status code, String message) {
		return Response.status(code).entity(message).build();
	}

}

package org.backmeup.rest.resources;

import javax.ws.rs.Path;

/**
 * This class contains search specific operations. 
 * 
 * @author fschoeppl
 */
@Path("backups")
public class Backups extends Base {

//    @Context
//    private SecurityContext securityContext;
//
//    @Context
//	UriInfo info;
//	
//	@POST
//	@Path("/{username}/search")
//	@Produces("application/json")
//	public Response search(@PathParam("username") String username,
//			@FormParam("keyRing") String keyRing,
//			@FormParam("query") String query) throws URISyntaxException {
//		long searchId = getLogic().searchBackup(userId, keyRing, query);
//		URI u = new URI(String.format("%sbackups/%s/%d/query", info
//				.getBaseUri().toString(), username, searchId));
//		return Response.status(Status.ACCEPTED).location(u)
//				.entity(new BackupSearchContainer(searchId)).build();
//	}
//
//	@GET
//	@Path("/{username}/{searchId}/query")
//	@Produces("application/json")
//	public SearchResponseContainer query(@PathParam("username") String username,
//			@PathParam("searchId") Long searchId,
//			@QueryParam("source") String source, @QueryParam("type") String type,
//			@QueryParam("job") String job ) {
//		SearchResponse sr = null;
//		Map<String, List<String>> filters = null;
//		
//		if ((source != null) || (type != null) || (job != null))
//		{
//			filters = new HashMap<String, List<String>>();
//			
//			if (source != null)
//			{
//				List<String> filtervalue = new LinkedList<String>();
//				filtervalue.add (source);
//				filters.put ("source", filtervalue);
//			}
//			
//			if (type != null)
//			{
//				List<String> filtervalue = new LinkedList<String>();
//				filtervalue.add (type);
//				filters.put ("type", filtervalue);
//			}
//			
//			if (job != null)
//			{
//				List<String> filtervalue = new LinkedList<String>();
//				filtervalue.add (job);
//				filters.put ("job", filtervalue);
//			}
//			
//		}
//		
//		sr = getLogic().queryBackup(username, searchId, filters);
//		
//		/*
//		System.out.println ("######################################################");
//		System.out.println (sr.getQuery ());
//		System.out.println (new SearchResponseContainer(sr).toString ());
//		System.out.println ("######################################################");
//		*/
//		
//		return new SearchResponseContainer(sr);
//	}
}

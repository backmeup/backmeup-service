package org.backmeup.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.dto.UserDTO;

/**
 * All user specific operations will be handled within this class.
 */
@Path("/users")
public class Users extends Base {	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public void listUsers() {
		
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public UserDTO addUser(UserDTO user) {
		BackMeUpUser userModel = getLogic().register(user.getName(), user.getPassword(), user.getPassword(), user.getEmail());
		user = getMapper().map(userModel, UserDTO.class);
		return user;
	}
	
	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserDTO getUser(@PathParam("userId") String userId) {
		BackMeUpUser userModel = getLogic().getUser(userId);
		UserDTO user = getMapper().map(userModel, UserDTO.class);
		return user;
	}
	
	public UserDTO updateUser(UserDTO user) {
		return user;
	}
	
	@DELETE
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteUser(UserDTO user) {
		getLogic().deleteUser(user.getEmail());
		
	}
}

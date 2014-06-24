package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.backmeup.model.dto.UserDTO;
import org.backmeup.rest.DummyDataManager;

/**
 * All user specific operations will be handled within this class.
 */
@Path("/users")
public class Users extends Base {	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserDTO> listUsers(@QueryParam("offset") int offset, @QueryParam("limit") int limit) {
		List<UserDTO> userList = new ArrayList<UserDTO>();
		userList.add(new UserDTO("John", "Doe", null, "john.doe@example.com"));
		userList.add(new UserDTO("Bob", "Doe", null, "bob.doe@example.com"));
		return userList;
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public UserDTO addUser(UserDTO user) {
//		BackMeUpUser userModel = getLogic().register(user.getLastname(), user.getPassword(), user.getPassword(), user.getEmail());
//		return getMapper().map(userModel, UserDTO.class);
		return DummyDataManager.getUserDTO();
	}
	
	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserDTO getUser(@PathParam("userId") String userId) {
//		BackMeUpUser userModel = getLogic().getUser(userId);
//		return getMapper().map(userModel, UserDTO.class);
		return DummyDataManager.getUserDTO();
	}
	
	@PUT
	@Path("/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public UserDTO updateUser(@PathParam("userId") String userId, UserDTO user) {
//		BackMeUpUser userModel = getLogic().getUser(userId);
//		BackMeUpUser newUser = getLogic().changeUser(userModel.getUsername(), user.getLastname(), user.getPassword(), user.getEmail());
//		return getMapper().map(newUser, UserDTO.class);
		return user;
	}
	
	@DELETE
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteUser(@PathParam("userId") String userId) {
//		getLogic().deleteUser(userId);
	}
}

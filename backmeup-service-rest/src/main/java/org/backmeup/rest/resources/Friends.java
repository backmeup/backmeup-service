package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.dto.FriendlistUserDTO;
import org.backmeup.rest.auth.BackmeupPrincipal;

/**
 * All friend specific endpoints e.g. friend list for sharing, heirs, etc.
 */
@Path("/friends")
public class Friends extends SecureBase {
    @Context
    private SecurityContext securityContext;

    @RolesAllowed("user")
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FriendlistUserDTO addFriend(FriendlistUserDTO userDTO) {
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        FriendlistUser user = getMapper().map(userDTO, FriendlistUser.class);
        user = getLogic().addFriend(activeUser.getUserId(), user);
        return getMapper().map(user, FriendlistUserDTO.class);
    }

    @RolesAllowed("user")
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FriendlistUserDTO> getFriends() {
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        List<FriendlistUser> lFriends = getLogic().getFriends(activeUser.getUserId());
        //build the return list
        List<FriendlistUserDTO> friendsDTOs = new ArrayList<>();
        for (FriendlistUser friend : lFriends) {
            FriendlistUserDTO friendDTO = getMapper().map(friend, FriendlistUserDTO.class);
            friendsDTOs.add(friendDTO);
        }
        return friendsDTOs;
    }

}

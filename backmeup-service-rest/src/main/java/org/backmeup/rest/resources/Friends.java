package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;
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
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        activeUser.setPassword(principal.getAuthToken().getB64Token());
        
        FriendlistUser user = getMapper().map(userDTO, FriendlistUser.class);
        user = getLogic().addFriend(activeUser, user);
        return getMapper().map(user, FriendlistUserDTO.class);
    }

    @RolesAllowed("user")
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FriendlistUserDTO> getFriends(//
            @QueryParam("list") FriendListType listType) {
        mandatory("list", listType);
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        List<FriendlistUser> lFriends = getLogic().getFriends(activeUser.getUserId(), listType);
        //build the return list
        List<FriendlistUserDTO> friendsDTOs = new ArrayList<>();
        for (FriendlistUser friend : lFriends) {
            FriendlistUserDTO friendDTO = getMapper().map(friend, FriendlistUserDTO.class);
            friendsDTOs.add(friendDTO);
        }
        return friendsDTOs;
    }

    @RolesAllowed("user")
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FriendlistUserDTO updateFriend(FriendlistUserDTO userDTO) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        FriendlistUser user = getMapper().map(userDTO, FriendlistUser.class);
        user = getLogic().updateFriend(activeUser.getUserId(), user);
        return getMapper().map(user, FriendlistUserDTO.class);
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> deleteFriend(//
            @QueryParam("friendId") Long friendId, //
            @QueryParam("list") FriendListType listType) {
        mandatory("friendId", friendId);
        mandatory("list", listType);

        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        getLogic().removeFriend(activeUser.getUserId(), friendId, listType);
        //TODO need to delete anonymous heritage user account when removing from friends list
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("status", "friend deleted");
        return ret;
    }

    private void mandatory(String name, Long l) {
        if (l == null || l == 0) {
            badRequestMissingParameter(name);
        }
    }

    private void mandatory(String name, FriendListType t) {
        if (t == null) {
            badRequestMissingParameter(name);
        }
    }

    private void badRequestMissingParameter(String name) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                entity(name + " parameter is mandatory"). //
                build());
    }

}

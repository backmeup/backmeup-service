package org.backmeup.rest.resources;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.rest.auth.BackmeupPrincipal;
import org.backmeup.utilities.qrcode.AccessTokenPdfQRCodeGenerator;

/**
 * All user specific operations will be handled within this class.
 */
@Path("/users")
public class Users extends SecureBase {
    @Context
    private SecurityContext securityContext;

    @PermitAll
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO addUser(UserDTO user) {
        BackMeUpUser userModel = getMapper().map(user, BackMeUpUser.class);
        userModel = getLogic().addUser(userModel);
        return getMapper().map(userModel, UserDTO.class);
    }
    
    @RolesAllowed("user")
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUser(@PathParam("userId") Long userId) {
        canOnlyWorkWithMyData(userId);

        BackMeUpUser userModel = getLogic().getUserByUserId(userId);
        return getMapper().map(userModel, UserDTO.class);
    }

    @RolesAllowed("user")
    @PUT
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO updateUser(@PathParam("userId") Long userId, UserDTO user) {
        canOnlyWorkWithMyData(userId);

        BackMeUpUser userModel = getLogic().getUserByUserId(userId);
        userModel.setFirstname(user.getFirstname());
        userModel.setLastname(user.getLastname());
        userModel.setEmail(user.getEmail());

        BackMeUpUser newUser = getLogic().updateUser(userModel);
        return getMapper().map(newUser, UserDTO.class);
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteUser(@PathParam("userId") Long userId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        if (!activeUser.getUserId().equals(userId)) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        getLogic().deleteUser(activeUser, userId);
    }
    
    @RolesAllowed("user")
    @POST
    @Path("/anonymous")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO addAnonymousUser() {
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        BackMeUpUser userModel = getLogic().addAnonymousUser(activeUser);
        return getMapper().map(userModel, UserDTO.class);
    }
    
    @RolesAllowed("user")
    @GET
    @Path("/{userId}/activationCode")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getAnonymousUserActivationCodeAsText(@PathParam("userId") Long userId) {
    	BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        String activationCode = getLogic().getAnonymousUserActivationCode(activeUser, userId);
        Map<String, String> map = new HashMap<String, String>();
        map.put("activationCode", activationCode);
        return map;
    }
    
    @RolesAllowed("user")
    @GET
    @Path("/{userId}/activationCode/pdf")
    @Produces("application/pdf")
    public Response getAnonymousUserActivationCodeAsPdf(@PathParam("userId") Long userId) {
        try {
            BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
            String activationCode = getLogic().getAnonymousUserActivationCode(activeUser, userId);
            InputStream pdf = new AccessTokenPdfQRCodeGenerator().generateQRCodePDF(activationCode);
            return Response
                    .ok(pdf)
                    .header("Content-Disposition",
                            "attachment; filename=" + "Backmeup_ActivationCode").build();
        } catch (Exception e) {
            throw new BackMeUpException("Cannot generate activation code document");
        }
    }
}

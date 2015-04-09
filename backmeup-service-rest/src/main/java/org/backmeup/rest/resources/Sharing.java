package org.backmeup.rest.resources;

import java.util.Set;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.UriInfo;

import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.rest.auth.BackmeupPrincipal;

/**
 * This class contains sharing policy specific operations.
 * 
 */
@Path("sharing")
public class Sharing extends SecureBase {

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo info;

    //TODO a user can remove a created sharing OR (or sharing that involves him?)
    //TODO a user must approve a sharing policy or individual record?

    @RolesAllowed("user")
    @GET
    @Path("/owned")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<SharingPolicyEntry> getAllOwned() {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        Set<SharingPolicyEntry> response = getLogic().getAllOwnedSharingPolicies(activeUser.getUserId());
        return response;
    }

    @RolesAllowed("user")
    @GET
    @Path("/incoming")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<SharingPolicyEntry> getAllIncoming() {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        Set<SharingPolicyEntry> response = getLogic().getAllIncomingSharingPolicies(activeUser.getUserId());
        return response;
    }

    @RolesAllowed("user")
    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public SharingPolicyEntry add(//
            @QueryParam("withUserId") Long sharingWithUserId,// 
            @QueryParam("policyType") SharingPolicyTypeEntry policyType,//
            @QueryParam("policyValue") String sharedElementID) {

        mandatory("withUserId", sharingWithUserId);
        mandatory("policyType", policyType);
        if ((policyType == SharingPolicyTypeEntry.Backup) || (policyType == SharingPolicyTypeEntry.Document)) {
            mandatory("policyValue", sharedElementID);
        }
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        try {
            SharingPolicyEntry response = getLogic().createAndAddSharingPolicy(activeUser.getUserId(),
                    sharingWithUserId, policyType, sharedElementID);
            return response;
        } catch (UnknownUserException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                    entity("non existing sharing partner"). //
                    build());
        }
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeOwned(//
            @QueryParam("policyID") Long policyID) {

        mandatory("policyID", policyID);
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        try {
            String response = getLogic().removeOwnedSharingPolicy(activeUser.getUserId(), policyID);
            return response;
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                    entity("non existing policy"). //
                    build());
        }
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/remove/all")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeAllOwned() {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        String response = getLogic().removeAllOwnedSharingPolicies(activeUser.getUserId());
        return response;
    }

    private void mandatory(String name, Long l) {
        if (l == null || l == 0) {
            badRequestMissingParameter(name);
        }
    }

    private void mandatory(String name, SharingPolicyTypeEntry type) {
        if (type == null || type.toString().isEmpty()) {
            badRequestMissingParameter(name);
        }
    }

    private void mandatory(String name, String value) {
        if (value == null || value.isEmpty()) {
            badRequestMissingParameter(name);
        }
    }

    private void badRequestMissingParameter(String name) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                entity(name + " parameter is mandatory"). //
                build());
    }

}

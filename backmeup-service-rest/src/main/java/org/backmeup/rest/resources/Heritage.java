package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.dto.SharingPolicyDTO;
import org.backmeup.model.dto.SharingPolicyDTO.SharingPolicyTypeEntryDTO;
import org.backmeup.model.dto.SharingPolicyUpdateDTO;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.rest.auth.BackmeupPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains heritage sharing policy specific operations.
 * 
 */
@Path("/heritage")
public class Heritage extends SecureBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sharing.class);

    @Context
    private SecurityContext securityContext;

    @RolesAllowed("user")
    @GET
    @Path("/owned")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<SharingPolicyEntry> getAllOwned() {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        Set<SharingPolicyEntry> response = getLogic().getAllOwnedHeritagePolicies(activeUser.getUserId());
        return response;
    }

    @RolesAllowed("user")
    @GET
    @Path("/incoming")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<SharingPolicyEntry> getAllIncoming() {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        Set<SharingPolicyEntry> response = getLogic().getAllIncomingHeritagePolicies(activeUser.getUserId());
        return response;
    }

    @RolesAllowed("user")
    @POST
    @Path("/add/json")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public SharingPolicyEntry add(SharingPolicyDTO sharingRequest) {
        //the UI framework is set to use POST operations with JSON requests and not Query Parameters
        return this.add(//
                sharingRequest.getWithUserId(),//
                convert(sharingRequest.getPolicyType()),//
                sharingRequest.getPolicyValue(),//
                sharingRequest.getName(), //
                sharingRequest.getDescription(),//
                sharingRequest.getLifespanstart(), sharingRequest.getLifespanend());
    }

    @RolesAllowed("user")
    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public SharingPolicyEntry add(//
            @QueryParam("withUserId") Long sharingWithUserId,// 
            @QueryParam("policyType") SharingPolicyTypeEntry policyType,//
            @QueryParam("policyValue") String sharedElementID,//
            @QueryParam("name") String name,//
            @QueryParam("description") String description,//
            @QueryParam("lifespanstart") Date lifespanStart,//
            @QueryParam("lifespanend") Date lifespanEnd) {

        if (policyType == SharingPolicyTypeEntry.Backup) {
            mandatoryLong("policyValue", sharedElementID);
        } else if (policyType == SharingPolicyTypeEntry.Document) {
            mandatoryUUID("policyValue", sharedElementID);
        } else if (policyType == SharingPolicyTypeEntry.DocumentGroup) {
            mandatoryListFromString("policyValue", sharedElementID);
        } else if (policyType == SharingPolicyTypeEntry.TaggedCollection) {
            mandatoryLong("policyValue", sharedElementID);
        }

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        try {
            SharingPolicyEntry response = getLogic().createAndAddHeritagePolicy(activeUser.getUserId(),
                    sharingWithUserId, policyType, sharedElementID, name, description, lifespanStart, lifespanEnd);
            return response;
        } catch (UnknownUserException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                    entity("non existing sharing partner"). //
                    build());
        }
    }

    @RolesAllowed("user")
    @POST
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public SharingPolicyEntry update(//
            @QueryParam("policyID") Long policyID,//
            @QueryParam("name") String name,//
            @QueryParam("description") String description,//
            @QueryParam("lifespanstart") Date lifespanStart,//
            @QueryParam("lifespanend") Date lifespanEnd) {

        mandatory("policyID", policyID);
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        try {
            SharingPolicyEntry response = getLogic().updateExistingHeritagePolicy(activeUser.getUserId(), policyID,
                    name, description, lifespanStart, lifespanEnd);
            return response;
        } catch (UnknownUserException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                    entity("non existing sharing partner"). //
                    build());
        }
    }

    @RolesAllowed("user")
    @POST
    @Path("/update/json")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public SharingPolicyEntry update(SharingPolicyUpdateDTO policyUpdateRequest) {
        //the UI framework is set to use POST operations with JSON requests and not Query Parameters
        return this.update(//
                policyUpdateRequest.getPolicyID(),//
                policyUpdateRequest.getName(), //
                policyUpdateRequest.getDescription(),//
                policyUpdateRequest.getLifespanstart(),//
                policyUpdateRequest.getLifespanend());
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> removeOwned(//
            @QueryParam("policyID") Long policyID) {

        mandatory("policyID", policyID);
        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        try {
            String response = getLogic().removeOwnedHeritagePolicy(activeUser.getUserId(), policyID);
            //return Map instead of String so that JSON response can be created
            Map<String, String> ret = new HashMap<String, String>();
            ret.put("status", response);
            return ret;
        } catch (IllegalArgumentException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                    entity("non existing policy"). //
                    build());
        }
    }

    @RolesAllowed("user")
    @POST
    @Path("/deadmannswitch/activate")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> activateDeadMannSwitchAndImport() {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        try {
            //Trigger data import of all owned heritage policies for this user
            String response = getLogic().activateDeadMannSwitchAndImport(activeUser.getUserId());
            //TODO AL lock the account of the sharing providing user

            //return Map instead of String so that JSON response can be created
            Map<String, String> ret = new HashMap<String, String>();
            ret.put("status", response);
            return ret;
        } catch (UnknownUserException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                    entity("activating dead man switch failed"). //
                    build());
        }
    }

    private void mandatory(String name, Long l) {
        if (l == null || l == 0) {
            badRequestMissingParameter(name);
        }
    }

    private void mandatoryListFromString(String name, String value) {
        if (value == null || value.isEmpty()) {
            badRequestMissingParameter(name);
        }
        try {
            String[] sArr = value.substring(1, value.length() - 1).split(",\\s*");
            List<String> lArr = Arrays.asList(sArr);
            if (lArr.size() <= 1) {
                badRequestMalformedListOfUUIDsParameter(name);
            }
            //test sample on UUIDs
            for (int i = 0; i < lArr.size(); i++) {
                UUID.fromString(lArr.get(i));
            }
        } catch (Exception e) {
            LOGGER.error("", e);
            badRequestMalformedListOfUUIDsParameter(name);
        }
    }

    public void mandatoryLong(String name, String value) {
        try {
            Long l = Long.valueOf(value);
            mandatory(name, l);
        } catch (Exception e) {
            LOGGER.error("", e);
            badRequestMissingParameter(name);
        }
    }

    private void mandatoryUUID(String name, String value) {
        if (value == null || value.isEmpty()) {
            badRequestMissingParameter(name);
        }
        try {
            UUID.fromString(value);
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(name + " parameter is malformed. Expecting UUID of syntax: "
                                    + UUID.randomUUID().toString()).build());
        }
    }

    private void badRequestMissingParameter(String name) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                entity(name + " parameter is mandatory"). //
                build());
    }

    private void badRequestMalformedListOfUUIDsParameter(String name) {
        List<UUID> l = new ArrayList<UUID>();
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                entity(name + " parameter is malformed. Expecting list in syntax: " + l.toString()). //
                build());
    }

    /**
     * A simple converter as the Model Objects of the bmu service project can't access the index-model
     * 
     * @param entry
     * @return
     */
    public SharingPolicyTypeEntry convert(SharingPolicyTypeEntryDTO entry) {
        if (entry == SharingPolicyTypeEntryDTO.AllFromNow) {
            return SharingPolicyTypeEntry.AllFromNow;
        }
        if (entry == SharingPolicyTypeEntryDTO.AllInklOld) {
            return SharingPolicyTypeEntry.AllInklOld;
        }
        if (entry == SharingPolicyTypeEntryDTO.Backup) {
            return SharingPolicyTypeEntry.Backup;
        }
        if (entry == SharingPolicyTypeEntryDTO.Document) {
            return SharingPolicyTypeEntry.Document;
        }
        if (entry == SharingPolicyTypeEntryDTO.DocumentGroup) {
            return SharingPolicyTypeEntry.DocumentGroup;
        }
        if (entry == SharingPolicyTypeEntryDTO.TaggedCollection) {
            return SharingPolicyTypeEntry.TaggedCollection;
        }
        return null;
    }

}

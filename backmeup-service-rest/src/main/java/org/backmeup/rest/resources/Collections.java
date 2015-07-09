package org.backmeup.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.index.model.tagging.TaggedCollectionEntry;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.dto.TaggedCollectionDTO;
import org.backmeup.model.dto.TaggedCollectionDocumentsDTO;
import org.backmeup.rest.auth.BackmeupPrincipal;

/**
 * This class contains tagged collections specific operations.
 * 
 */
@Path("collections")
public class Collections extends SecureBase {

    @Context
    private SecurityContext securityContext;

    @RolesAllowed("user")
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<TaggedCollectionEntry> getTaggedCollections(//
            @QueryParam("containsName") String name,//
            @QueryParam("containsDocs") List<UUID> lDocumentUUIDs) {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();

        if ((lDocumentUUIDs != null) && (lDocumentUUIDs.size() > 0)) {
            return getLogic().getAllTaggedCollectionsContainingDocuments(activeUser.getUserId(), lDocumentUUIDs);
        } else if ((name != null) && (!name.equals(""))) {
            return getLogic().getAllTaggedCollectionsByNameQuery(activeUser.getUserId(), name);
        } else {
            return getLogic().getAllTaggedCollections(activeUser.getUserId());
        }
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> removeTaggedCollections(//
            @QueryParam("collectionId") Long collectionID) {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();

        if (collectionID != null && collectionID != -1) {
            String response = getLogic().removeTaggedCollection(activeUser.getUserId(), collectionID);
            //return Map instead of String so that JSON response can be created
            Map<String, String> ret = new HashMap<String, String>();
            ret.put("status", response);
            return ret;
        } else {
            String response = getLogic().removeAllCollectionsForUser(activeUser.getUserId());
            //return Map instead of String so that JSON response can be created
            Map<String, String> ret = new HashMap<String, String>();
            ret.put("status", response);
            return ret;
        }
    }

    @RolesAllowed("user")
    @POST
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public TaggedCollectionEntry createAndAddTaggedCollection(//
            TaggedCollectionDTO taggedColl) {

        return this.createAndAddTaggedCollection(taggedColl.getName(), taggedColl.getDescription(),
                taggedColl.getDocumentIds());
    }

    @RolesAllowed("user")
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public TaggedCollectionEntry createAndAddTaggedCollection(//
            @QueryParam("name") String name,//
            @QueryParam("description") String description, //
            @QueryParam("documentIds") List<UUID> containedDocumentIDs) {

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        return getLogic().createAndAddTaggedCollection(activeUser.getUserId(), name, description, containedDocumentIDs);
    }

    @RolesAllowed("user")
    @POST
    @Path("/{collId}/adddocuments/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> addDocumentsToTaggedCollection(TaggedCollectionDocumentsDTO collDocs) {

        return this.addDocumentsToTaggedCollection(collDocs.getCollectionId(), collDocs.getDocumentIds());
    }

    @RolesAllowed("user")
    @POST
    @Path("/{collId}/adddocuments")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> addDocumentsToTaggedCollection(//
            @PathParam("collId") Long collectionID,//
            @QueryParam("documentIds") List<UUID> documentIDs) {

        mandatory("collId", collectionID);
        mandatory("documentIds", documentIDs);

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        String response = getLogic().addDocumentsToTaggedCollection(activeUser.getUserId(), collectionID, documentIDs);
        //return Map instead of String so that JSON response can be created
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("status", response);
        return ret;
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/{collId}/removedocuments/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> removeDocumentsFromTaggedCollection(TaggedCollectionDocumentsDTO collDocs) {

        return this.removeDocumentsFromTaggedCollection(collDocs.getCollectionId(), collDocs.getDocumentIds());
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/{collId}/removedocuments")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> removeDocumentsFromTaggedCollection(//
            @PathParam("collId") Long collectionID,//
            @QueryParam("documentIds") List<UUID> documentIDs) {

        mandatory("collId", collectionID);
        mandatory("documentIds", documentIDs);

        BackMeUpUser activeUser = ((BackmeupPrincipal) this.securityContext.getUserPrincipal()).getUser();
        String response = getLogic().removeDocumentsFromTaggedCollection(activeUser.getUserId(), collectionID,
                documentIDs);
        //return Map instead of String so that JSON response can be created
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("status", response);
        return ret;
    }

    private void mandatory(String name, Long l) {
        if (l == null || l == 0) {
            badRequestMissingParameter(name);
        }
    }

    public void mandatory(String name, List<UUID> value) {
        if (value == null || value.size() < 0) {
            badRequestMissingParameter(name);
        }
    }

    private void badRequestMissingParameter(String name) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST). //
                entity(name + " parameter is mandatory"). //
                build());
    }

}

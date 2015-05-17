package org.backmeup.logic.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.index.api.TaggedCollectionClient;
import org.backmeup.index.client.TaggedCollectionClientFactory;
import org.backmeup.index.model.tagging.TaggedCollectionEntry;
import org.backmeup.logic.CollectionLogic;
import org.backmeup.model.BackMeUpUser;

@ApplicationScoped
public class CollectionLogicImpl implements CollectionLogic {

    @Inject
    private TaggedCollectionClientFactory taggedCollectionClientFactory;

    private TaggedCollectionClient getTaggedCollectionClient(Long userId) {
        return this.taggedCollectionClientFactory.getTaggedCollectionClient(userId);
    }

    @Override
    public Set<TaggedCollectionEntry> getAllTaggedCollectionsContainingDocuments(BackMeUpUser currUser,
            List<UUID> lDocumentUUIDs) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            Set<TaggedCollectionEntry> result = client.getAllTaggedCollectionsContainingDocuments(lDocumentUUIDs);
            return result;
        }
    }

    @Override
    public Set<TaggedCollectionEntry> getAllTaggedCollectionsByNameQuery(BackMeUpUser currUser, String nameQuery) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            Set<TaggedCollectionEntry> result = client.getAllTaggedCollectionsByNameQuery(nameQuery);
            return result;
        }
    }

    @Override
    public Set<TaggedCollectionEntry> getAllTaggedCollections(BackMeUpUser currUser) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            Set<TaggedCollectionEntry> result = client.getAllTaggedCollections();
            return result;
        }
    }

    @Override
    public String removeTaggedCollection(BackMeUpUser currUser, Long collectionID) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            String result = client.removeTaggedCollection(collectionID);
            return result;
        }
    }

    @Override
    public String removeAllCollectionsForUser(BackMeUpUser currUser) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            String result = client.removeAllCollectionsForUser();
            return result;
        }
    }

    @Override
    public TaggedCollectionEntry createAndAddTaggedCollection(BackMeUpUser currUser, String name, String description,
            List<UUID> containedDocumentIDs) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            TaggedCollectionEntry result = client.createAndAddTaggedCollection(name, description, containedDocumentIDs);
            return result;
        }
    }

    @Override
    public String addDocumentsToTaggedCollection(BackMeUpUser currUser, Long collectionID,
            List<UUID> containedDocumentIDs) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            String result = client.addDocumentsToTaggedCollection(collectionID, containedDocumentIDs);
            return result;
        }
    }

    @Override
    public String removeDocumentsFromTaggedCollection(BackMeUpUser currUser, Long collectionID,
            List<UUID> containedDocumentIDs) {
        try (TaggedCollectionClient client = getTaggedCollectionClient(currUser.getUserId())) {
            String result = client.removeDocumentsFromTaggedCollection(collectionID, containedDocumentIDs);
            return result;
        }
    }

}

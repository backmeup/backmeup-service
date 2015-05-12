package org.backmeup.logic;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.backmeup.index.model.tagging.TaggedCollectionEntry;
import org.backmeup.model.BackMeUpUser;

/**
 * Tagged Collection related business logic.
 * 
 */
public interface CollectionLogic {

    Set<TaggedCollectionEntry> getAllTaggedCollectionsContainingDocuments(BackMeUpUser currUser,
            List<UUID> lDocumentUUIDs);

    Set<TaggedCollectionEntry> getAllTaggedCollectionsByNameQuery(BackMeUpUser currUser, String name);

    Set<TaggedCollectionEntry> getAllTaggedCollections(BackMeUpUser currUser);

    String removeTaggedCollection(BackMeUpUser currUser, Long collectionID);

    String removeAllCollectionsForUser(BackMeUpUser currUser);

    TaggedCollectionEntry createAndAddTaggedCollection(BackMeUpUser currUser, String name, String description,
            List<UUID> containedDocumentIDs);

    String addDocumentsToTaggedCollection(BackMeUpUser currUser, Long collectionID, List<UUID> containedDocumentIDs);

    String removeDocumentsFromTaggedCollection(BackMeUpUser currUser, Long collectionID, List<UUID> containedDocumentIDs);
}
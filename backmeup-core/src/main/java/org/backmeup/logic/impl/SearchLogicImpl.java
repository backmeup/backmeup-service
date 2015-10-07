package org.backmeup.logic.impl;

import java.io.File;
import java.util.Date;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.index.api.IndexClient;
import org.backmeup.index.client.IndexClientFactory;
import org.backmeup.index.model.FileItem;
import org.backmeup.index.model.SearchResponse;
import org.backmeup.index.model.User;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.logic.SearchLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.ProtocolDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SearchLogicImpl implements SearchLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchLogicImpl.class);

    @Inject
    private IndexClientFactory indexClientFactory;

    private IndexClient getIndexClient(User user) {
        return this.indexClientFactory.getIndexClient(user);
    }

    @Override
    public SearchResponse runSearch(BackMeUpUser bmuUser, String query, String source, String type, String job, String owner, String tag,
            Long offSetStart, Long maxResults) {

        try (IndexClient client = getIndexClient(wrap2IndexerUser(bmuUser))) {

            SearchResponse result = new SearchResponse(query);
            result.setDetails(client.queryBackup(result.getQuery(), source, type, job, owner, tag, bmuUser.getUsername(), offSetStart,
                    maxResults));
            return result;

        }
    }

    @Override
    public Set<FileItem> getAllFileItems(BackMeUpUser bmuUser, Long jobId) {
        try (IndexClient client = getIndexClient(wrap2IndexerUser(bmuUser))) {

            return client.searchAllFileItemsForJob(jobId);

        }
    }

    @Override
    public ProtocolDetails getProtocolDetails(BackMeUpUser bmuUser, String fileId) {
        try (IndexClient client = getIndexClient(wrap2IndexerUser(bmuUser))) {

            ProtocolDetails pd = new ProtocolDetails();
            pd.setFileInfo(client.getFileInfoForFile(fileId));
            return pd;

        }
    }

    @Override
    public File getThumbnailPathForFile(BackMeUpUser bmuUser, String fileId) {
        try (IndexClient client = getIndexClient(wrap2IndexerUser(bmuUser))) {

            String thumbnailPath = client.getThumbnailPathForFile(fileId);
            LOGGER.debug("Got thumbnail path: " + thumbnailPath);
            if (thumbnailPath != null) { // NOSONAR can be null!
                return new File(thumbnailPath);
            }
            return null; // Too bad there's no optional return types in Java...

        }
    }

    @Override
    public void delete(BackMeUpUser bmuUser, Long jobId, Long timestamp) {
        try (IndexClient client = getIndexClient(wrap2IndexerUser(bmuUser))) {

            client.deleteRecordsForUserAndJobAndTimestamp(jobId, new Date(timestamp));

        }
    }

    @Override
    public void deleteIndexOf(BackMeUpUser bmuUser) {
        try (IndexClient client = getIndexClient(wrap2IndexerUser(bmuUser))) {

            client.deleteRecordsForUser();

        }
    }

    /**
     * Takes a BMUUser object, creates keyserver internal token and returns a User object which is used throughout the
     * indexer REST calls and backend
     */
    private User wrap2IndexerUser(BackMeUpUser bmuUser) {
        //create a keyserver tokenDTO object for this user - usable to authenticate with keyserver
        TokenDTO keyserverToken = new TokenDTO(Kind.INTERNAL, bmuUser.getPassword());
        //wrap the user object to be understood by the indexer
        return new User(bmuUser.getUserId(), keyserverToken.toTokenString());
    }

}

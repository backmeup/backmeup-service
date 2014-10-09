package org.backmeup.logic.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.backmeup.index.client.ElasticSearchIndexClient;
import org.backmeup.index.model.FileItem;
import org.backmeup.index.model.SearchResponse;
import org.backmeup.logic.SearchLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.ProtocolDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SearchLogicImpl implements SearchLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ElasticSearchIndexClient getIndexClient(Long userId) {
        return new ElasticSearchIndexClient(userId);
    }

    @Override
    public SearchResponse runSearch(BackMeUpUser user, String query, Map<String, List<String>> filters) {
        try (ElasticSearchIndexClient client = getIndexClient(user.getUserId());) {

            SearchResponse result = new SearchResponse(query);
            client.queryBackup(result.getQuery(), filters, user.getUsername(), result);
            return result;
            
        }
    }

    @Override
    public Set<FileItem> getAllFileItems(Long userId, Long jobId) {
        try (ElasticSearchIndexClient client = getIndexClient(userId)) {

            return client.searchAllFileItemsForJob(jobId);
            
        }
    }

    @Override
    public ProtocolDetails getProtocolDetails(Long userId, String fileId) {
        try (ElasticSearchIndexClient client = getIndexClient(userId)) {

            ProtocolDetails pd = new ProtocolDetails();
            pd.setFileInfo(client.getFileInfoForFile(fileId));
            return pd;
            
        }
    }

    @Override
    public File getThumbnailPathForFile(Long userId, String fileId) {
        try (ElasticSearchIndexClient client = getIndexClient(userId)) {

            String thumbnailPath = client.getThumbnailPathForFile(fileId);
            logger.debug("Got thumbnail path: " + thumbnailPath);
            if (thumbnailPath != null) { // NOSONAR can be null!
                return new File(thumbnailPath);
            }
            return null; // Too bad there's no optional return types in Java...

        }
    }

    @Override
    public void delete(Long userId, Long jobId, Long timestamp) {
        try (ElasticSearchIndexClient client = getIndexClient(userId)) {

            client.deleteRecordsForJobAndTimestamp(jobId, timestamp);

        }
    }

    @Override
    public void deleteIndexOf(Long userId) {
        try (ElasticSearchIndexClient client = getIndexClient(userId)) {

            client.deleteRecordsForUser();

        }
    }

}

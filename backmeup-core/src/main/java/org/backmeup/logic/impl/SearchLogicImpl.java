package org.backmeup.logic.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.SearchResponseDao;
import org.backmeup.index.model.FileItem;
import org.backmeup.logic.SearchLogic;
import org.backmeup.logic.index.ElasticSearchIndexClient;
import org.backmeup.logic.index.IndexUtils;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.exceptions.BackMeUpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SearchLogicImpl implements SearchLogic {

    private static final String UNKNOWN_SEARCH_ID = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SEARCH_ID";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceBundle textBundle = ResourceBundle.getBundle("SearchLogicImpl");

    @Inject
    private DataAccessLayer dal;

    private SearchResponseDao getSearchResponseDao() {
        return dal.createSearchResponseDao();
    }

    private ElasticSearchIndexClient getIndexClient() {
        // TODO PK remove use indexer-client here, port depends on user
        return new ElasticSearchIndexClient("x", 1);
    }

    @Override
    public SearchResponse createSearch(String query, String[] typeFilters) {
        SearchResponse search = new SearchResponse(query, Arrays.asList(typeFilters));
        search = getSearchResponseDao().save(search);
        return search;
    }

    @Override
    public SearchResponse runSearch(BackMeUpUser user, long searchId, Map<String, List<String>> filters) {
        try (ElasticSearchIndexClient client = getIndexClient();) {

            SearchResponse search = queryExistingSearch(searchId);
            String query = search.getQuery();
            
            org.elasticsearch.action.search.SearchResponse esResponse = client.queryBackup(user.getUserId(), query, filters);
            search.setFiles(IndexUtils.convertSearchEntries(esResponse, user.getUsername()));
            search.setBySource(IndexUtils.getBySource(esResponse));
            search.setByType(IndexUtils.getByType(esResponse));
            search.setByJob(IndexUtils.getByJob(esResponse));
            return search;

        } 
    }

    private SearchResponse queryExistingSearch(long searchId) {
        SearchResponse search = getSearchResponseDao().findById(searchId);
        if (search == null) {
            throw new BackMeUpException(textBundle.getString(UNKNOWN_SEARCH_ID));
        }
        return search;
    }

    @Override
    public Set<FileItem> getAllFileItems(Long jobId) {
        try (ElasticSearchIndexClient client = getIndexClient()) {

            org.elasticsearch.action.search.SearchResponse esResponse = client.searchByJobId(jobId);
            return IndexUtils.convertToFileItems(esResponse);

        } 
    }

    @Override
    public ProtocolDetails getProtocolDetails(Long userId, String fileId) {
        try (ElasticSearchIndexClient client = getIndexClient()) {

            org.elasticsearch.action.search.SearchResponse esResponse = client.getFileById(userId, fileId);

            ProtocolDetails pd = new ProtocolDetails();
            pd.setFileInfo(IndexUtils.convertToFileInfo(esResponse));
            return pd;

        }
    }

    @Override
    public File getThumbnailPathForFile(BackMeUpUser user, String fileId) {
        try (ElasticSearchIndexClient client = getIndexClient()) {

            String thumbnailPath = client.getThumbnailPathForFile(user.getUserId(), fileId);
            logger.debug("Got thumbnail path: " + thumbnailPath);
            if (thumbnailPath != null) { // NOSONAR can be null!
                return new File(thumbnailPath);
            }
            return null; // Too bad there's no optional return types in Java...

        }
    }

    @Override
    public void delete(Long jobId, Long timestamp) {
        try (ElasticSearchIndexClient client = getIndexClient()) {

            client.deleteRecordsForJobAndTimestamp(jobId, timestamp);

        }
    }

    @Override
    public void deleteIndexOf(BackMeUpUser user) {
        try (ElasticSearchIndexClient client = getIndexClient()) {

            client.deleteRecordsForUser(user.getUserId());

        }
    }

}

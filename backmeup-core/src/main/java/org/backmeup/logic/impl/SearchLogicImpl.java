package org.backmeup.logic.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.SearchResponseDao;
import org.backmeup.logic.SearchLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.FileItem;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.plugin.api.actions.indexing.ElasticSearchIndexClient;
import org.backmeup.plugin.api.actions.indexing.IndexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SearchLogicImpl implements SearchLogic {

    private static final String UNKNOWN_SEARCH_ID = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SEARCH_ID";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceBundle textBundle = ResourceBundle.getBundle("SearchLogicImpl");

    @Inject
    @Configuration(key = "backmeup.index.host")
    private String indexHost;

    @Inject
    @Configuration(key = "backmeup.index.port")
    private Integer indexPort;

    @Inject
    private DataAccessLayer dal;

    private SearchResponseDao getSearchResponseDao() {
        return dal.createSearchResponseDao();
    }

    private ElasticSearchIndexClient getIndexClient() {
        return new ElasticSearchIndexClient(indexHost, indexPort);
    }

    @Override
    public SearchResponse createSearch(String query, String[] typeFilters) {
        SearchResponse search = new SearchResponse(query, Arrays.asList(typeFilters));
        search = getSearchResponseDao().save(search);
        return search;
    }

    @Override
    public SearchResponse runSearch(BackMeUpUser user, long searchId, Map<String, List<String>> filters) {
        ElasticSearchIndexClient client = null;
        try {

            SearchResponse search = queryExistingSearch(searchId);

            String query = search.getQuery();

            client = getIndexClient();
            org.elasticsearch.action.search.SearchResponse esResponse = client.queryBackup(user, query, filters);
            search.setFiles(IndexUtils.convertSearchEntries(esResponse, user));
            search.setBySource(IndexUtils.getBySource(esResponse));
            search.setByType(IndexUtils.getByType(esResponse));
            search.setByJob(IndexUtils.getByJob(esResponse));
            return search;

        } finally {
            if (client != null) {
                client.close();
            }
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
    public Set<FileItem> getAllFileItems(BackupJob job) {
        ElasticSearchIndexClient client = null;
        try {

            client = getIndexClient();
            org.elasticsearch.action.search.SearchResponse esResponse = client.searchByJobId(job.getId());
            return IndexUtils.convertToFileItems(esResponse);

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public ProtocolDetails getProtocolDetails(String username, String fileId) {
        ElasticSearchIndexClient client = null;
        try {

            client = getIndexClient();
            org.elasticsearch.action.search.SearchResponse esResponse = client.getFileById(username, fileId);

            ProtocolDetails pd = new ProtocolDetails();
            pd.setFileInfo(IndexUtils.convertToFileInfo(esResponse));
            return pd;

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public File getThumbnailPathForFile(BackMeUpUser user, String fileId) {
        ElasticSearchIndexClient client = null;
        try {

            client = getIndexClient();
            String thumbnailPath = client.getThumbnailPathForFile(user.getUsername(), fileId);
            logger.debug("Got thumbnail path: " + thumbnailPath);
            if (thumbnailPath != null) {
                return new File(thumbnailPath);
            }
            return null; // Too bad there's no optional return types in Java...

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void delete(BackupJob job, Long timestamp) {
        ElasticSearchIndexClient client = null;
        try {

            client = getIndexClient();
            client.deleteRecordsForJobAndTimestamp(job.getId(), timestamp);

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void deleteIndexOf(BackMeUpUser user) {
        ElasticSearchIndexClient client = null;
        try {

            client = getIndexClient();
            client.deleteRecordsForUser(user.getUserId());

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

}

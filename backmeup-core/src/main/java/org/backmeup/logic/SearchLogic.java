package org.backmeup.logic;

import java.io.File;
import java.util.Set;

import org.backmeup.index.model.FileItem;
import org.backmeup.index.model.SearchResponse;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.ProtocolDetails;

/**
 * Search related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface SearchLogic {

    /**
     * Querying the backuped and indexed data of a given user by search queries, filters, etc.
     * @query user the backmeup user currently operating the system sending this request
     * @param query the query string including wildcards etc
     * @param filterBySource by plugin source
     * @param filterByType by data type e.g. image, html, etc.
     * @param filterByJob by a specific backupjob
     * @param owner query for a specific userID that is the owner of the data
     * @param username query by username e.g. to distinguish owner and sharing partner
     * @param tag provide tags to restrict the query
     * @param offSetStart when offSetStart is set to 100 then the first 1-99 results will not be returned
     * @param maxResults limits the number of returned results e.g. to 50 search results
     * @return
     */
    SearchResponse runSearch(BackMeUpUser user, String query, String source, String type, String job, String owner,
            String tag, Long offSetStart, Long maxResults);

    Set<FileItem> getAllFileItems(BackMeUpUser user, Long jobId);

    ProtocolDetails getProtocolDetails(BackMeUpUser user, String fileId);

    File getThumbnailPathForFile(BackMeUpUser user, String fileId);

    void delete(BackMeUpUser user, Long jobId, Long timestamp);

    void deleteIndexOf(BackMeUpUser user);

}
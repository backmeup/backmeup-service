package org.backmeup.logic;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.FileItem;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.SearchResponse;

/**
 * Search related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface SearchLogic {

    SearchResponse createSearch(String query, String[] typeFilters);

    SearchResponse runSearch(BackMeUpUser user, long searchId, Map<String, List<String>> filters);

    Set<FileItem> getAllFileItems(BackupJob job);

    ProtocolDetails getProtocolDetails(String username, String fileId);

    File getThumbnailPathForFile(BackMeUpUser user, String fileId);

    void delete(BackupJob job, Long timestamp);

    void deleteIndexOf(BackMeUpUser user);

}
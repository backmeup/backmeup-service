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

    SearchResponse runSearch(BackMeUpUser user, String query, String source, String type, String job, String owner,
            String tag);

    Set<FileItem> getAllFileItems(Long userId, Long jobId);

    ProtocolDetails getProtocolDetails(Long userId, String fileId);

    File getThumbnailPathForFile(Long userId, String fileId);

    void delete(Long userId, Long jobId, Long timestamp);

    void deleteIndexOf(Long userId);

}
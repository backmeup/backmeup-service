package org.backmeup.logic;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.backmeup.model.FileItem;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.User;

/**
 * Search related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface SearchLogic {

    SearchResponse createSearch(String query, String[] typeFilters);

    SearchResponse runSearch(User user, long searchId, Map<String, List<String>> filters);

    Set<FileItem> getAllFileItems(Long jobId);

    ProtocolDetails getProtocolDetails(String username, String fileId);

    File getThumbnailPathForFile(User user, String fileId);

    void delete(Long jobId, Long timestamp);

    void deleteIndexOf(User user);

}
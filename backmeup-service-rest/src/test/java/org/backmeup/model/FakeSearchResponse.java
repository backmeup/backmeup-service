package org.backmeup.model;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.backmeup.index.model.CountedEntry;
import org.backmeup.index.model.SearchEntry;
import org.backmeup.index.model.SearchResponse;

public class FakeSearchResponse {

    public static SearchResponse oneFile() {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setQuery("find_me");
        searchResponse.setByJob(Arrays.asList(new CountedEntry("first Job", 1), new CountedEntry("next Job", 1)));
        searchResponse.setBySource(Arrays.asList(new CountedEntry("Dropbox", 2), new CountedEntry("Facebook", 2)));
        searchResponse.setByType(Arrays.asList(new CountedEntry("Type", 3)));
        searchResponse.setFiles(Arrays.asList(new SearchEntry("fileId", "2", true, new Date(), "type",
                "A wonderful file (title)", "downloadUrl", "thmbnailUrl", "Dropbox", "Storage", "first Job",
                "...lore ipsum...", new HashMap<String, String>(), new HashMap<String, String>())));
        return searchResponse;
    }
}

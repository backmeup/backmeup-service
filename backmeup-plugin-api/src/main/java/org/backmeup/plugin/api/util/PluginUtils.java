package org.backmeup.plugin.api.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class PluginUtils {
    private static final String CHAR_ENCODING = "UTF-8";

    private PluginUtils() {
        // Utility classes should not have public constructor
    }
    
    public static class QueryParameters {
        private Map<String, List<String>> queryPairs;

        public QueryParameters(Map<String, List<String>> queryPairs) {
            this.queryPairs = queryPairs;
        }

        public String getParameter(String key) {
            List<String> values = this.queryPairs.get(key);
            if (values == null || values.isEmpty()) {
                return null;
            }
            return values.get(0);
        }

        public List<String> getMultivalueParameter(String key) {
            List<String> values = this.queryPairs.get(key);
            if (values == null) {
                return new ArrayList<String>();
            }
            return values;
        }

    }

    public static QueryParameters splitQuery(URL url) throws UnsupportedEncodingException {
        return splitQuery(url.getQuery());
    }

    public static QueryParameters splitQuery(String queryParams) throws UnsupportedEncodingException {
        if (queryParams == null) {
            throw new IllegalArgumentException("Parameter queryParams must not be null");
        }

        final Map<String, List<String>> queryPairs = new LinkedHashMap<String, List<String>>();

        for (String pair : queryParams.split("&")) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), CHAR_ENCODING) : pair;

            if (!queryPairs.containsKey(key)) {
                queryPairs.put(key, new LinkedList<String>());
            }

            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), CHAR_ENCODING) : null;
            queryPairs.get(key).add(value);
        }

        return new QueryParameters(queryPairs);
    }
}

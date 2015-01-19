package org.backmeup.plugin.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;

public class PluginUtils {
    public static class QueryParameters {
        private Map<String, List<String>> query_pairs;
        
        public QueryParameters(Map<String, List<String>> query_pairs) {
            this.query_pairs = query_pairs;
        }
        
        public String getParameter(String key) {
            List<String> values = this.query_pairs.get(key);
            if (values == null || values.size() < 1) {
                return null;
            }
            return values.get(0);
        }
        
        public List<String> getMultivalueParameter(String key) {
            List<String> values = this.query_pairs.get(key);
            if (values == null || values.size() < 1) {
                return null;
            }
            return values;
        }
        
    }
    
    public static QueryParameters splitQuery(URL url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        
        for (String pair : url.getQuery().split("&")) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        
        return new QueryParameters(query_pairs);
    }
    
    public static QueryParameters splitQuery(String queryParams) throws UnsupportedEncodingException, MalformedURLException {
        if(queryParams == null) {
            throw new NullPointerException();
        }
        return splitQuery(new URL(queryParams));
    }
}

package org.backmeup.service.client.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.service.client.BackmeupService;
import org.backmeup.service.client.model.auth.AuthInfo;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackmeupServiceClient implements BackmeupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackmeupServiceClient.class);

    private static final int DEFAULT_PORT = 80;

    private final String scheme;

    private final String host;

    private final String basePath;

    private String accessToken;

    // Constructors -----------------------------------------------------------

    public BackmeupServiceClient(String scheme, String host, String basePath, String accessToken) {
        this.scheme = scheme;
        this.host = host;
        this.basePath = basePath;
        this.accessToken = accessToken;
    }

    public BackmeupServiceClient(String scheme, String host, String basePath) {
        this.scheme = scheme;
        this.host = host;
        this.basePath = basePath;
    }
    
    public BackmeupServiceClient(String path) {
        try {
            URIBuilder uriBuilder = new URIBuilder(path);
            URI uri = uriBuilder.build();
            
            this.scheme = uri.getScheme();
            if(uri.getPort() == -1) {
                this.host = uri.getHost(); 
            }else {
                this.host = uri.getHost() + ":" + uri.getPort();
            }
            this.basePath = uri.getPath();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Path is not valid: " + path, e);
        }
    }

    // Properties -------------------------------------------------------------

    public String getAccessToken() {
        return accessToken;
    }

    public Boolean isAuthenticated() {
        return !accessToken.isEmpty();
    }

    // Public methods ---------------------------------------------------------

    @Override
    public AuthInfo authenticate(String username, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        Result r = execute("/authenticate", ReqType.GET, params, null, null);
        if (r.response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new BackMeUpException("Failed to authenticate user: " + r.content);
        }

        try {
            ObjectMapper mapper = createJsonMapper();
            AuthInfo authInfo = mapper.readValue(r.content, AuthInfo.class);
            accessToken = authInfo.getAccessToken();
            return authInfo;
        }  catch (IOException e) {
            LOGGER.error("", e);
            throw new BackMeUpException("Bad JSON in auth info: " + e);
        }
    }

    @Override
    public BackupJobDTO getBackupJob(Long jobId) {
        ensureAuthenticated();

        Map<String, String> params = new HashMap<>();
        params.put("expandUser", "true");
        params.put("expandToken", "true");
        params.put("expandProfiles", "true");
        params.put("expandProtocol", "true");

        Result r = execute("/backupjobs/" + jobId, ReqType.GET, params, null, accessToken);
        if (r.response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new BackMeUpException("Failed to retrieve BackupJob: " + r.content);
        }
        LOGGER.debug("getBackupJob: " + r.content);

        try {
            ObjectMapper mapper = createJsonMapper();
            return mapper.readValue(r.content, BackupJobDTO.class);
        }  catch (IOException e) {
            LOGGER.error("", e);
            throw new BackMeUpException("Failed to retrieve BackupJob: " + e);
        }
    }

    @Override
    public BackupJobDTO updateBackupJob(BackupJobDTO backupJob) {	
        ensureAuthenticated();

        try {
            ObjectMapper mapper = createJsonMapper();
            String json = mapper.writeValueAsString(backupJob);

            Result r = execute("/backupjobs/" + backupJob.getJobId(), ReqType.PUT, null, json, accessToken);
            if (r.response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new BackMeUpException("Failed to update BackupJob: " + r.content);
            }

            LOGGER.debug("saveBackupJob: " + r.content);
            return mapper.readValue(r.content, BackupJobDTO.class);

        } catch (IOException e) {
            LOGGER.error("", e);
            throw new BackMeUpException("Failed to update BackupJob: " + e);
        }
    }

    // Private methods --------------------------------------------------------

    private void ensureAuthenticated() {
        if (accessToken.isEmpty()) {
            throw new IllegalStateException(
                    "Client is not authenticated, set access token or call authenticate");
        }
    }

    private HttpClient createClient() {
        return HttpClientBuilder.create().build();
    }

    private URI createRequestURI(String path, Map<String, String> queryParams) {
        int rPort = DEFAULT_PORT;

        String rPath = basePath + path;
        String rHost = host;
        if (host.contains(":")) {
            String[] sp = host.split(":");
            rHost = sp[0];
            try {
                rPort = Integer.parseInt(sp[1]);
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
        }

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(scheme).setHost(rHost).setPort(rPort).setPath(rPath);

        if(queryParams != null && !queryParams.isEmpty()) {
            for(Entry<String, String> param : queryParams.entrySet()) {
                uriBuilder.addParameter(param.getKey(), param.getValue());
            }
        }

        try {
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            LOGGER.error("", e);
            throw new BackMeUpException(e);
        }
    }

    private Result execute(String path, ReqType type, Map<String, String> queryParams, String jsonParams, String authToken) {
        HttpClient client = createClient();

        try {
            URI registerUri = createRequestURI(path, queryParams);
            HttpUriRequest request;

            switch (type) {
            case PUT:
                HttpPut put = new HttpPut(registerUri);
                if (jsonParams != null && !jsonParams.isEmpty()) {
                    StringEntity entity = new StringEntity(jsonParams, StandardCharsets.UTF_8);					
                    put.setEntity(entity);

                    put.setHeader("Accept", "application/json");
                    put.setHeader("Content-type", "application/json");
                }
                request = put;
                break;
            case DELETE:
                request = new HttpDelete(registerUri);
                break;
            case GET:
                request = new HttpGet(registerUri);
                break;
            default:
                HttpPost post = new HttpPost(registerUri);
                if (jsonParams != null && !jsonParams.isEmpty()) {
                    StringEntity entity = new StringEntity(jsonParams, "UTF-8");					
                    post.setEntity(entity);

                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");
                }
                request = post;
                break;
            }

            if(authToken != null && !authToken.isEmpty()) {
                request.setHeader("Authorization", authToken);
            }

            HttpResponse response = client.execute(request);
            Result r = new Result();
            r.response = response;
            if (response.getEntity() != null) {
                try {
                    r.content = new Scanner(response.getEntity().getContent()).useDelimiter("\\A").next();
                } catch (NoSuchElementException nee) {
                    LOGGER.debug("", nee);
                }
            }
            return r;
        } catch (ClientProtocolException e) {
            throw new BackMeUpException(e);
        } catch (IOException e) {
            throw new BackMeUpException(e);
        }
    }

    private ObjectMapper createJsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
        objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(Feature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        return objectMapper;
    }

    // Private classes and enums ----------------------------------------------

    private static class Result {
        public HttpResponse response;

        public String content;
    }

    private enum ReqType {
        GET, DELETE, PUT, POST
    }
}

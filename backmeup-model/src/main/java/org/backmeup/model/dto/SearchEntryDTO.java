package org.backmeup.model.dto;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SearchEntryDTO {

    private String fileId;
    private String ownerId; //the user/sharingpartner that provides this record 
    private boolean isSharing;
    private Date timeStamp;
    private String title;
    private String type;
    private String thumbnailUrl;
    private String downloadUrl;
    private String datasource;
    private String datasink;
    //private String datasourceId;
    private String jobName;
    private String preview;
    private Map<String, String> properties;
    private Map<String, String> metadata;

    public String getFileId() {
        return this.fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDatasource() {
        return this.datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasink() {
        return this.datasink;
    }

    public void setDatasink(String datasink) {
        this.datasink = datasink;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getPreview() {
        return this.preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getJobName() {
        return this.jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String userId) {
        this.ownerId = userId;
    }

    public boolean getIsSharing() {
        return this.isSharing;
    }

    public void setIsSharing(boolean isSharing) {
        this.isSharing = isSharing;
    }
    /*public static class KeyValue {

        private String key;
        private String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }*/

}

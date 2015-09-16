package org.backmeup.plugin.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.plugin.util.GeoMetadataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metainfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(Metainfo.class);

    private static final String PROP_DESTINATION = "destination";
    private static final String PROP_TYPE = "type";
    private static final String PROP_SOURCE = "source";
    private static final String PROP_BACKUP_TIME = "backupedAt";
    private static final String PROP_PARENT = "parent";
    private static final String PROP_ID = "id";
    private static final String PROP_MODIFIED = "modified";
    private static final String PROP_CREATED = "created";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss z";

    private static final String PROP_LOCACTION_NAME = "locName";
    private static final String PROP_LOCACTION_LATITUDE = "locLat";
    private static final String PROP_LOCACTION_LONGITUDE = "locLon";
    private static final String PROP_LOCACTION_CITY = "locCity";
    private static final String PROP_LOCACTION_COUNTRY = "locCountry";
    private static final String PROP_AUTHOR_NAME = "authorName";

    private final Properties metainfo = new Properties();

    public void setModified(Date modifiedDate) {
        final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        this.metainfo.setProperty(PROP_MODIFIED, formatter.format(modifiedDate));
    }

    public void setBackupDate(Date backupTime) {
        final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        this.metainfo.setProperty(PROP_BACKUP_TIME, formatter.format(backupTime));
    }

    public void setCreated(Date createdDate) {
        final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        this.metainfo.setProperty(PROP_CREATED, formatter.format(createdDate));
    }

    public Date getCreated() {
        return parseDate(getAttribute(PROP_CREATED));
    }

    public Date getBackupDate() {
        return parseDate(getAttribute(PROP_BACKUP_TIME));
    }

    public void setSource(String source) {
        this.metainfo.setProperty(PROP_SOURCE, source);
    }

    public String getSource() {
        return this.metainfo.getProperty(PROP_SOURCE);
    }

    public void setType(String type) {
        this.metainfo.setProperty(PROP_TYPE, type);
    }

    public String getType() {
        return this.metainfo.getProperty(PROP_TYPE);
    }

    public void setDestination(String destination) {
        this.metainfo.setProperty(PROP_DESTINATION, destination);
    }

    public String getDestination() {
        return this.metainfo.getProperty(PROP_DESTINATION);
    }

    public void setId(String id) {
        this.metainfo.setProperty(PROP_ID, id);
    }

    public void setParent(String parentId) {
        this.metainfo.setProperty(PROP_PARENT, parentId);
    }

    public String getParent() {
        return this.metainfo.getProperty(PROP_PARENT);
    }

    private Date parseDate(String input) {
        if (input != null) {
            try {
                final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
                return formatter.parse(input);
            } catch (ParseException e) {
                LOGGER.error("", e);
            }
        }
        return null;
    }

    public Date getModified() {
        return parseDate(getAttribute(PROP_MODIFIED));
    }

    public String getId() {
        return getAttribute(PROP_ID);
    }

    public void setLocationName(String locationName) {
        this.metainfo.setProperty(PROP_LOCACTION_NAME, locationName);
    }

    public String getLocationName() {
        return this.metainfo.getProperty(PROP_LOCACTION_NAME);
    }

    public void setLocationLatitude(String locationLatitude) {
        if (GeoMetadataConverter.isValidGeoCoordinate(locationLatitude)) {
            this.metainfo.setProperty(PROP_LOCACTION_LATITUDE, locationLatitude);
        } else {
            try {
                Double lat = GeoMetadataConverter.extractAndConvertGeoCoordinates(locationLatitude);
                this.metainfo.setProperty(PROP_LOCACTION_LATITUDE, lat + "");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public Double getLocationLatitude() {
        if (this.metainfo.getProperty(PROP_LOCACTION_LATITUDE) != null) {
            return Double.valueOf(this.metainfo.getProperty(PROP_LOCACTION_LATITUDE));
        }
        return -1D;
    }

    public void setLocationLongitude(String locationLongitude) {
        if (GeoMetadataConverter.isValidGeoCoordinate(locationLongitude)) {
            this.metainfo.setProperty(PROP_LOCACTION_LONGITUDE, locationLongitude);
        } else {
            try {
                Double longitude = GeoMetadataConverter.extractAndConvertGeoCoordinates(locationLongitude);
                this.metainfo.setProperty(PROP_LOCACTION_LONGITUDE, longitude + "");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public Double getLocationLongitude() {
        if (this.metainfo.getProperty(PROP_LOCACTION_LONGITUDE) != null) {
            return Double.valueOf(this.metainfo.getProperty(PROP_LOCACTION_LONGITUDE));
        }
        return -1D;
    }

    public void setLocationCity(String locationCity) {
        this.metainfo.setProperty(PROP_LOCACTION_CITY, locationCity);
    }

    public String getLocationCity() {
        return this.metainfo.getProperty(PROP_LOCACTION_CITY);
    }

    public void setLocationCountry(String locationCountry) {
        this.metainfo.setProperty(PROP_LOCACTION_COUNTRY, locationCountry);
    }

    public String getLocationCountry() {
        return this.metainfo.getProperty(PROP_LOCACTION_COUNTRY);
    }

    public void setAuthorName(String authorName) {
        this.metainfo.setProperty(PROP_AUTHOR_NAME, authorName);
    }

    public String getAuthorName() {
        return this.metainfo.getProperty(PROP_AUTHOR_NAME);
    }

    public void setAttribute(String key, String value) {
        this.metainfo.setProperty(key, value);
    }

    public String getAttribute(String key) {
        return this.metainfo.getProperty(key);
    }

    public Properties getAttributes() {
        return (Properties) this.metainfo.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<Object, Object> attributes : this.metainfo.entrySet()) {
            sb.append(attributes.getKey()).append(" = ").append(attributes.getValue()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (this.metainfo.getProperty(PROP_ID) == null ? 0 : this.metainfo.getProperty(PROP_ID).hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Metainfo other = (Metainfo) obj;
        if (this.metainfo == null) {
            return other.metainfo == null;
        }
        if (this.metainfo.getProperty(PROP_ID) == null) {
            return other.metainfo != null && other.metainfo.getProperty(PROP_ID) == null;
        }
        return other.metainfo != null && this.metainfo.getProperty(PROP_ID).equals(other.metainfo.getProperty(PROP_ID));
    }
}

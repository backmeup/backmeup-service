package org.backmeup.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.utils.Serialization;

/**
 * 
 * The AuthData class represents authentication information for a profile. The
 * plugin of the respective profile specifies how this auth data has to be
 * structured and what it should contain.
 * 
 * The class has been annotated with JPA specific annotations.
 */
@Entity
public class AuthData {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String pluginId;

    // The username that has been used for a certain profile, 
    // e.g. the dropbox username or facebook username
    private String identification;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private BackMeUpUser user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Transient
    //	@ElementCollection(fetch = FetchType.EAGER)
    //	@MapKeyColumn(name = "authdata_key")
    //	@Column(name = "authdata_value", columnDefinition="text")
    //	@CollectionTable(name = "AuthDataProperties", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> properties;

    public AuthData() {

    }

    public AuthData(String name, String pluginId, BackMeUpUser user) {
        this(name, pluginId, user, new HashMap<String, String>());
    }

    public AuthData(String name, String pluginId, BackMeUpUser user, Map<String, String> properties) {
        super();
        this.name = name;
        this.pluginId = pluginId;
        this.user = user;
        this.properties = properties;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public BackMeUpUser getUser() {
        return user;
    }

    public void setUser(BackMeUpUser user) {
        this.user = user;
    }

    public Date getCreated() {
        if (this.created == null) {
            return null;
        }
        return (Date)created.clone();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public Date getModified() {
        if (this.modified == null) {
            return null;
        }
        return (Date) modified.clone();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modified = new Date();
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }

    public String getPropertiesAsEncodedString() {
        try {
            return Serialization.getObjectAsEncodedString(properties);
        } catch (Exception e) {
            throw new BackMeUpException("Cannot serialize auth data properties", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void setPropertiesFromEncodedString(String properpies) {
        try {
            this.properties = Serialization.getEncodedStringAsObject(properpies, HashMap.class);
        } catch (Exception e) {
            throw new BackMeUpException("Cannot deserialize auth data properties", e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s: id=%d Name=%s Plugin=%s", "AuthData", id, name, pluginId);
    }

    /**
     * Attempt to establish identity based on id if both exist. 
     * If either id does not exist use Object.equals().
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof AuthData)) {
            return false;
        }
        AuthData entity = (AuthData) other;
        if (id == null || entity.getId() == null) {
            return false;
        }
        return id.equals(entity.getId());
    }

    /**
     * Use ID if it exists to establish hash code, otherwise fall back to
     * Object.hashCode(). 
     */
    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        return 17 * id.hashCode();
    }
}

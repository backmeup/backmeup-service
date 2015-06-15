package org.backmeup.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.model.utils.Serialization;

/**
 * 
 * The Profile class represents a configuration for a certain plugin
 * (datasource, -sink or action).
 * 
 * The class has been annotated with JPA specific annotations.
 * 
 * @author fschoeppl
 * 
 */
@Entity
public class Profile implements Serializable {
    private static final long serialVersionUID = 1606660919647823719L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, optional = false)
    private BackMeUpUser user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    private String pluginId;

    @Enumerated(EnumType.STRING)
    private PluginType pluginType;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, optional = true)
    private AuthData authData;

    @Transient
//    @ElementCollection(fetch=FetchType.EAGER)
//    @OrderColumn(name = "properties_index")
    private Map<String, String> properties;

    @Transient
//    @ElementCollection(fetch=FetchType.EAGER)
//    @OrderColumn(name = "options_index")
    private List<String> options;

    public Profile() {

    }

    public Profile(BackMeUpUser user, String pluginId, PluginType pluginType) {
        this(null, user, pluginId, pluginType);
    }

    public Profile(Long id, BackMeUpUser user, String pluginId, PluginType pluginType) {
        this.id = id;
        this.user = user;
        this.pluginId = pluginId;
        this.pluginType = pluginType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long profileId) {
        this.id = profileId;
        this.modified = new Date();
    }

    public BackMeUpUser getUser() {
        return user;
    }

    public void setUser(BackMeUpUser user) {
        this.user = user;
        this.modified = new Date();
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.modified = new Date();
        this.pluginId = pluginId;
    }

    public PluginType getType() {
        return pluginType;
    }

    public void setType(PluginType pluginType) {
        this.modified = new Date();
        this.pluginType = pluginType;
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

    public AuthData getAuthData() {
        return authData;
    }

    public void setAuthData(AuthData authData) {
        if(this.pluginId == null) {
            throw new IllegalStateException("Cannot set auth data. Profile has to be associated with a plugin");
        }

        if(!authData.getPluginId().equals(this.pluginId)){
            throw new IllegalArgumentException("Auth data is associated with a different plugin");
        }

        if (!authData.getUser().getUserId().equals(this.getUser().getUserId())) {
            throw new IllegalArgumentException("Auth data is associated with a different user");
        }

        this.authData = authData;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, String value) {
        if(this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void addOption(String option) {
        if(this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(option);
    }

    public String getPropertiesAndOptionsAsEncodedString() {
        try {            
            Map<String, String> props = new HashMap<String, String>();
            if (properties != null) {
                props.putAll(properties);
            }
            props.put(String.valueOf(Profile.serialVersionUID), Serialization.getObjectAsEncodedString(options));
            return Serialization.getObjectAsEncodedString(props);
        } catch (Exception e) {
            throw new BackMeUpException("Cannot serialize profile data", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void setPropertiesAndOptionsFromEncodedString(String properpies) {
        try {
            Map<String, String> props = Serialization.getEncodedStringAsObject(properpies, HashMap.class);
            String encodedOptions = props.get(String.valueOf(Profile.serialVersionUID));
            this.options = Serialization.getEncodedStringAsObject(encodedOptions, ArrayList.class);
            props.remove(String.valueOf(Profile.serialVersionUID));
            if (!props.isEmpty()) {
                this.properties = props;
            }
        } catch (Exception e) {
            throw new BackMeUpException("Cannot deserialize profile data", e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s: id=%d Plugin=%s Type=%s", "Profile", id, pluginId, pluginType);
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
        if (!(other instanceof Profile)) {
            return false;
        }
        Profile entity = (Profile) other;
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
        return 89 * id.hashCode();
    }
}

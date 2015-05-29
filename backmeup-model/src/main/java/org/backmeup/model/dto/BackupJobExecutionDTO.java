package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.constants.JobExecutionStatus;
import org.backmeup.model.spi.PluginDescribable.PluginType;

@XmlRootElement
public class BackupJobExecutionDTO {
    private Long id;
    private String name;
    private JobExecutionStatus status;
    
    private Date created;
    private Date modified;
    private Date start;
    private Date end;

    private Long jobId;
    private UserDTO user;
    private String token;

    private PluginProfileDTO source;
    private List<PluginProfileDTO> actions;
    private PluginProfileDTO sink;

    public BackupJobExecutionDTO() {

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

    public JobExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public PluginProfileDTO getSource() {
        return source;
    }

    public void setSource(PluginProfileDTO source) {
        if ((source != null) && 
            (source.getProfileType() != PluginType.Source) && 
            (source.getProfileType() != PluginType.SourceSink)) {
            throw new IllegalArgumentException("Only profiles from a source plugin can be assigned, but type is: " + source.getProfileType());
        }
        this.source = source;
    }

    public List<PluginProfileDTO> getActions() {
        return actions;
    }

    public void setActions(List<PluginProfileDTO> actions) {
        this.actions = actions;
    }

    public void addAction(PluginProfileDTO action) {
        if (action.getProfileType() != PluginType.Action) {
            throw new IllegalArgumentException("Only profiles from action plugins can be assigned, but type is: " + action.getProfileType());
        }

        if (actions == null) {
            actions = new ArrayList<>();
        }
        this.actions.add(action);
    }

    public PluginProfileDTO getSink() {
        return sink;
    }

    public void setSink(PluginProfileDTO sink) {
        if ((sink != null) && 
            (sink.getProfileType() != PluginType.Sink) && 
            (sink.getProfileType() != PluginType.SourceSink)) {
            throw new IllegalArgumentException("Only profiles from a sink plugin can be assigned, but type is: " + source.getProfileType());
        }
        this.sink = sink;
    }
}

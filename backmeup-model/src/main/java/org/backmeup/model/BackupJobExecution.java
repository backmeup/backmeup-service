package org.backmeup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.backmeup.model.constants.BackupJobStatus;
import org.backmeup.model.spi.PluginDescribable.PluginType;

/**
 * Domain object representing a uniquely identifiable execution 
 * of a BackupJob.
 * 
 */
@Entity
public class BackupJobExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    
    private String name;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    
    @Enumerated(EnumType.STRING)
    private BackupJobStatus status;
    
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private BackMeUpUser user;
    
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private BackupJob backupJob;
    
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Profile sourceProfile;
    
    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private List<Profile> actionProfiles = new ArrayList<>();
    
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Profile sinkProfile;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Token token;
    
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "job")
//    private Set<JobProtocol> jobProtocol = new HashSet<>();
    
    // ExecutionContext
    // failureExceptions
    
    public BackupJobExecution() {

    }

    public BackupJobExecution(String name) {
        this.name = name;
    }

    public BackupJobExecution(BackupJob job) {
        this.name = job.getJobName() + " Execution";
        this.createTime = new Date();
        this.lastUpdated = createTime;
        this.status = BackupJobStatus.queued;
        this.user = job.getUser();
        this.backupJob = job;
        this.sourceProfile = job.getSourceProfile();
        this.sinkProfile = job.getSinkProfile();
        for (Profile action : job.getActionProfiles()) {
            actionProfiles.add(action);
        }
        this.token = job.getToken();
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
    
    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Date getStartTime() {
        return (Date) startTime.clone();
    }

    public void setStartTime(Date startTime) {
        this.startTime = (Date) startTime.clone();
    }

    public Date getEndTime() {
        return (Date) endTime.clone();
    }

    public void setEndTime(Date endTime) {
        this.endTime = (Date) endTime.clone();
    }

    public Date getLastUpdated() {
        return (Date) lastUpdated.clone();
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = (Date) lastUpdated.clone();
    }

    public BackupJobStatus getStatus() {
        return status;
    }

    public void setStatus(BackupJobStatus status) {
        this.status = status;
    }

    public BackMeUpUser getUser() {
        return user;
    }

    public void setUser(BackMeUpUser user) {
        this.user = user;
    }

    public BackupJob getBackupJob() {
        return backupJob;
    }

    public void setBackupJob(BackupJob backupJob) {
        this.backupJob = backupJob;
    }

    public Profile getSourceProfile() {
        return sourceProfile;
    }

    public void setSourceProfile(Profile sourceProfile) {
        if (sourceProfile.getType() != PluginType.Source) {
            throw new IllegalArgumentException(
                    "Source profile must be of type Source, but is of type "
                            + sourceProfile.getType());
        }
        this.sourceProfile = sourceProfile;
    }

    public List<Profile> getActionProfiles() {
        return actionProfiles;
    }

    public void setActionProfiles(List<Profile> actionProfiles) {
        for (Profile actionProfile : actionProfiles) {
            if (actionProfile.getType() != PluginType.Action) {
                throw new IllegalArgumentException("Action profile ("
                        + actionProfile.getId()
                        + ") must be of type Action, but is of type "
                        + actionProfile.getType());
            }
        }
        this.actionProfiles = actionProfiles;
    }

    public Profile getSinkProfile() {
        return sinkProfile;
    }

    public void setSinkProfile(Profile sinkProfile) {
        if (sinkProfile.getType() != PluginType.Sink) {
            throw new IllegalArgumentException(
                    "Sink profile must be of type Sink, but is of type "
                            + sinkProfile.getType());
        }
        this.sinkProfile = sinkProfile;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

//    public Set<JobProtocol> getJobProtocol() {
//        return jobProtocol;
//    }
//
//    public void setJobProtocol(Set<JobProtocol> jobProtocol) {
//        this.jobProtocol = jobProtocol;
//    }

    @Override
    public String toString() {
        return String.format("%s: id=%d Name=%s", "BackupJobExecution", id, name);
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
        if (!(other instanceof BackupJobExecution)) {
            return false;
        }
        BackupJobExecution entity = (BackupJobExecution) other;
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
        return 139 * id.hashCode();
    }
}

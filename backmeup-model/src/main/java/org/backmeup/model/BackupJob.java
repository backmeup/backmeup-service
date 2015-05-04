package org.backmeup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.backmeup.model.constants.JobFrequency;
import org.backmeup.model.constants.JobStatus;
import org.backmeup.model.spi.PluginDescribable.PluginType;

/**
 * The BackupJob class contains all necessary data to perform the backup job. It
 * must be created by the org.backmeup.job.JobManager's implementation.
 * 
 * 
 * @author fschoeppl
 * 
 */
@Entity
public class BackupJob {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;

	private String jobName;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private BackMeUpUser user;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private Profile sourceProfile;

	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private List<Profile> actionProfiles = new ArrayList<>();

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private Profile sinkProfile;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Token token;

//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "job")
//	private final Set<JobProtocol> jobProtocols = new HashSet<>();

	@Enumerated(EnumType.STRING)
	private JobFrequency jobFrequency;

	private long delay;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date nextExecutionTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastSuccessful;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastFailed;

	@Enumerated(EnumType.STRING)
	private JobStatus status;

	private UUID validScheduleID = null;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdatedTime;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "backupJob")
    private Set<BackupJobExecution> jobExecutions = new HashSet<>();

	public BackupJob() {
		super();
	}

	public BackupJob(BackMeUpUser user, String name, Profile sourceProfile,
			Profile sinkProfile, List<Profile> actionProfiles, Date start, JobFrequency frequency) {
		this.user = user;
		this.jobName = name;
		this.startTime = start;
		this.jobFrequency = frequency;
		this.delay = jobFrequency.getDelayTime();
	    setSourceProfile(sourceProfile);
	    setSinkProfile(sinkProfile);
	    setActionProfiles(actionProfiles);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public BackMeUpUser getUser() {
		return user;
	}

	public void setUser(BackMeUpUser user) {
		this.user = user;
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

	public List<Profile> getActionProfiles() {
		return this.actionProfiles;
	}

	@Deprecated
	public List<Profile> getSortedRequiredActions() {
		return this.actionProfiles;
	}

	public void setActionProfiles(List<Profile> actionProfiles) {
		for (Profile actionProfile : actionProfiles) {
			if (actionProfile.getType() != PluginType.Action) {
				throw new IllegalArgumentException("Action profile (" + actionProfile.getId()
				        + ") must be of type Action, but is of type " + actionProfile.getType());
			}
		}
		this.actionProfiles = actionProfiles;
	}

	public Date getStartTime() {
        if (this.startTime == null) {
            return null;
        }
		return (Date) startTime.clone();
	}

	public void setStartTime(Date start) {
		this.startTime = (Date) start.clone();
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public Date getCreateTime() {
        if (this.createTime == null) {
            return null;
        }
		return (Date)createTime.clone();
	}
	
    @PrePersist
    protected void onCreate() {
        this.createTime = new Date();
    }

    public Date getLastUpdatedTime() {
        if (this.lastUpdatedTime == null) {
            return null;
        }
        return (Date) lastUpdatedTime.clone();
    }
    
    @PreUpdate
    protected void onUpdate() {
      this.lastUpdatedTime = new Date();
    }

//	public JobProtocol lastProtocol() {
//		JobProtocol last = null;
//		for (JobProtocol jp : jobProtocols) {
//			if (last == null
//					|| jp.getExecutionTime().compareTo(last.getExecutionTime()) > 0) {
//				last = jp;
//			}
//		}
//		return last;
//	}

	public Date getNextExecutionTime() {
        if (this.nextExecutionTime == null) {
            return null;
        }
		return (Date) nextExecutionTime.clone();
	}

	public void setNextExecutionTime(Date nextExecutionTime) {
		this.nextExecutionTime = (Date) nextExecutionTime.clone();
	}

//	public boolean isReschedule() {
//		return reschedule;
//	}
//
//	public void setReschedule(boolean reschedule) {
//		this.reschedule = reschedule;
//	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public Date getLastSuccessful() {
        if (this.lastSuccessful == null) {
            return null;
        }
		return (Date) lastSuccessful.clone();
	}

	public void setLastSuccessful(Date lastSuccessful) {
		this.lastSuccessful = (Date) lastSuccessful.clone();
	}

	public Date getLastFailed() {
        if (this.lastFailed == null) {
            return null;
        }
		return (Date) lastFailed.clone();
	}

	public void setLastFailed(Date lastFailed) {
		this.lastFailed = lastFailed;
	}
	
    public boolean isActive() {
        return status == JobStatus.ACTIVE;
    }

    public JobFrequency getJobFrequency() {
        return jobFrequency;
    }

    public void setJobFrequency(JobFrequency jobFrequency) {
        this.jobFrequency = jobFrequency;
        this.delay = jobFrequency.getDelayTime();
    }

    public Set<BackupJobExecution> getJobExecutions() {
        return jobExecutions;
    }

    public void setJobExecutions(Set<BackupJobExecution> jobExecutions) {
        for (BackupJobExecution jobExecution : jobExecutions) {
            if (jobExecution.getBackupJob().getId() != this.getId()) {
                throw new IllegalArgumentException(String.format(
                        "JobExecution with id '%s' is attached to another job",
                        jobExecution.getBackupJob().getId()));
            }
        }
        this.jobExecutions = jobExecutions;
    }
    

	public UUID getValidScheduleID() {
		return validScheduleID;
	}

	public void setValidScheduleID(UUID validScheduleID) {
		this.validScheduleID = validScheduleID;
	}
    
    @Override
    public String toString() {
        return String.format("%s: id=%d Name=%s", "BackupJob", id, jobName);
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
        if (!(other instanceof BackupJob)) {
            return false;
        }
        BackupJob entity = (BackupJob) other;
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
        return 59 * id.hashCode();
    }
}

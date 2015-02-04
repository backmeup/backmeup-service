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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.backmeup.model.constants.BackupJobStatus;
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

	private String jobTitle;

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

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "job")
	private final Set<JobProtocol> jobProtocols = new HashSet<>();

	private String timeExpression;

	private long delay;

	@Temporal(TemporalType.TIMESTAMP)
	private Date start;

	@Temporal(TemporalType.TIMESTAMP)
	private Date nextExecutionTime;

	private boolean reschedule;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastSuccessful;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastFailed;

	@Enumerated(EnumType.STRING)
	private BackupJobStatus status;

	private boolean onHold = false;

	private UUID validScheduleID = null;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;

	public BackupJob() {
		super();
	}

	public BackupJob(BackMeUpUser user, Profile sourceProfile,
			Profile sinkProfile, List<Profile> actionProfiles, Date start,
			long delay, String jobTitle, boolean reschedule) {
		this.user = user;
		setSourceProfile(sourceProfile);
		setSinkProfile(sinkProfile);
		setActionProfiles(actionProfiles);
		this.actionProfiles = actionProfiles;
		this.start = start;
		this.delay = delay;

		this.created = new Date();
		this.modified = this.created;
		this.jobTitle = jobTitle;
		this.reschedule = reschedule;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.modified = new Date();
		this.id = id;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.modified = new Date();
		this.jobTitle = jobTitle;
	}

	public BackMeUpUser getUser() {
		return user;
	}

	public void setUser(BackMeUpUser user) {
		this.modified = new Date();
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
		this.modified = new Date();
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
		this.modified = new Date();
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
		this.modified = new Date();
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.modified = new Date();
		this.start = start;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.modified = new Date();
		this.delay = delay;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.modified = new Date();
		this.token = token;
	}

	public Date getCreated() {
		return created;
	}

	public Date getModified() {
		return modified;
	}

	public JobProtocol lastProtocol() {
		JobProtocol last = null;
		for (JobProtocol jp : jobProtocols) {
			if (last == null
					|| jp.getExecutionTime().compareTo(last.getExecutionTime()) > 0) {
				last = jp;
			}
		}
		return last;
	}

	public Date getNextExecutionTime() {
		return nextExecutionTime;
	}

	public void setNextExecutionTime(Date nextExecutionTime) {
		this.nextExecutionTime = nextExecutionTime;
	}

	public boolean isReschedule() {
		return reschedule;
	}

	public void setReschedule(boolean reschedule) {
		this.reschedule = reschedule;
	}

	public BackupJobStatus getStatus() {
		return status;
	}

	public void setStatus(BackupJobStatus status) {
		this.status = status;
	}

	public Date getLastSuccessful() {
		return lastSuccessful;
	}

	public void setLastSuccessful(Date lastSuccessful) {
		this.lastSuccessful = lastSuccessful;
	}

	public Date getLastFailed() {
		return lastFailed;
	}

	public void setLastFailed(Date lastFailed) {
		this.lastFailed = lastFailed;
	}

	public boolean isOnHold() {
		return onHold;
	}

	public void setOnHold(boolean onHold) {
		this.onHold = onHold;
	}

	public String getTimeExpression() {
		return timeExpression;
	}

	public void setTimeExpression(String timeExpression) {
		this.timeExpression = timeExpression;
	}

	public UUID getValidScheduleID() {
		return validScheduleID;
	}

	public void setValidScheduleID(UUID validScheduleID) {
		this.validScheduleID = validScheduleID;
	}
}

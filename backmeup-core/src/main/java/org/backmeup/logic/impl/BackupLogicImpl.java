package org.backmeup.logic.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.JobProtocolDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.logic.BackupLogic;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.JobProtocol;
import org.backmeup.model.JobProtocol.JobProtocolMember;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.ProtocolOverview.Activity;
import org.backmeup.model.ProtocolOverview.Entry;
import org.backmeup.model.Status;
import org.backmeup.model.constants.BackupJobStatus;
import org.backmeup.model.dto.JobProtocolDTO;

@ApplicationScoped
public class BackupLogicImpl implements BackupLogic {

    private static final String JOB_USER_MISSMATCH = "org.backmeup.logic.impl.BusinessLogicImpl.JOB_USER_MISSMATCH";
    private static final String NO_SUCH_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_SUCH_JOB";
    private static final String NO_PROFILE_WITHIN_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_PROFILE_WITHIN_JOB";
    
    @Inject
    private DataAccessLayer dal;

    private final ResourceBundle textBundle = ResourceBundle.getBundle("BackupLogicImpl");
    
    private BackupJobDao getBackupJobDao() {
        return dal.createBackupJobDao();
    }

    private StatusDao getStatusDao() {
        return dal.createStatusDao();
    }

    private JobProtocolDao createJobProtocolDao() {
        return dal.createJobProtocolDao();
    }

    @Override
    public void deleteJobsOf(String username) {
        BackupJobDao jobDao = getBackupJobDao();
        StatusDao statusDao = getStatusDao();
        for (BackupJob job : jobDao.findByUsername(username)) {
            for (Status status : statusDao.findByJobId(job.getId())) {
                statusDao.delete(status);
            }
            jobDao.delete(job);
        }
    }

    @Override
    public BackupJob getExistingJob(Long jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("JobId must not be null");
        }
        BackupJob job = getBackupJobDao().findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
        }
        return job;
    }

    @Override
    public BackupJob getExistingUserJob(Long jobId, String username) {
        BackupJob job = getExistingJob(jobId);
        if (!job.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                    jobId, username));
        }
        return job;
    }

    @Override
    public ActionProfile getJobActionOption(String actionId, Long jobId) {
        BackupJob job = getExistingJob(jobId);
        for (ActionProfile ap : job.getRequiredActions()) {
            if (ap.getActionId().equals(actionId)) {
                return ap;
            }
        }
        throw new IllegalArgumentException(String.format(textBundle.getString(NO_PROFILE_WITHIN_JOB), jobId, actionId));
    }

    @Override
    public void updateJobActionOption(String actionId, Long jobId, Map<String, String> actionOptions) {
        BackupJob job = getExistingJob(jobId);
        for (ActionProfile ap : job.getRequiredActions()) {
            if (ap.getActionId().equals(actionId)) {
                ap.getActionOptions().clear();
                addActionProperties(ap, actionOptions);
            }
        }
    }

    private void addActionProperties(ActionProfile ap, Map<String, String> keyValues) {
        for (Map.Entry<String, String> e : keyValues.entrySet()) {
            ActionProperty aprop = new ActionProperty(e.getKey(), e.getValue());
            aprop.setProfile(ap);
            ap.getActionOptions().add(aprop);
        }
    }

    @Override
    public BackupJob fullJobFor(Long jobId) {
        return getExistingJob(jobId);
    }

    @Override
    public void deleteJob(String username, Long jobId) {
        BackupJob job = getExistingUserJob(jobId, username);

        deleteStatuses(job.getId());

        getBackupJobDao().delete(job);
    }

    private void deleteStatuses(Long jobId) {
        // Delete Job status records first
        StatusDao statusDao = getStatusDao();
        for (Status status : statusDao.findByJobId(jobId)) {
            statusDao.delete(status);
        }
    }

    @Override
    public List<Status> getStatus(String username, Long jobId) {
        BackupJobDao jobDao = getBackupJobDao();
        
        if (jobId == null) {
            List<Status> status = new ArrayList<>();
            BackupJob job = jobDao.findLastBackupJob(username);
            if (job != null) {
                status.addAll(getStatusForJob(job));
            }
            // for (BackupJob job : jobs) {
            //     status.add(getStatusForJob(job));
            // }
            return status;
        }
        
        BackupJob job = getExistingUserJob(jobId, username);
        List<Status> status = new ArrayList<>();
        status.addAll(getStatusForJob(job));
        return status;
    }

    private List<Status> getStatusForJob(final BackupJob job) {
        StatusDao sd = dal.createStatusDao();
        List<Status> status = sd.findLastByJob(job.getUser().getUsername(), job.getId());
        return status;
    }

    @Override
    public List<BackupJob> getBackupJobsOf(String username) {
        return getBackupJobDao().findByUsername(username);
    }

    @Override
    public BackupJob updateRequestFor(Long jobId) {
        return getExistingJob(jobId);
    }

    @Override
    public void updateJob(BackupJob persistentJob, BackupJob updatedJob) {
    	persistentJob.getToken().setTokenId(updatedJob.getToken().getTokenId());
    	persistentJob.getToken().setToken(updatedJob.getToken().getToken());
    	persistentJob.getToken().setBackupdate(updatedJob.getToken().getBackupdate());
    	
    	persistentJob.setStatus(updatedJob.getStatus());
    	
    	// TODO: update fields
    }

    @Override
    public ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to) {
        List<JobProtocol> protocols = createJobProtocolDao().findByUsernameAndDuration(user.getUsername(), from, to);
        ProtocolOverview po = new ProtocolOverview();
        Map<String, Entry> entries = new HashMap<>();
        double totalSize = 0;
        long totalCount = 0;
        for (JobProtocol prot : protocols) {
            totalCount += prot.getTotalStoredEntries();
            for (JobProtocolMember member : prot.getMembers()) {
                Entry entry = entries.get(member.getTitle());
                if (entry == null) {
                    entry = new Entry(member.getTitle(), 0, member.getSpace());
                    entries.put(member.getTitle(), entry);
                } else {
                    entry.setAbsolute(entry.getAbsolute() + member.getSpace());
                }
                totalSize += member.getSpace();
            }
            po.getActivities().add(new Activity(prot.getJob().getJobTitle(), prot.getExecutionTime()));
        }

        for (Entry entry : entries.values()) {
            entry.setPercent(100 * entry.getAbsolute() / totalSize);
            po.getStoredAmount().add(entry);
        }
        po.setTotalCount(totalCount+"");
        // TODO Determine format of bytes (currently MB)
        po.setTotalStored(totalSize / 1024 / 1024 +" MB");
        po.setUser(user.getUserId());
        return po;
    }

    @Override
    public void createJobProtocol(BackMeUpUser user, BackupJob job, JobProtocolDTO jobProtocol) {
        JobProtocolDao jpd = createJobProtocolDao();
        
        JobProtocol protocol = new JobProtocol();
        protocol.setUser(user);
        protocol.setJob(job);
        protocol.setSuccessful(jobProtocol.isSuccessful());
        
//        for(JobProtocolMemberDTO pm : jobProtocol.getMembers()) {
//            protocol.addMember(new JobProtocolMember(protocol, pm.getTitle(), pm.getSpace()));
//        }
        
        if (protocol.isSuccessful()) {
            job.setLastSuccessful(protocol.getExecutionTime());
            job.setStatus(BackupJobStatus.successful);
        } else {
            job.setLastFailed(protocol.getExecutionTime());
            job.setStatus(BackupJobStatus.error);
        }
        
        jpd.save(protocol);
    }

    @Override
    public void deleteProtocolsOf(String username) {
        createJobProtocolDao().deleteByUsername(username);
    }

}

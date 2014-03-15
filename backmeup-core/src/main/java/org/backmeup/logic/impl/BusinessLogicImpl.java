package org.backmeup.logic.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.JobProtocolDao;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.SearchResponseDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.dal.UserDao;
import org.backmeup.job.JobManager;
import org.backmeup.job.impl.rabbitmq.RabbitMQJobReceiver;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.UserRegistration;
import org.backmeup.logic.impl.helper.BackUpJobConverter;
import org.backmeup.logic.impl.helper.BackUpJobCreationHelper;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.FileItem;
import org.backmeup.model.JobProtocol;
import org.backmeup.model.JobProtocol.JobProtocolMember;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.ProtocolOverview.Activity;
import org.backmeup.model.ProtocolOverview.Entry;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Status;
import org.backmeup.model.Token;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.ExecutionTime;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.dto.JobUpdateRequest;
import org.backmeup.model.dto.SourceProfileEntry;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PasswordTooShortException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.PluginUnavailableException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.actions.encryption.EncryptionDescribable;
import org.backmeup.plugin.api.actions.filesplitting.FilesplittDescribable;
import org.backmeup.plugin.api.actions.indexing.ElasticSearchIndexClient;
import org.backmeup.plugin.api.actions.indexing.IndexDescribable;
import org.backmeup.plugin.api.actions.indexing.IndexUtils;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the BusinessLogic interface by delegating most operations to
 * following layers: - DataAccessLayer - JobManager - PluginLayer
 * 
 * If an error occurs within a method an exception will be thrown that must be
 * handled by the client of the business logic.
 * 
 * @author fschoeppl
 */
@ApplicationScoped // TODO PK is ApplicationScoped correct when all calls share the same connection? are multiple calls within the same application possible? 
public class BusinessLogicImpl implements BusinessLogic {

    private static final String JOB_USER_MISSMATCH = "org.backmeup.logic.impl.BusinessLogicImpl.JOB_USER_MISSMATCH";
    private static final String NO_SUCH_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_SUCH_JOB";
    private static final String UNKNOWN_JOB_WITH_ID = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_JOB_WITH_ID";
    private static final String UNKNOWN_SOURCE_SINK = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SOURCE_SINK";
    private static final String USER_HAS_NO_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.USER_HAS_NO_PROFILE";
    private static final String SHUTTING_DOWN_BUSINESS_LOGIC = "org.backmeup.logic.impl.BusinessLogicImpl.SHUTTING_DOWN_BUSINESS_LOGIC";
    private static final String VALIDATION_OF_ACCESS_DATA_FAILED = "org.backmeup.logic.impl.BusinessLogicImpl.VALIDATION_OF_ACCESS_DATA_FAILED";
    private static final String UNKNOWN_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_PROFILE";
    private static final String UNKNOWN_ACTION = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_ACTION";
    private static final String ERROR_OCCURED = "org.backmeup.logic.impl.BusinessLogicImpl.ERROR_OCCURED";
    private static final String UNKNOWN_SEARCH_ID = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SEARCH_ID";
    private static final String NO_PROFILE_WITHIN_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_PROFILE_WITHIN_JOB";

    @Inject
    @Configuration(key="backmeup.callbackUrl")
    private String callbackUrl;

    @Inject
    @Configuration(key="backmeup.minimalPasswordLength")
    private Integer minimalPasswordLength;

    @Inject
    @Configuration(key="backmeup.index.host")
    private String indexHost;

    @Inject
    @Configuration(key="backmeup.index.port")
    private Integer indexPort;

    @Inject
    private DataAccessLayer dal;

    @Inject
    private Keyserver keyserverClient;

    @Inject
    @Named("plugin")
    private Plugin plugins;

    // See setJobManager()
    private JobManager jobManager;

    @Inject
    private Connection conn;

    @Inject
    private Node esNode;

    // RabbitMQJobReceiver -------------------
    @Inject
    @Configuration(key="backmeup.message.queue.host")
    private String mqHost;

    @Inject
    @Configuration(key="backmeup.message.queue.name")
    private String mqName;

    @Inject
    @Configuration(key="backmeup.message.queue.receivers")
    private Integer numberOfJobWorker;

    @Inject
    @Configuration(key="backmeup.job.backupname")
    private String backupName;

    @Inject
    @Configuration(key="backmeup.job.temporaryDirectory")
    private String jobTempDir;

    private List<RabbitMQJobReceiver> jobWorker;

    @Inject
    private UserRegistration registrationService;
    // ---------------------------------------

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ResourceBundle textBundle = ResourceBundle
            .getBundle(getClass().getSimpleName());

    @PostConstruct
    public void startup() {
        logger.info("Starting job workers");
        try {
            jobWorker = new ArrayList<>();
            for (int i = 0; i < numberOfJobWorker; i++) {
                RabbitMQJobReceiver rec = new RabbitMQJobReceiver(mqHost,
                        mqName, indexHost, indexPort, backupName, jobTempDir,
                        plugins, keyserverClient, dal);
                rec.start();
                jobWorker.add(rec);
            }
        } catch (Exception e) {
            logger.error("Error while starting job receivers", e);
        }
    }

    private ProfileDao getProfileDao() {
        return dal.createProfileDao();
    }

    private UserDao getUserDao() {
        return dal.createUserDao();
    }

    private BackupJobDao getBackupJobDao() {
        return dal.createBackupJobDao();
    }

    private StatusDao getStatusDao() {
        return dal.createStatusDao();
    }

    private SearchResponseDao getSearchResponseDao() {
        return dal.createSearchResponseDao();
    }

    @Override
    public BackMeUpUser getUser(String username) {
        try {
            conn.beginOrJoin();
            
            return registrationService.queryActivatedUser(username);
            
        } finally {
            conn.rollback();
        }
    }

    @Override
    public BackMeUpUser deleteUser(String username) {
        conn.begin();
        ElasticSearchIndexClient client = null;
        try {
            BackMeUpUser u = registrationService.queryExistingUser(username);
            Long uid = u.getUserId();
            UserDao userDao = getUserDao();

            try {
                keyserverClient.deleteUser(u.getUserId());
            } catch (Exception ex) {
                logger.warn(MessageFormat.format("Couldn't delete user \"{0}\" from keyserver", username), ex);
            }

            BackupJobDao jobDao = getBackupJobDao();
            StatusDao statusDao = getStatusDao();
            for (BackupJob job : jobDao.findByUsername(username)) {
                for (Status status : statusDao.findByJobId(job.getId())) {
                    statusDao.delete(status);
                }
                jobDao.delete(job);
            }

            ProfileDao profileDao = getProfileDao();
            for (Profile p : profileDao.findProfilesByUsername(username)) {
                profileDao.delete(p);
            }

            userDao.delete(u);
            conn.commit();

            client = getIndexClient();
            client.deleteRecordsForUser(uid);

            return u;
        } finally {
            if(client != null){
                client.close();
            }
            conn.rollback();
        }
    }

    @Override
    public BackMeUpUser changeUser(String oldUsername, String newUsername, String oldPassword,
            String newPassword, String oldKeyRingPassword, String newKeyRingPassword, String newEmail) {
        try {
            conn.begin();
            
            BackMeUpUser user = registrationService.queryActivatedUser(oldUsername);
            registrationService.ensureNewValuesAvailable(user, newUsername, newEmail);
            
            // TODO Remove keyring from change user options
            if (!keyserverClient.validateUser(user.getUserId(), oldPassword)) {
                conn.rollback();
                throw new InvalidCredentialsException();
            }

            if (newPassword != null) {
                throwIfPasswordInvalid(newPassword);
                keyserverClient.changeUserPassword(user.getUserId(), oldPassword, newPassword);
            }

            if (newKeyRingPassword != null && oldKeyRingPassword != null && !oldKeyRingPassword.equals(newKeyRingPassword)) {
                keyserverClient.changeUserKeyRing(user.getUserId(), oldKeyRingPassword, newKeyRingPassword);
            }

            registrationService.updateValues(user, newUsername, newEmail);
            
            conn.commit();
            return user;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public BackMeUpUser login(String username, String password) {
        try {
            conn.begin();
            
            BackMeUpUser user = registrationService.queryExistingUser(username);
            if (!keyserverClient.validateUser(user.getUserId(), password)) {
                throw new InvalidCredentialsException();
            }

            return user;
        } finally {
            conn.rollback();
        }
    }

    private void throwIfPasswordInvalid(String password) {
        if (password == null || password.length() < minimalPasswordLength) {
            throw new PasswordTooShortException(minimalPasswordLength, password == null ? 0 : password.length());
        }
    }

    @Override
    public BackMeUpUser register(String username, String password,
            String keyRingPassword, String email) {

        throwIfPasswordInvalid(password);
        throwIfPasswordInvalid(keyRingPassword); // TODO PK keyRingPassword unused?

        try {
            conn.begin();
            
            BackMeUpUser user = registrationService.register(username, email);

            keyserverClient.registerUser(user.getUserId(), password);
            
            registrationService.sendVerificationEmailFor(user);
            
            conn.commit();
            return user;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void setUserProperty(String username, String key, String value) {
        try {
            conn.beginOrJoin();
            
            BackMeUpUser user = getUser(username);
            user.setUserProperty(key, value);
            
            conn.commit();
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void deleteUserProperty(String username, String key) {
        try {
            conn.beginOrJoin();
            
            BackMeUpUser user = getUser(username);
            user.deleteUserProperty(key);
            
            conn.commit();
        } finally {
            conn.rollback();
        }
    }

    @Override
    public List<SourceSinkDescribable> getDatasources() {
        return plugins.getConnectedDatasources();
    }

    @Override
    public List<Profile> getDatasourceProfiles(String username) {
        try {
            conn.begin();
            List<Profile> profiles = getProfileDao()
                    .findDatasourceProfilesByUsername(username);
            return profiles;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public Profile deleteProfile(String username, Long profileId) {
        try {
            conn.begin();
            
            Profile profile = deleteProfileX(username, profileId);
            
            conn.commit();
            return profile;
        } finally {
            conn.rollback();
        }
    }

    private Profile deleteProfileX(String username, Long profileId) {
        // TODO PK move
        ProfileDao profileDao = dal.createProfileDao();
        Profile profile = profileDao.findById(profileId);
        if (profile == null || !profile.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException();
        }
        profileDao.delete(profile);
        return profile;
    }

    @Override
    public List<String> getDatasourceOptions(String username, Long profileId,
            String keyRingPassword) {
        try {
            conn.beginOrJoin();
            
            ProfileDao pd = getProfileDao();
            Profile p = pd.findById(profileId);
            if (!p.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException();
            }
            Datasource source = plugins.getDatasource(p.getDescription());
            Token t = keyserverClient.getToken(p, keyRingPassword, new Date().getTime(), false, null);
            AuthDataResult authData = keyserverClient.getData(t);
            Properties accessData = authData.getByProfileId(profileId);
            return source.getAvailableOptions(accessData);
            
        } catch (PluginException pe) {
            logger.error("Error during getAvailableOptions", pe);
            throw pe;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public List<String> getStoredDatasourceOptions(String username,
            Long profileId, Long jobId) {
        try {
            conn.beginOrJoin();
            getUser(username);
            BackupJobDao jobDao = getBackupJobDao();
            BackupJob job = jobDao.findById(jobId);
            if (job == null) {
                throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
            }
            if (!job.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                        jobId, username));
            }
            for (ProfileOptions po : job.getSourceProfiles()) {
                if (po.getProfile().getProfileId().equals(profileId)) {
                    String[] options = po.getOptions();
                    if (options == null) {
                        return new ArrayList<>();
                    }
                    return Arrays.asList(options);
                }
            }
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void changeProfile(Long profileId, Long jobId, List<String> sourceOptions)
    {
        try
        {
            conn.beginOrJoin();
            ProfileDao pd = getProfileDao ();
            Profile p = pd.findById (profileId);
            if (p == null)
            {
                throw new IllegalArgumentException (String.format (textBundle.getString(UNKNOWN_PROFILE), profileId));
            }

            BackupJobDao bd = getBackupJobDao ();
            BackupJob backupjob = bd.findById (jobId);

            Set<ProfileOptions> profileoptions = backupjob.getSourceProfiles ();
            for (ProfileOptions option : profileoptions)
            {
                if (option.getProfile ().getProfileId ().equals(p.getProfileId ()))
                {
                    String[] new_options = sourceOptions.toArray (new String[sourceOptions.size ()]);
                    option.setOptions (new_options);
                }
            }

            conn.commit ();
        }
        finally
        {
            conn.rollback();
        }
    }

    @Override
    public void deleteDatasourcePlugin(String name) {
        throw new UnsupportedOperationException("delete Datasource Plugin not implemented");
    }

    @Override
    public List<SourceSinkDescribable> getDatasinks() {
        List<SourceSinkDescribable> sinks = plugins.getConnectedDatasinks();
        return sinks;
    }

    @Override
    public List<Profile> getDatasinkProfiles(String username) {
        try {
            conn.begin();
            List<Profile> profiles = getProfileDao().findDatasinkProfilesByUsername(
                    username);
            return profiles;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void deleteDatasinkPlugin(String name) {
    }

    @Override
    public List<ActionDescribable> getActions() {
        List<ActionDescribable> actions = plugins.getActions();
        //TODO Move all internal actions to real OSGi bundles!!
        actions.add(new IndexDescribable());
        actions.add(new FilesplittDescribable());
        actions.add(new EncryptionDescribable());
        return actions;
    }

    @Override
    public ActionProfile getStoredActionOptions(String actionId, Long jobId) {
        try {
            conn.beginOrJoin();
            BackupJobDao jobDao = getBackupJobDao();
            BackupJob job = jobDao.findById(jobId);
            if (job == null) {
                throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
            }
            for (ActionProfile ap : job.getRequiredActions()) {
                if (ap.getActionId().equals(actionId)) {
                    return ap;
                }
            }
            throw new IllegalArgumentException(String.format(textBundle.getString(NO_PROFILE_WITHIN_JOB), jobId, actionId));
        } finally {
            conn.rollback();
        }
    }

    @Override
    public List<String> getActionOptions(String actionId)
    {
        //TODO Move all internal actions to real OSGi bundles!!
        //ActionDescribable action = plugins.getActionById (actionId);
        //return action.getAvailableOptions ();

        if (actionId.equals ("org.backmeup.indexer"))
        {
            return new IndexDescribable().getAvailableOptions ();
        }
        else if (actionId.equals ("org.backmeup.filesplitting"))
        {
            return new FilesplittDescribable().getAvailableOptions();
        }
        else if (actionId.equals ("org.backmeup.encryption"))
        {
            return new EncryptionDescribable().getAvailableOptions ();
        }
        else
        {
            return new LinkedList<> ();
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
    public void changeActionOptions(String actionId, Long jobId,
            Map<String, String> actionOptions) {
        try {
            conn.beginOrJoin();
            BackupJobDao jobDao = getBackupJobDao();
            BackupJob job = jobDao.findById(jobId);
            if (job == null) {
                throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
            }
            for (ActionProfile ap : job.getRequiredActions()) {
                if (ap.getActionId().equals(actionId)) {
                    ap.getActionOptions().clear();
                    addActionProperties(ap, actionOptions);
                }
            }
            conn.commit();
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void deleteActionPlugin(String name) {
        throw new UnsupportedOperationException("delete Action Plugin not implemented");
    }

    private List<ActionProfile> getActionProfilesFor(JobCreationRequest request) {
        List<ActionProfile> actions = new ArrayList<>();

        for (ActionProfileEntry action : request.getActions()) {
            ActionDescribable ad = null;
            // TODO Remove workaround for embedded action plugins
            if ("org.backmeup.filesplitting".equals(action.getId())) {
                ad = new FilesplittDescribable();
            } else if ("org.backmeup.indexer".equals(action.getId())) {
                ad = new IndexDescribable();
            } else if ("org.backmeup.encryption".equals(action.getId())) {
                ad = new EncryptionDescribable();
            } else {
                ad = plugins.getActionById(action.getId());
            }
            if (ad == null) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(UNKNOWN_ACTION), action.getId()));
            }
            ActionProfile ap = new ActionProfile(ad.getId(), ad.getPriority());
            for (Map.Entry<String, String> entry : action.getOptions().entrySet()) {
                ap.addActionOption(entry.getKey(), entry.getValue());
            }
            actions.add(ap);
        }
        Collections.sort(actions);
        return actions;
    }

    private Set<ProfileOptions> getSourceProfilesFor(JobCreationRequest request) {
        Set<ProfileOptions> profiles = new HashSet<>();
        if (request.getSourceProfiles().size() == 0) {
            throw new IllegalArgumentException(
                    "There must be at least one source profile to download data from!");
        }

        for (SourceProfileEntry source : request.getSourceProfiles()) {
            Profile p = getProfileDao().findById(source.getId());
            if (p == null) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(UNKNOWN_PROFILE), source.getId()));
            }

            profiles.add(new ProfileOptions(p, source.getOptions().keySet().toArray(new String[]{})));
        }
        return profiles;
    }

    private Profile getSinkProfileFor(JobCreationRequest request) {
        Profile sink = getProfileDao().findById(request.getSinkProfileId());
        if (sink == null) {
            throw new IllegalArgumentException(String.format(
                    textBundle.getString(UNKNOWN_PROFILE), request.getSinkProfileId()));
        }
        return sink;
    }

    @Override
    public ValidationNotes createBackupJob(String username, JobCreationRequest request) {
        try {
            conn.begin();
            BackMeUpUser user = registrationService.queryActivatedUser(username);

            if (!keyserverClient.validateUser(user.getUserId(), request.getKeyRing())) {
                throw new InvalidCredentialsException();
            }

            Set<ProfileOptions> profiles = getSourceProfilesFor(request);

            Profile sink = getSinkProfileFor(request);

            List<ActionProfile> actions = getActionProfilesFor(request);

            ExecutionTime execTime = BackUpJobCreationHelper.getExecutionTimeFor(request);

            conn.rollback();
            BackupJob job = jobManager.createBackupJob(user, profiles, sink, actions,
                    execTime.getStart(), execTime.getDelay(), request.getKeyRing(), request.getJobTitle(), execTime.isReschedule(), request.getTimeExpression());
            ValidationNotes vn = validateBackupJob(username, job.getId(), request.getKeyRing());
            vn.setJob(job);
            return vn;
        } finally {
            conn.rollback();
        }
    }

    // Note: keyRing won't be overridden
    @Override
    public ValidationNotes updateBackupJob(String username,
            JobUpdateRequest updateRequest) {
        boolean scheduleJob = false;

        if (updateRequest == null) {
            throw new IllegalArgumentException("Update must not be null!");
        }

        if (updateRequest.getJobId() == null) {
            throw new IllegalArgumentException("JobId must not be null!");
        }


        BackMeUpUser user = getUser(username);
        if (!keyserverClient.validateUser(user.getUserId(), updateRequest.getKeyRing())) {
            throw new InvalidCredentialsException();
        }

        try {
            conn.begin();
            BackupJob job = getBackupJobDao().findById(updateRequest.getJobId());
            if (job == null || !job.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                        updateRequest.getJobId(), username));
            }

            job.getRequiredActions().clear();
            job.getRequiredActions().addAll(getActionProfilesFor(updateRequest));

            job.getSourceProfiles().clear();
            job.getSourceProfiles().addAll(getSourceProfilesFor(updateRequest));

            job.setJobTitle(updateRequest.getJobTitle());
            job.setSinkProfile(getSinkProfileFor(updateRequest));

            ExecutionTime et = BackUpJobCreationHelper.getExecutionTimeFor(updateRequest);

            // check if the interval has changed
            if (job.getTimeExpression ().compareToIgnoreCase (updateRequest.getTimeExpression()) != 0)
            {
                scheduleJob = true;

                // chage start date to now if interval has changed
                job.setStart (new Date ());
            }

            job.setTimeExpression(updateRequest.getTimeExpression());
            job.setDelay(et.getDelay());
            job.setReschedule (et.isReschedule ());

            if (job.isReschedule () == true)
            {
                Date execTime = new Date (new Date().getTime() + job.getDelay());
                job.setNextExecutionTime (execTime);
            }
            else
            {
                job.setNextExecutionTime (null);
            }

            conn.commit();

            if(scheduleJob == true)
            {
                // add the updated job to the queue. (All old queue entrys get invalid and will not be executed)
                jobManager.runBackUpJob (job);
            }

            ValidationNotes vn = validateBackupJob(username, job.getId(), updateRequest.getKeyRing());
            vn.setJob(job);
            return vn;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public JobUpdateRequest getBackupJob(String username, Long jobId) {
        try {
            if (jobId == null) {
                throw new IllegalArgumentException("Missing parameter " + jobId);
            }
            conn.begin();
            BackupJobDao jobDao = dal.createBackupJobDao();
            BackupJob job = jobDao.findById(jobId);
            return BackUpJobConverter.convertJobToUpdateRequest(job);
        } finally {
            conn.rollback();
        }
    }

    @Override
    public List<BackupJob> getJobs(String username) {
        try {
            conn.begin();
            getUser(username);
            BackupJobDao jobDao = getBackupJobDao();
            return jobDao.findByUsername(username);
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void deleteJob(String username, Long jobId) {
        try {
            conn.begin();
            getUser(username);
            BackupJobDao jobDao = getBackupJobDao();
            BackupJob job = jobDao.findById(jobId);
            if (job == null) {
                throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
            }
            if (!job.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                        jobId, username));
            }

            // Delete Job status records first
            StatusDao statusDao = getStatusDao();
            for (Status status : statusDao.findByJobId(job.getId())) {
                statusDao.delete(status);
            }

            jobDao.delete(job);
            conn.commit();
        } finally {
            conn.rollback();
        }
    }

    private List<Status> getStatusForJob(BackupJob job) {
        try {
            conn.beginOrJoin();
            StatusDao sd = dal.createStatusDao();
            List<Status> status = sd.findLastByJob(job.getUser().getUsername(), job.getId());
            ElasticSearchIndexClient client = null;

            // Getting all files for job.getId()
            try {
                client = new ElasticSearchIndexClient(indexHost, indexPort);
                org.elasticsearch.action.search.SearchResponse esResponse = client.searchByJobId(job.getId());

                Set<FileItem> fileItems = IndexUtils.convertToFileItems(esResponse);
                for (Status stat : status) {
                    stat.setFiles(fileItems);
                }
                return status;
            } catch (Throwable t) {
                logger.error("", t);
            } finally {
                if(client != null){
                    client.close();
                }
            }
            return null;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public List<Status> getStatus(String username, Long jobId) {
        try {
            conn.begin();
            registrationService.queryActivatedUser(username);
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

            BackupJob job = jobDao.findById(jobId);
            if (job == null) {
                throw new IllegalArgumentException(textBundle.getString(String.format(NO_SUCH_JOB, jobId)));
            }
            if (!job.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(textBundle.getString(String.format(JOB_USER_MISSMATCH,
                        jobId, username)));
            }
            List<Status> status = new ArrayList<>();
            status.addAll(getStatusForJob(job));
            return status;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public ProtocolDetails getProtocolDetails(String username, String fileId) {
        try {
            conn.begin();
            ElasticSearchIndexClient client = null;

            try {
                client = new ElasticSearchIndexClient(indexHost, indexPort);
                org.elasticsearch.action.search.SearchResponse esResponse = client.getFileById(username, fileId);
                ProtocolDetails pd = new ProtocolDetails();
                pd.setFileInfo(IndexUtils.convertToFileInfo(esResponse));
                return pd;
            } catch (Throwable t) {
                logger.error("", t);
            } finally {
                if(client != null) {
                    client.close();
                }
            }
            return new ProtocolDetails();
        } finally {
            conn.rollback();
        }
    }

    @Override
    public ProtocolOverview getProtocolOverview(String username, String duration) {
        try {
            conn.begin();
            BackMeUpUser user = getUser(username);
            
            Date to = new Date();
            Date from = duration.equals("month") ? new Date(to.getTime() - DELAY_MONTHLY) :
                new Date(to.getTime() - DELAY_WEEKLY);

            return getProtocolOverview(user, from, to);
            
        } finally {
            conn.rollback();
        }
    }

    private ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to) {
        // TODO PK only depending on JobProtocolDao - move to service?
        JobProtocolDao jpd = dal.createJobProtocolDao();
        List<JobProtocol> protocols = jpd.findByUsernameAndDuration(user.getUsername(), from, to);
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
    public AuthRequest preAuth(String username, String uniqueDescIdentifier,
            String profileName, String keyRing) throws PluginException,
            InvalidCredentialsException {
        Authorizable auth = plugins.getAuthorizable(uniqueDescIdentifier);
        SourceSinkDescribable desc = plugins
                .getSourceSinkById(uniqueDescIdentifier);
        org.backmeup.model.spi.SourceSinkDescribable.Type type = desc.getType();
        AuthRequest ar = new AuthRequest();
        try {
            conn.begin();
            BackMeUpUser user = getUser(username);

            if (!keyserverClient.validateUser(user.getUserId(), keyRing)) {
                conn.rollback();
                throw new InvalidCredentialsException();
            }
            Profile profile = new Profile(getUserDao().findByName(username),
                    profileName, uniqueDescIdentifier, type);
            switch (auth.getAuthType()) {
            case OAuth:
                OAuthBased oauth = plugins
                .getOAuthBasedAuthorizable(uniqueDescIdentifier);
                Properties p = new Properties();
                p.setProperty("callback", callbackUrl);
                String redirectUrl = oauth.createRedirectURL(p, callbackUrl);
                ar.setRedirectURL(redirectUrl);
                // TODO Store all properties within keyserver & don't store them within the local database!

                profile = getProfileDao().save(profile);
                if (!keyserverClient.isServiceRegistered(profile.getProfileId())) {
                    keyserverClient.addService(profile.getProfileId());
                }
                keyserverClient.addAuthInfo(profile, keyRing, p);
                break;
            case InputBased:
                InputBased ibased = plugins
                .getInputBasedAuthorizable(uniqueDescIdentifier);
                ar.setRequiredInputs(ibased.getRequiredInputFields());
                p = new Properties();
                p.setProperty("callback", callbackUrl);
                profile = getProfileDao().save(profile);
                if (!keyserverClient.isServiceRegistered(profile.getProfileId())) {
                    keyserverClient.addService(profile.getProfileId());
                }
                keyserverClient.addAuthInfo(profile, keyRing, p);
                break;
            default:
                throw new IllegalArgumentException("unknown enum value " + auth.getAuthType());
            }

            conn.commit();
            ar.setProfile(profile);
            return ar;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void postAuth(Long profileId, Properties props, String keyRing)
            throws PluginException, ValidationException, InvalidCredentialsException {
        try {
            if (keyRing == null) {
                throw new IllegalArgumentException("keyRing-Parameter cannot be null!");
            } else if (profileId == null) {
                throw new IllegalArgumentException("profileId-Parameter cannot be null!");
            } else if (props == null) {
                throw new IllegalArgumentException("properties-Parameter cannot be null!");
            }

            conn.begin();
            ProfileDao profileDao = getProfileDao();
            Profile p = profileDao.findById(profileId);

            if (!keyserverClient.isServiceRegistered(p.getProfileId())) {
                keyserverClient.addService(p.getProfileId());
            }
            if (keyserverClient.isAuthInformationAvailable(p, keyRing)) {
                Token t = keyserverClient.getToken(p, keyRing, new Date().getTime(), false, null);
                AuthDataResult adr = keyserverClient.getData(t);
                if (adr.getAuthinfos().length > 0) {
                    props.putAll(adr.getAuthinfos()[0].getAi_data());
                }
            }

            Authorizable auth = plugins.getAuthorizable(p.getDescription());
            if (auth.getAuthType() == AuthorizationType.InputBased) {
                InputBased inputBasedService = plugins.getInputBasedAuthorizable(p
                        .getDescription());
                if (inputBasedService.isValid(props)) {
                    String userId = auth.postAuthorize(props);
                    if (userId != null) {
                        p.setIdentification(userId);
                    }
                    profileDao.save(p);
                    if (keyserverClient.isAuthInformationAvailable(p, keyRing)) {
                        keyserverClient.deleteAuthInfo(p.getProfileId());
                    }
                    keyserverClient.addAuthInfo(p, keyRing, props);
                    conn.commit();
                    return;
                } else {
                    conn.rollback();
                    throw new ValidationException(ValidationExceptionType.AuthException,
                            textBundle.getString(VALIDATION_OF_ACCESS_DATA_FAILED));
                }
            } else {
                String userId = auth.postAuthorize(props);
                if (userId != null) {
                    p.setIdentification(userId);
                }
                profileDao.save(p);
                if (keyserverClient.isAuthInformationAvailable(p, keyRing)) {
                    keyserverClient.deleteAuthInfo(p.getProfileId());
                }
                keyserverClient.addAuthInfo(p, keyRing, props);
                conn.commit();
            }
        } catch (PluginException pe) {
            logger.error("", pe);
            throw pe;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public long searchBackup(String username, String keyRingPassword, String query) {
        return searchBackup(username, keyRingPassword, query, new String[0]);
    }

    private long searchBackup(String username, String keyRingPassword, String query, String[] typeFilters) {
        try {
            conn.begin();
            BackMeUpUser user = getUser(username);

            if (!keyserverClient.validateUser(user.getUserId(), keyRingPassword)) {
                throw new InvalidCredentialsException();
            }

            SearchResponse search = new SearchResponse(query, Arrays.asList(typeFilters));
            SearchResponseDao searchDao = getSearchResponseDao();
            search = searchDao.save(search);
            conn.commit();

            return search.getId();
        } catch (Throwable t) {
            if (t instanceof BackMeUpException) {
                throw (BackMeUpException) t;
            }
            throw new BackMeUpException(textBundle.getString(ERROR_OCCURED), t);
        } finally {
            conn.rollback();
        }
    }

    @Override
    public void deleteIndexForUser(String username) {
        ElasticSearchIndexClient client = null;
        try {
            conn.begin();

            BackMeUpUser user = registrationService.queryActivatedUser(username);

            try {
                client = getIndexClient();
                client.deleteRecordsForUser(user.getUserId());
            } catch (Throwable t) {
                logger.error("", t);
            }
            
        } finally {
            conn.rollback();
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void deleteIndexForJobAndTimestamp(Long jobId, Long timestamp) {
        ElasticSearchIndexClient client = null;
        try {
            conn.begin();

            try {
                client = getIndexClient();
                client.deleteRecordsForJobAndTimestamp(jobId, timestamp);
            } catch (Throwable t) {
                logger.error("", t);
            }
        } finally {
            conn.rollback();
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public SearchResponse queryBackup(String username, long searchId, Map<String, List<String>> filters) {

        ElasticSearchIndexClient client = null;
        try {
            conn.begin();

            // at least we make sure, that the user exists
            BackMeUpUser user = getUser(username);

            SearchResponse search = getSearchResponseDao().findById(searchId);
            if (search == null) {
                throw new BackMeUpException(textBundle.getString(UNKNOWN_SEARCH_ID));
            }

            try {
                String query = search.getQuery();

                client = getIndexClient();
                org.elasticsearch.action.search.SearchResponse esResponse = client.queryBackup(user, query, filters);
                search.setFiles(IndexUtils.convertSearchEntries(esResponse, user));
                search.setBySource(IndexUtils.getBySource(esResponse));
                search.setByType(IndexUtils.getByType(esResponse));
                search.setByJob (IndexUtils.getByJob (esResponse));
            } catch (Throwable t) {
                logger.error("", t);
            }
            return search;
        } finally {
            conn.rollback();
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public File getThumbnail(String username, String fileId) {
        // TODO verify that the user is logged in!

        ElasticSearchIndexClient client = getIndexClient();
        String thumbnailPath = client.getThumbnailPathForFile(username, fileId);
        logger.debug("Got thumbnail path: " + thumbnailPath);
        if (thumbnailPath != null) {
            return new File(thumbnailPath);
        }

        client.close();

        return null; // Too bad there's no optional return types in Java...
    }

    private ElasticSearchIndexClient getIndexClient() {
        return new ElasticSearchIndexClient(indexHost, indexPort);
    }

    @Override
    @PreDestroy
    public void shutdown() {
        logger.debug(textBundle.getString(SHUTTING_DOWN_BUSINESS_LOGIC));
        jobManager.shutdown();
        plugins.shutdown();
        esNode.close();

        logger.info("Shutting down job workers!");
        for (RabbitMQJobReceiver receiver : jobWorker) {
            receiver.stop();
        }
    }

    @Inject
    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
        this.jobManager.start();
    }

    private Properties fetchAuthenticationData(Profile p, String password) {
        Token t = keyserverClient.getToken(p, password, new Date().getTime(), false, null);
        AuthDataResult result = keyserverClient.getData(t);
        Properties props = new Properties();
        if (result.getAuthinfos().length > 0) {
            props.putAll(result.getAuthinfos()[0].getAi_data());
        }
        return props;
    }

    @Override
    public Properties getMetadata(String username, Long profileId, String keyRing) {
        try {
            conn.beginOrJoin();
            Profile p = getProfileDao().findById(profileId);
            if (p == null) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(UNKNOWN_PROFILE), profileId));
            }
            if (!p.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(USER_HAS_NO_PROFILE), username, profileId));
            }
            SourceSinkDescribable ssd = plugins.getSourceSinkById(p.getDescription());
            if (ssd == null) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(UNKNOWN_SOURCE_SINK), p.getDescription()));
            }


            Properties accessData = keyRing != null ? fetchAuthenticationData(p, keyRing) : null;
            Properties metadata = ssd.getMetadata(accessData);
            return metadata;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public ValidationNotes validateProfile(String username, Long profileId, String keyRing) {
        String pluginName = null;
        try {
            conn.beginOrJoin();
            Profile p = getProfileDao().findById(profileId);
            if (p == null || !p.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(USER_HAS_NO_PROFILE), username, profileId));
            }
            pluginName = p.getDescription();
            Validationable validator = plugins.getValidator(p.getDescription());
            Properties accessData = fetchAuthenticationData(p, keyRing);
            return validator.validate(accessData);

        } catch (PluginUnavailableException pue) {
            ValidationNotes notes = new ValidationNotes();
            notes.addValidationEntry(ValidationExceptionType.NoValidatorAvailable, pluginName);
            return notes;
        } catch (Exception pe) {
            ValidationNotes notes = new ValidationNotes();
            notes.addValidationEntry(ValidationExceptionType.Error, pluginName, pe);
            return notes;
        } finally {
            conn.rollback();
        }
    }

    //TODO Add password parameter to get token from keyserver to validate the profile
    @Override
    public ValidationNotes validateBackupJob(String username, Long jobId, String keyRing) {
        try {
            conn.begin();
            getUser(username);

            BackupJob job = jobManager.getBackUpJob(jobId);
            if (job == null || !job.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(UNKNOWN_JOB_WITH_ID), jobId));
            }

            ValidationNotes notes = new ValidationNotes();
            try {
                // plugin-level validation
                for (ProfileOptions po : job.getSourceProfiles()) {
                    SourceSinkDescribable ssd = plugins.getSourceSinkById(po.getProfile()
                            .getDescription());
                    if (ssd == null) {
                        notes.addValidationEntry(ValidationExceptionType.PluginUnavailable, po
                                .getProfile().getDescription());
                    }

                    // Validate source plug-in itself
                    notes.getValidationEntries().addAll(
                            validateProfile(username, po.getProfile().getProfileId(), keyRing)
                            .getValidationEntries());
                }

                // validate sink profile
                notes.getValidationEntries().addAll(
                        validateProfile(username, job.getSinkProfile().getProfileId(), keyRing)
                        .getValidationEntries());

            } catch (BackMeUpException bme) {
                notes.addValidationEntry(ValidationExceptionType.Error,
                        bme);
            }
            return notes;
        } finally {
            conn.rollback();
        }
    }

    //TODO Store profile data within keyserver!
    @Override
    public void addProfileEntries(Long profileId, Properties entries, String keyRing) {
        try {
            conn.begin();
            
            ProfileDao dao = getProfileDao();
            Profile p = dao.findById(profileId);
            if (p == null) {
                throw new IllegalArgumentException("Unknown profile " + profileId);
            }
            Properties props = new Properties();
            if (keyserverClient.isAuthInformationAvailable(p, keyRing)) {
                props.putAll(fetchAuthenticationData(p, keyRing));
                keyserverClient.deleteAuthInfo(p.getProfileId());
            }
            props.putAll(entries);
            keyserverClient.addAuthInfo(p, keyRing, props);
            dao.save(p);
            
            conn.commit();
        } finally {
            conn.rollback();
        }
    }

    @Override
    public BackMeUpUser verifyEmailAddress(String verificationKey) {
        try {
            conn.begin();
            
            BackMeUpUser user = registrationService.activateUserFor(verificationKey);

            conn.commit();
            return user;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public BackMeUpUser requestNewVerificationEmail(String username) {
        try {
            conn.begin();
            
            BackMeUpUser user = registrationService.requestNewVerificationEmail(username);
            
            conn.commit();
            return user;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public List<KeyserverLog> getKeysrvLogs (BackMeUpUser user) {
        return keyserverClient.getLogs (user);
    }
}

package org.backmeup.tests.integration.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.backmeup.model.constants.JobFrequency;
import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.dto.WorkerInfoDTO;
import org.backmeup.model.dto.WorkerMetricDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;

public class TestDataManager {
    public static final String WORKER_ID = "backmeup-worker";
    public static final String WORKER_SECRET = "REPLACE-WORKER";
    
    public static UserDTO getUser() {
        String username = "john.doe";
        String firstname = "John";
        String lastname = "Doe";
        String password = "password1";
        String email = "TestUser@trash-mail.com";

        return new UserDTO(username, firstname, lastname, password, email);
    }
    
    public static AuthDataDTO getAuthDataDummy() {
        String authDataName = "AuthData1";

        AuthDataDTO authData = new AuthDataDTO();
        authData.setName(authDataName);
        authData.addProperty("password", "s3cr3t");

        return authData;
    }

    public static AuthDataDTO getAuthDataBackmeupStorage() {
        String authName = "BackmeupStorage";

        AuthDataDTO authData = new AuthDataDTO();
        authData.setName(authName);
        authData.addProperty("username", getUser().getUsername());
        authData.addProperty("password", getUser().getPassword());

        return authData;
    }

    public static AuthDataDTO getAuthDataEmail() {
        String authName = "EmailWork";

        AuthDataDTO authData = new AuthDataDTO();
        authData.setName(authName);
        authData.addProperty("Username", "");
        authData.addProperty("Password", "");
        authData.addProperty("Type", "imap"); // "imap"
        authData.addProperty("Host", "");
        authData.addProperty("Port", "");
        authData.addProperty("SSL", "false");

        return authData;
    }

    public static PluginProfileDTO getProfileFilegenerator() {
        String pluginId = "org.backmeup.filegenerator";
        PluginType profileType = PluginType.Source;

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);

        pluginProfile.addProperty("text", "true");
        pluginProfile.addProperty("image", "true");
        pluginProfile.addProperty("pdf", "true");
        pluginProfile.addProperty("binary", "true");

        return pluginProfile;
    }

    public static PluginProfileDTO getProfileThumbnailAction() {
        String pluginId = "org.backmeup.thumbnail";
        PluginType profileType = PluginType.Action;

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);

        return pluginProfile;
    }
    
    public static PluginProfileDTO getProfileZipAction() {
        String pluginId = "org.backmeup.zip";
        PluginType profileType = PluginType.Action;

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);

        return pluginProfile;
    }

    public static PluginProfileDTO getProfileBackmeupStorageSink() {
        String pluginId = "org.backmeup.storage";
        PluginType profileType = PluginType.Sink;
        AuthDataDTO authData = getAuthDataBackmeupStorage();

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);
        pluginProfile.setAuthData(authData);

        return pluginProfile;
    }

    public static PluginProfileDTO getProfileEmail() {
        String pluginId = "org.backmeup.mail";
        PluginType profileType = PluginType.Source;
        AuthDataDTO authData = getAuthDataEmail();

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);
        pluginProfile.setAuthData(authData);

        return pluginProfile;
    }

    public static PluginProfileDTO getProfileDummySource() {
        String pluginId = "org.backmeup.dummy";
        PluginType profileType = PluginType.Source;

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);

        return pluginProfile;
    }

    public static PluginProfileDTO getProfileDummySink() {
        String pluginId = "org.backmeup.dummy";
        PluginType profileType = PluginType.Sink;

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);

        return pluginProfile;

    }

    public static BackupJobCreationDTO getBackupJob(String sourceProfileId, String sinkProfileId) {
        String jobTitle = "BackupJob1";
        JobFrequency schedule = JobFrequency.WEEKLY;
        Date start = new Date();

        BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
        backupJob.setJobTitle(jobTitle);
        backupJob.setSchedule(schedule);
        backupJob.setStart(start);
        backupJob.setSource(Long.parseLong(sourceProfileId));
        backupJob.setSink(Long.parseLong(sinkProfileId));

        return backupJob;
    }

    public static BackupJobCreationDTO getBackupJob(String sourceProfileId, String sinkProfileId, String... actionProfileIds) {
        String jobTitle = "BackupJob1";
        JobFrequency schedule = JobFrequency.WEEKLY;
        Date start = new Date();

        BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
        backupJob.setJobTitle(jobTitle);
        backupJob.setSchedule(schedule);
        backupJob.setStart(start);
        backupJob.setSource(Long.parseLong(sourceProfileId));
        backupJob.setSink(Long.parseLong(sinkProfileId));

        for (String actionProfileId : actionProfileIds) {
            backupJob.addAction(Long.parseLong(actionProfileId));
        }

        return backupJob;
    }
    
    public static WorkerInfoDTO getWorkerInfo() {
        WorkerInfoDTO workerInfo = new WorkerInfoDTO();

        workerInfo.setWorkerId(UUID.randomUUID().toString());
        workerInfo.setWorkerName("Hostname");
        workerInfo.setOsName(System.getProperty("os.name"));
        workerInfo.setOsVersion(System.getProperty("os.version"));
        workerInfo.setOsArchitecture(System.getProperty("os.arch"));
        workerInfo.setTotalMemory(Runtime.getRuntime().totalMemory());
        workerInfo.setTotalCPUCores(Runtime.getRuntime().availableProcessors());
        long totalSpace = new File("/").getTotalSpace();
        workerInfo.setTotalSpace(totalSpace);

        return workerInfo;
    }
    
    public static List<WorkerMetricDTO> getWorkerMetrics() {
        List<WorkerMetricDTO> metrics = new ArrayList<>();
        metrics.add(new WorkerMetricDTO(new Date(), "metric1", 47.11));
        return metrics;
    }
}

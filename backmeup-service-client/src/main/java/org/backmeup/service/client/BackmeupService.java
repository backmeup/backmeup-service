package org.backmeup.service.client;

import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.service.client.model.auth.AuthInfo;

public interface BackmeupService {
	AuthInfo authenticate(String username, String password);

	BackupJobDTO getBackupJob(Long jobId);

	BackupJobDTO updateBackupJob(BackupJobDTO backupJob);
}

package org.backmeup.service.client;

import org.backmeup.model.dto.BackupJobDTO;

public interface BackmeupServiceFacade {
	BackupJobDTO getBackupJob(Long jobId);
	
	BackupJobDTO updateBackupJob(BackupJobDTO backupJob);	
}

package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.BackupJobExecution;

public interface BackupJobExecutionDao extends BaseDao<BackupJobExecution> {

    List<BackupJobExecution> findByUserId(Long userId);

    List<BackupJobExecution> findByBackupJobId(Long jobId);

    List<BackupJobExecution> findAll();
}

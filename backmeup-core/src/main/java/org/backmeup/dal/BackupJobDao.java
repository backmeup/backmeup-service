package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.BackupJob;

public interface BackupJobDao extends BaseDao<BackupJob> {

    List<BackupJob> findByUsername(String username);

    BackupJob findLastBackupJob(String username);

    List<BackupJob> findAll();
}

package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.BackupJob;

public interface BackupJobDao extends BaseDao<BackupJob> {

    List<BackupJob> findByUserId(Long userId);

    List<BackupJob> findAll();
}

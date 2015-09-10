package org.backmeup.dal.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.BackupJobExecutionDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.FriendlistDao;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.UserDao;
import org.backmeup.dal.WorkerInfoDao;
import org.backmeup.dal.WorkerMetricDao;

/**
 * The DataAccessLayerImpl uses JPA to interact with the underlying database.
 * 
 * @author fschoeppl
 * 
 */
@ApplicationScoped
public class DataAccessLayerImpl implements DataAccessLayer {
    private final ThreadLocal<EntityManager> threadLocalEntityManager = new ThreadLocal<>();

    public DataAccessLayerImpl() {
    }

    @Override
    public UserDao createUserDao() {
        return new UserDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public ProfileDao createProfileDao() {
        return new ProfileDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public BackupJobDao createBackupJobDao() {
        return new BackupJobDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public BackupJobExecutionDao createBackupJobExecutionDao() {
        return new BackupJobExecutionDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public AuthDataDao createAuthDataDao() {
        return new AuthDataDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public WorkerInfoDao createWorkerInfoDao() {
        return new WorkerInfoDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public WorkerMetricDao createWorkerMetricDao() {
        return new WorkerMetricDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public FriendlistDao createFriendlistDao() {
        return new FriendlistDaoImpl(this.threadLocalEntityManager.get());
    }

    @Override
    public void setConnection(Object connection) {
        this.threadLocalEntityManager.set((EntityManager) connection);
    }
}

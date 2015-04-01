package org.backmeup.dal.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.BackupJobExecutionDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.JobProtocolDao;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.UserDao;

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
        return new UserDaoImpl(threadLocalEntityManager.get());
    }

    @Override
    public ProfileDao createProfileDao() {
        return new ProfileDaoImpl(threadLocalEntityManager.get());
    }

    @Override
    public BackupJobDao createBackupJobDao() {
        return new BackupJobDaoImpl(threadLocalEntityManager.get());
    }
    
    public BackupJobExecutionDao createBackupJobExecutionDao() {
        return new BackupJobExecutionDaoImpl(threadLocalEntityManager.get());
    }

    @Override
    public JobProtocolDao createJobProtocolDao() {
        return new JobProtocolDaoImpl(threadLocalEntityManager.get());
    }

    @Override
    public AuthDataDao createAuthDataDao() {
        return new AuthDataDaoImpl(threadLocalEntityManager.get());
    }

    @Override
    public void setConnection(Object connection) {
        this.threadLocalEntityManager.set((EntityManager) connection);
    }
}

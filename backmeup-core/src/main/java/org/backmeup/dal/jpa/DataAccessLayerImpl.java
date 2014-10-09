package org.backmeup.dal.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.JobProtocolDao;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.ServiceDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.dal.UserDao;

/**
 * The DataAccessLayerImpl uses JPA to interact with the underlying database.
 * 
 * @author fschoeppl
 * 
 */
@ApplicationScoped
public class DataAccessLayerImpl implements DataAccessLayer {
	private final ThreadLocal<EntityManager> threaLocalEntityManager = new ThreadLocal<>();

	public DataAccessLayerImpl() {
	}

	@Override
	public UserDao createUserDao() {
		return new UserDaoImpl(threaLocalEntityManager.get());
	}

	@Override
	public ProfileDao createProfileDao() {
		return new ProfileDaoImpl(threaLocalEntityManager.get());
	}

	@Override
	public StatusDao createStatusDao() {
		return new StatusDaoImpl(threaLocalEntityManager.get());
	}

	@Override
	public BackupJobDao createBackupJobDao() {
		return new BackupJobDaoImpl(threaLocalEntityManager.get());
	}

	@Override
	public ServiceDao createServiceDao() {
		return new ServiceDaoImpl(threaLocalEntityManager.get());
	}

	@Override
	public JobProtocolDao createJobProtocolDao() {
		return new JobProtocolDaoImpl(threaLocalEntityManager.get());
	}

	@Override
	public void setConnection(Object connection) {
		this.threaLocalEntityManager.set((EntityManager) connection);
	}
}

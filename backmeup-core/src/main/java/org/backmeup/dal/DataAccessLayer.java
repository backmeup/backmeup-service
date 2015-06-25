package org.backmeup.dal;

/**
 * The DataAccessLayer provides access to any kind of database. 
 * It uses Data Access Objects (e.g. UserDao) to store, retrieve and delete data of a certain database.
 * 
 * @author fschoeppl
 */
public interface DataAccessLayer {

    UserDao createUserDao();

    ProfileDao createProfileDao();

    BackupJobDao createBackupJobDao();

    BackupJobExecutionDao createBackupJobExecutionDao();

    AuthDataDao createAuthDataDao();

    void setConnection(Object connection);

}

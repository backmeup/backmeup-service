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

    StatusDao createStatusDao();

    BackupJobDao createBackupJobDao();

    ServiceDao createServiceDao();

    JobProtocolDao createJobProtocolDao();

    SearchResponseDao createSearchResponseDao();

    void setConnection(Object connection);

}

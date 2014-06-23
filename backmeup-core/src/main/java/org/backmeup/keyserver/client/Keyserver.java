package org.backmeup.keyserver.client;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.Token;

public interface Keyserver {
    // User operations
    void registerUser(Long userId, String password);

    boolean isUserRegistered(Long userId);

    void deleteUser(Long userId);

    boolean validateUser(Long userId, String password);

    void changeUserPassword(Long userId, String oldPassword, String newPassword);

    void changeUserKeyRing(Long userId, String oldKeyRing, String newKeyRing);

    //Service operations
    void addService(Long serviceId);

    boolean isServiceRegistered(Long serviceId);

    void deleteService(Long serviceId);

    //Authentication information
    void addAuthInfo(Long userId, String userPwd, Long serviceId, Long authInfoId, Properties keyValuePairs);

    void addAuthInfo(Profile profile, String userPwd, Properties keyValuePairs);

    boolean isAuthInformationAvailable(Long authInfoId, Long userId, Long serviceId, String userPwd);

    boolean isAuthInformationAvailable(Profile profile, String userPwd);

    void deleteAuthInfo(Long authInfoId);

    // Token operations
    Token getToken(Long userId, String userPwd, Long[] services, Long[] authinfos, Long backupdate, boolean reusable, String encryptionPwd);

    Token getToken(Profile profile, String userPwd, Long backupdate, boolean reusable, String encryptionPwd);

    Token getToken(BackupJob job, String userPwd, Long backupdate, boolean reusable, String encryptionPwd);

    AuthDataResult getData(Token token);

    // Logs
    List<KeyserverLog> getLogs(BackMeUpUser user);
}

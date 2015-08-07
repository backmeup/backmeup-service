package org.backmeup.logic;

import java.util.Date;
import java.util.Set;

import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.model.BackMeUpUser;

/**
 * Sharing related business logic common for both SharingLogic and HeritageLogic.
 * 
 */
public interface SharingLogicCommons {

    Set<SharingPolicyEntry> getAllOwned(BackMeUpUser owner);

    Set<SharingPolicyEntry> getAllIncoming(BackMeUpUser forUser);

    SharingPolicyEntry add(BackMeUpUser owner, BackMeUpUser sharingWith, SharingPolicyTypeEntry policy,
            String sharedElementID, String name, String description, Date lifespanstart, Date lifespanend);

    SharingPolicyEntry updateOwned(BackMeUpUser owner, Long policyID, String name, String description,
            Date lifespanstart, Date lifespanend);

    String removeOwned(BackMeUpUser owner, Long policyID);

}
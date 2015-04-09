package org.backmeup.logic;

import java.util.Set;

import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.model.BackMeUpUser;

/**
 * Sharing related business logic.
 * 
 */
public interface SharingLogic {

    Set<SharingPolicyEntry> getAllOwned(BackMeUpUser owner);

    Set<SharingPolicyEntry> getAllIncoming(BackMeUpUser forUser);

    SharingPolicyEntry add(BackMeUpUser owner, BackMeUpUser sharingWith, SharingPolicyTypeEntry policy,
            String sharedElementID);

    String removeOwned(BackMeUpUser owner, Long policyID);

    String removeAllOwned(BackMeUpUser owner);

}
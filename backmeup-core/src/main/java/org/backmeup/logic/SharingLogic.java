package org.backmeup.logic;

import org.backmeup.model.BackMeUpUser;

/**
 * Sharing related business logic.
 * 
 */
public interface SharingLogic extends SharingLogicCommons {

    String removeAllOwned(BackMeUpUser owner);

    String acceptIncomingSharing(BackMeUpUser user, Long policyID);

    String declineIncomingSharing(BackMeUpUser user, Long policyID);

}
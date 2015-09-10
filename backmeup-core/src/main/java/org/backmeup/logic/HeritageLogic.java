package org.backmeup.logic;

import org.backmeup.model.BackMeUpUser;

/**
 * Heritage Sharing related business logic.
 * 
 */
public interface HeritageLogic extends SharingLogicCommons {

    String activateDeadMannSwitchAndImport(BackMeUpUser owner);

}
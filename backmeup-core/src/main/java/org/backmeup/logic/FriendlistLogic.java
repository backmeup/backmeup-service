package org.backmeup.logic;

import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FriendlistUser;

/**
 * Friendlist related business logic.
 * 
 */
public interface FriendlistLogic {

    FriendlistUser addFriend(BackMeUpUser currUser, FriendlistUser friend);

    List<FriendlistUser> getFriends(BackMeUpUser currUser);

}
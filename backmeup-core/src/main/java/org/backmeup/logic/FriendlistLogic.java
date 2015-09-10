package org.backmeup.logic;

import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;

/**
 * Friendlist related business logic.
 * 
 */
public interface FriendlistLogic {

    FriendlistUser addFriend(BackMeUpUser currUser, FriendlistUser friend);

    List<FriendlistUser> getFriends(BackMeUpUser currUser, FriendListType friendlist);

    void removeFriend(BackMeUpUser currUser, Long friendId, FriendListType friendlist);

    FriendlistUser updateFriend(BackMeUpUser currUser, FriendlistUser friend);

}
package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;

/**
 * The FriendlistDao contains database relevant operations for the model class FriendlistUser.
 * 
 */
public interface FriendlistDao extends BaseDao<FriendlistUser> {

    List<FriendlistUser> getFriends(Long ownerId, FriendListType friendlist);

    FriendlistUser getFriend(Long ownerId, Long friendId, FriendListType friendlist);

}

package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.FriendlistUser;

/**
 * The FriendlistDao contains database relevant operations for the model class FriendlistUser.
 * 
 */
public interface FriendlistDao extends BaseDao<FriendlistUser> {

    List<FriendlistUser> getFriends(Long ownerId);

    FriendlistUser getFriend(Long ownerId, Long friendId);

    List<FriendlistUser> getHeritageFriends(Long ownerId);

    FriendlistUser getHeritageFriend(Long ownerId, Long friendId);

}

package org.backmeup.logic.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.FriendlistDao;
import org.backmeup.logic.FriendlistLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;
import org.backmeup.model.exceptions.NotAnEmailAddressException;
import org.backmeup.model.exceptions.UnknownFriendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class FriendlistLogicImpl implements FriendlistLogic {

    private static final Logger log = LoggerFactory.getLogger(FriendlistLogicImpl.class);

    @Inject
    @Configuration(key = "backmeup.emailRegex")
    private String emailRegex;

    @Inject
    private DataAccessLayer dal;

    private FriendlistDao getFriendlistDao() {
        return this.dal.createFriendlistDao();
    }

    @Override
    public FriendlistUser addFriend(BackMeUpUser currUser, FriendlistUser friend) {
        //verify a valid email address was provided
        throwIfEmailInvalid(friend.getEmail());
        if (friend.getOwnerId() == null) {
            friend.setOwnerId(currUser.getUserId());
        }
        friend = this.getFriendlistDao().save(friend);
        log.debug("added and persisted friend for userId: " + currUser.getUserId() + " " + friend.toString());

        //add the bmu user id for the friend if he's a BMU user - field is transient
        setFriendsBMUUserId(friend);
        return friend;
    }

    @Override
    public List<FriendlistUser> getFriends(BackMeUpUser currUser, FriendListType friendlist) {
        List<FriendlistUser> ret = this.getFriendlistDao().getFriends(currUser.getUserId(), friendlist);
        setFriendsBMUUserId(ret);
        return ret;
    }

    @Override
    public void removeFriend(BackMeUpUser currUser, Long friendId, FriendListType friendlist) {
        FriendlistUser friend = getFriendFromDB(currUser, friendId, friendlist);
        this.getFriendlistDao().delete(friend);
        log.debug("deleted friend for userId: " + currUser.getUserId() + " and friendId:" + friendId);
    }

    @Override
    public FriendlistUser updateFriend(BackMeUpUser currUser, FriendlistUser friendUpdate) {
        //we only allow updating name and description for friends
        if (friendUpdate.getEntityId() == null) {
            throw new UnknownFriendException("entityId required");
        }
        FriendlistUser dbFriend = getFriendFromDB(currUser, friendUpdate.getEntityId(),
                friendUpdate.getFriendListType());
        if (friendUpdate.getName() != null) {
            dbFriend.setName(friendUpdate.getName());
        }
        if (friendUpdate.getDescription() != null) {
            dbFriend.setDescription(friendUpdate.getDescription());
        }
        dbFriend = this.getFriendlistDao().merge(dbFriend);
        log.debug("updated friend for userId: " + currUser.getUserId() + " " + dbFriend.toString());
        return dbFriend;
    }

    //---------------------------private helpers -----------------------------//

    private FriendlistUser getFriendFromDB(BackMeUpUser currUser, Long friendId, FriendListType friendlist) {
        if (friendId == null) {
            throw new UnknownFriendException("friendId not provided");
        }
        //check if the provided friendId is 'owned' by this backmeup user
        FriendlistUser friend = this.getFriendlistDao().getFriend(currUser.getUserId(), friendId, friendlist);
        if (friend == null) {
            throw new UnknownFriendException(friendId);
        }
        return friend;
    }

    private void throwIfEmailInvalid(String email) {
        Pattern emailPattern = Pattern.compile(this.emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(email);
        if (!emailMatcher.matches()) {
            throw new NotAnEmailAddressException(this.emailRegex, email);
        }
    }

    private void setFriendsBMUUserId(FriendlistUser friend) {
        //lookup if this friend is registered at BMU and add his BMUUserId if so
        BackMeUpUser friendBMUUser = lookupEmailForBMUUserId(friend.getEmail());
        if (friendBMUUser != null) {
            friend.setFriendsBmuUserId(friendBMUUser.getUserId());
        }
    }

    private void setFriendsBMUUserId(List<FriendlistUser> lFriends) {
        for (FriendlistUser friend : lFriends) {
            setFriendsBMUUserId(friend);
        }
    }

    private BackMeUpUser lookupEmailForBMUUserId(String email) {
        return this.dal.createUserDao().findByEmail(email);
    }

}

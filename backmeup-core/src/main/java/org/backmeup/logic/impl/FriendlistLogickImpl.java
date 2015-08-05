package org.backmeup.logic.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.FriendlistDao;
import org.backmeup.logic.FriendlistLogick;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.exceptions.NotAnEmailAddressException;

@ApplicationScoped
public class FriendlistLogickImpl implements FriendlistLogick {

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

        //add the bmu user id for the friend if he's a BMU user - field is transient
        setFriendsBMUUserId(friend);
        return friend;
    }

    @Override
    public List<FriendlistUser> getFriends(BackMeUpUser currUser) {
        List<FriendlistUser> ret = this.getFriendlistDao().getFriends(currUser.getUserId());
        setFriendsBMUUserId(ret);
        return ret;
    }

    private void throwIfEmailInvalid(String email) {
        Pattern emailPattern = Pattern.compile(this.emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(email);
        if (!emailMatcher.matches()) {
            throw new NotAnEmailAddressException(this.emailRegex, email);
        }
    }

    //---------------------------private helpers -----------------------------//

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

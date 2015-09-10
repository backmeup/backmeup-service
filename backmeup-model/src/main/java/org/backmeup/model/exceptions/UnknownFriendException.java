package org.backmeup.model.exceptions;

/**
 * An operation might fail because the given user is unknown to the system. This exception should be thrown then.
 * 
 * @author fschoeppl
 *
 */
public class UnknownFriendException extends BackMeUpException {
    private static final long serialVersionUID = 1L;
    private static final String UNKNOWN_FRIEND = "Unknown or non existing friend";

    private final String friendname;

    public UnknownFriendException(String friendname) {
        super(UNKNOWN_FRIEND);
        this.friendname = friendname;
    }

    public UnknownFriendException(Long friendId) {
        super(UNKNOWN_FRIEND);
        this.friendname = Long.toString(friendId);
    }

    public String getFriend() {
        return this.friendname;
    }
}

package org.backmeup.model;

public class TestUser {

    public static final long ACTIVE_USER_ID = 1;

    public static BackMeUpUser createActive() {
        BackMeUpUser user = new BackMeUpUser();
        user.setActivated(true);
        user.setEmail("me@some.com");
        user.setFirstname("Hans");
        user.setLastname("Mayer");
        user.setUserId(ACTIVE_USER_ID);
        user.setUsername("hmayer");
        return user;
    }

}

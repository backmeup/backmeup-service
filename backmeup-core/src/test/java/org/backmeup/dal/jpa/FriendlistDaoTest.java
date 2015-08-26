package org.backmeup.dal.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class FriendlistDaoTest {

    @Rule
    public final DerbyDatabase database = new DerbyDatabase();
    private FriendlistUser user1, user2, user3, user4;
    private BackMeUpUser bmu1, bmu2;

    @Test
    public void getFriendLists() {
        List<FriendlistUser> friends = this.database.friendlistDao.getFriends(1L);
        assertNotNull(friends);
        assertTrue(friends.size() == 2);

        friends = this.database.friendlistDao.getFriends(2L);
        assertNotNull(friends);
        assertTrue(friends.size() == 1);
    }

    @Test
    public void getHeritageFriendLists() {
        List<FriendlistUser> friends = this.database.friendlistDao.getHeritageFriends(1L);
        assertNotNull(friends);
        assertTrue(friends.size() == 0);

        friends = this.database.friendlistDao.getHeritageFriends(2L);
        assertNotNull(friends);
        assertTrue(friends.size() == 1);
    }

    @Test
    public void getFriend() {
        FriendlistUser friend = this.database.friendlistDao.getFriend(1L, this.user1.getEntityId());
        assertNotNull(friend);
        assertEquals("Name1", friend.getName());
        assertEquals("Description1", friend.getDescription());
        assertEquals("bmuuser1@test.com", friend.getEmail());
        assertEquals(FriendListType.SHARING, friend.getFriendListType());
    }

    @Test
    public void getHeritageFriend() {
        FriendlistUser friend = this.database.friendlistDao.getHeritageFriend(2L, this.user4.getEntityId());
        assertNotNull(friend);
        assertEquals(FriendListType.HERITAGE, friend.getFriendListType());

        friend = this.database.friendlistDao.getHeritageFriend(2L, this.user3.getEntityId());
        assertNull(friend);
    }

    @Test
    public void getBMUUserIdOfFriend() {
        FriendlistUser friend = this.database.friendlistDao.getFriend(1L, this.user1.getEntityId());
        assertNotNull(friend);
        BackMeUpUser foundUser = this.database.userDao.findByEmail(this.user1.getEmail());
        assertNotNull(foundUser);

        friend = this.database.friendlistDao.getFriend(1L, this.user2.getEntityId());
        assertNotNull(friend);
        foundUser = this.database.userDao.findByEmail(this.user2.getEmail());
        assertNull(foundUser);
    }

    @Before
    public void createTestdata() {

        this.bmu1 = new BackMeUpUser("owner1", "Max", "Mustermann", "bmuuser1@test.com", "abcDEFGH!&/L");
        this.bmu2 = new BackMeUpUser("owner2", "Anita", "Musterfrau", "bmuuser2@test.com", "abcDEFGH!&/M");
        this.bmu1 = persistInTransaction(this.bmu1);
        this.bmu2 = persistInTransaction(this.bmu2);

        this.user1 = new FriendlistUser(1L, "Name1", "Description1", "bmuuser1@test.com");
        this.user2 = new FriendlistUser(1L, "Name2", "Description2", "random11@mail.com");
        this.user3 = new FriendlistUser(2L, "Name3", "Description3", "bmuuser2@test.com", FriendListType.SHARING);
        this.user4 = new FriendlistUser(2L, "Name4", "Description4", "bmuuser2@test.com", FriendListType.HERITAGE);
        this.user1 = persistInTransaction(this.user1);
        this.user2 = persistInTransaction(this.user2);
        this.user3 = persistInTransaction(this.user3);
        this.user4 = persistInTransaction(this.user4);
    }

    private FriendlistUser persistInTransaction(FriendlistUser friend) {
        // need manual transaction in test because transactional interceptor is not installed in tests
        this.database.entityManager.getTransaction().begin();
        friend = this.database.friendlistDao.save(friend);
        this.database.entityManager.getTransaction().commit();
        return friend;
    }

    private BackMeUpUser persistInTransaction(BackMeUpUser user) {
        // need manual transaction in test because transactional interceptor is not installed in tests
        this.database.entityManager.getTransaction().begin();
        user = this.database.userDao.save(user);
        this.database.entityManager.getTransaction().commit();
        return user;
    }

}

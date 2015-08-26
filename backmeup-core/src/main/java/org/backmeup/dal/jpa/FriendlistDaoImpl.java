package org.backmeup.dal.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.FriendlistDao;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;

/**
 * Implementation of the FriendlistDao interface
 * 
 *
 */
public class FriendlistDaoImpl extends BaseDaoImpl<FriendlistUser> implements FriendlistDao {

    private static final String TABLENAME = FriendlistUser.class.getSimpleName();

    public FriendlistDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<FriendlistUser> getFriends(Long ownerId, FriendListType friendlist) {
        return getFriendsOfType(ownerId, friendlist);
    }

    @Override
    public FriendlistUser getFriend(Long ownerId, Long friendId, FriendListType friendlist) {
        return getFriendOfType(ownerId, friendId, friendlist);
    }

    private List<FriendlistUser> getFriendsOfType(Long ownerId, FriendListType... types) {
        List<FriendListType> lTypes = Arrays.asList(types);
        TypedQuery<FriendlistUser> q = createTypedQuery("SELECT u FROM " + TABLENAME
                + " u WHERE u.ownerId = :ownerID and u.friendListType IN (:statusTypes) ORDER BY u.entityId ASC");
        q.setParameter("ownerID", ownerId);
        q.setParameter("statusTypes", lTypes);
        return executeQuery(q);
    }

    private FriendlistUser getFriendOfType(Long ownerId, Long friendId, FriendListType... types) {
        List<FriendListType> lTypes = Arrays.asList(types);
        TypedQuery<FriendlistUser> q = createTypedQuery("SELECT u FROM "
                + TABLENAME
                + " u WHERE u.ownerId = :ownerID and u.entityId = :friendID and u.friendListType IN (:statusTypes) ORDER BY u.entityId ASC");
        q.setParameter("ownerID", ownerId);
        q.setParameter("friendID", friendId);
        q.setParameter("statusTypes", lTypes);
        return executeQuerySelectLast(q);
    }

    private TypedQuery<FriendlistUser> createTypedQuery(String sql) {
        return this.em.createQuery(sql, FriendlistUser.class);
    }

    private List<FriendlistUser> executeQuery(TypedQuery<FriendlistUser> q) {
        List<FriendlistUser> status = q.getResultList();
        if (status != null && status.size() > 0) {
            return status;
        }
        return new ArrayList<>();
    }

    private FriendlistUser executeQuerySelectLast(TypedQuery<FriendlistUser> q) {
        List<FriendlistUser> status = executeQuery(q);
        return status.size() > 0 ? status.get(status.size() - 1) : null;
    }

}

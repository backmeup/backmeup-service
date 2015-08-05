package org.backmeup.dal.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.FriendlistDao;
import org.backmeup.model.FriendlistUser;

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
    public List<FriendlistUser> getFriends(Long ownerId) {
        TypedQuery<FriendlistUser> q = createTypedQuery("SELECT u FROM " + TABLENAME
                + " u WHERE u.ownerId = :ownerID ORDER BY u.entityId ASC");
        q.setParameter("ownerID", ownerId);
        return executeQuery(q);
    }

    @Override
    public FriendlistUser getFriend(Long ownerId, Long friendId) {
        TypedQuery<FriendlistUser> q = createTypedQuery("SELECT u FROM " + TABLENAME
                + " u WHERE u.ownerId = :ownerID and u.entityId = :friendID ORDER BY u.entityId ASC");
        q.setParameter("ownerID", ownerId);
        q.setParameter("friendID", friendId);
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

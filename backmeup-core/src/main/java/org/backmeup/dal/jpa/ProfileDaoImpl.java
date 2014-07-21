package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.backmeup.dal.ProfileDao;
import org.backmeup.model.Profile;

/**
 * The ProfileDaoImpl realizes the ProfileDao interface with 
 * JPA specific operations.
 * 
 * 
 * @author fschoeppl
 *
 */
public class ProfileDaoImpl extends BaseDaoImpl<Profile> implements ProfileDao {

	public ProfileDaoImpl(EntityManager em) {
		super(em);
	}

	@Override
    @SuppressWarnings("unchecked")
	public List<Profile> findDatasourceProfilesByUserId(Long userId) {		
		Query q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE p.user.userid = :userid AND p.sourceAndOrSink IN ('Source', 'Both')");
		q.setParameter("userid", userId);
		List<Profile> profiles = q.getResultList();		
		return profiles;
	}
	
	@Override
    @SuppressWarnings("unchecked")
  public List<Profile> findDatasinkProfilesByUserId(Long userId) {    
    Query q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE p.user.userid = :userid AND p.sourceAndOrSink IN ('Sink', 'Both')");
    q.setParameter("userid", userId);
    List<Profile> profiles = q.getResultList();   
    return profiles;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Profile> findProfilesByUserIdAndService(Long userId,
      String sourceSinkId) {
    Query q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE p.user.userid = :userid AND p.desc = :id ");
    q.setParameter("userid", userId);        
    q.setParameter("id", sourceSinkId);
    List<Profile> profiles = q.getResultList(); 
    return profiles;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Profile> findProfilesByUserId(Long userId) {
    Query q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE p.user.userid = :userid");
    q.setParameter("userid", userId);            
	List<Profile> profiles = q.getResultList();
    return profiles;
  }

}

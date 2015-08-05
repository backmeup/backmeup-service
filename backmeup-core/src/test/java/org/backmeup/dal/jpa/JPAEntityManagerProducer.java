package org.backmeup.dal.jpa;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAEntityManagerProducer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Produces
    @RequestScoped
    public EntityManager createConnectionForEachRequest() {
        EntityManager manager = createNewManager();
        log.debug("Created new EntityManager for request " + manager);
        return manager;
    }

    private EntityManager createNewManager() {
        return this.entityManagerFactory.createEntityManager();
    }

    public void destroy(@Disposes EntityManager manager) {
        if (manager.isOpen()) {
            log.debug("Destroyed EntityManager " + manager);
            manager.close();
        }
    }

}

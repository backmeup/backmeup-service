package org.backmeup.rest.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * This class creates one single EntityManager for the whole application. It
 * will be injected into the connection class of the business layer.
 */
public class EntityManagerFactoryProducer {

    private static final String PERSISTENCE_UNIT = "org.backmeup.jpa";

    @Produces
    @ApplicationScoped
    public EntityManagerFactory create() {
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    }

    public void destroy(@Disposes EntityManagerFactory factory) {
        factory.close();
    }
}

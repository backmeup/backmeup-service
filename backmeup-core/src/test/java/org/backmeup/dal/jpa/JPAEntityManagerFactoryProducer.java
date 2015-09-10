package org.backmeup.dal.jpa;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAEntityManagerFactoryProducer {

    private static final String PERSISTENCE_UNIT = "org.backmeup.jpa";
    private final Properties overwrittenJPAProps;

    public JPAEntityManagerFactoryProducer() {
        this(new Properties());
    }

    /**
     * Constructor for tests to overwrite the JPA properties.
     */
    public JPAEntityManagerFactoryProducer(Properties overwrittenJPAProps) {
        this.overwrittenJPAProps = overwrittenJPAProps;
    }

    @Produces
    @ApplicationScoped
    public EntityManagerFactory create() {
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, this.overwrittenJPAProps);
    }

    public void destroy(@Disposes EntityManagerFactory factory) {
        if (factory.isOpen())
            factory.close();
    }

}

package org.backmeup.dal.jpa;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.backmeup.dal.FriendlistDao;
import org.backmeup.dal.UserDao;
import org.junit.rules.ExternalResource;
import org.mockito.internal.util.reflection.Whitebox;

public class DerbyDatabase extends ExternalResource {

    private EntityManagerFactory entityManagerFactory;
    public EntityManager entityManager;

    public FriendlistDao friendlistDao;
    public UserDao userDao;

    @Override
    protected void before() {
        this.entityManagerFactory = new JPAEntityManagerFactoryProducer(overwrittenJPAProps()).create();
        this.entityManager = this.entityManagerFactory.createEntityManager();

        this.userDao = new UserDaoImpl(this.entityManager);
        Whitebox.setInternalState(this.userDao, "em", this.entityManager);

        this.friendlistDao = new FriendlistDaoImpl(this.entityManager);
        Whitebox.setInternalState(this.friendlistDao, "em", this.entityManager);
    }

    private Properties overwrittenJPAProps() {
        Properties overwrittenJPAProps = new Properties();

        overwrittenJPAProps.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        overwrittenJPAProps.setProperty("javax.persistence.jdbc.url", "jdbc:derby:target/junit;create=true");

        overwrittenJPAProps.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyTenSevenDialect");
        overwrittenJPAProps.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        return overwrittenJPAProps;
    }

    @Override
    protected void after() {
        this.entityManager.close();
        new JPAEntityManagerFactoryProducer().destroy(this.entityManagerFactory);
    }

}

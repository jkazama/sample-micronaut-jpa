package sample.context.orm;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.HibernateTransactionManager;

import io.micronaut.configuration.hibernate.jpa.HibernateTransactionManagerFactory;
import io.micronaut.context.annotation.*;

@Factory
@Requires(classes = HibernateTransactionManager.class)
@Replaces(factory = HibernateTransactionManagerFactory.class)
public class OrmTransactionManagerFactory {

    /**
     * @param sessionFactory The {@link SessionFactory}
     * @param dataSource     The {@link DataSource}
     * @return The {@link HibernateTransactionManager}
     */
    @Bean
    @Requires(classes = HibernateTransactionManager.class)
    @EachBean(SessionFactory.class)
    HibernateTransactionManager hibernateTransactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

}

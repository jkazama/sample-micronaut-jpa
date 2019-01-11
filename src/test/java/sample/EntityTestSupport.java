package sample;

import java.io.IOException;
import java.time.Clock;
import java.util.*;
import java.util.function.Supplier;

import javax.persistence.*;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.junit.*;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;

import io.micronaut.configuration.jdbc.hikari.*;
import io.micronaut.security.authentication.providers.PasswordEncoder;
import sample.context.*;
import sample.context.Entity;
import sample.context.actor.ActorSession;
import sample.context.orm.*;
import sample.model.*;
import sample.support.MockDomainHelperFactory;
import sample.usecase.security.HashPasswordEncoder;

/**
 * this component is specialized only in JPA which did not use container.
 * <p>Use it only with model package.
 */
public class EntityTestSupport {
    protected Clock clock = Clock.systemDefaultZone();
    protected Timestamper time;
    protected BusinessDayHandler businessDay;
    protected PasswordEncoder encoder;
    protected ActorSession session;
    protected DomainHelper dh;
    protected EntityManagerFactory emf;
    protected OrmRepository rep;
    protected PlatformTransactionManager txm;
    protected DataFixtures fixtures;
    protected Map<String, String> settingMap = new HashMap<>();

    /** Package path to be targeted for a test. (Recommend definitions of targetEntities) */
    private String packageToScan = "sample";
    /** List of Entity classes to be targeted for a test */
    private List<Class<?>> targetEntities = new ArrayList<>();

    @Before
    public final void setup() {
        setupPreset();
        dh = new MockDomainHelperFactory().create(settingMap);
        time = dh.time();
        session = dh.actorSession();
        businessDay = BusinessDayHandler.of(time);
        encoder = new HashPasswordEncoder();
        setupRepository();
        setupDataFixtures();
        before();
    }

    /** It is before rep instance create */
    protected void setupPreset() {
        // Override entity test class.
    }

    /** After rep instance created */
    protected void before() {
     // Override entity test class.
    }

    /**
     * Set target Entity in {@link #setupPreset()}.
     * (it is necessary to set targetEntities or this)
     */
    protected void targetPackage(String packageToScan) {
        this.packageToScan = packageToScan;
    }

    /**
     * Set target Entity in {@link #setupPreset()}.
     * (it is necessary to set targetPackage or this)
     */
    protected void targetEntities(Class<?>... list) {
        if (list != null) {
            this.targetEntities = Arrays.asList(list);
        }
    }

    /**
     * Set Clock which you want to use in {@link #setupPreset()}.
     */
    protected void clock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Set a mock setting value in {@link #before()}.
     */
    protected void setting(String id, String value) {
        settingMap.put(id, value);
    }

    @After
    public void cleanup() {
        emf.close();
    }

    protected void setupRepository() {
        setupEntityManagerFactory();
        rep = new TestRepository((SessionFactory)emf, dh, Optional.of(entityInterceptor()));
    }

    protected void setupDataFixtures() {
        fixtures = new DataFixtures(
                rep, txm, rep, txm, businessDay, encoder);
    }

    protected void setupEntityManagerFactory() {
        DataSource ds = EntityTestFactory.dataSource();
        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
        sfb.setDataSource(ds);
        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        sfb.setHibernateProperties(props);
        if (targetEntities.isEmpty()) {
            sfb.setPackagesToScan(packageToScan);
        } else {
            sfb.setAnnotatedClasses(targetEntities.toArray(new Class[0]));
        }
        try {
            sfb.afterPropertiesSet();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        
        this.emf = sfb.getObject();
        this.txm = new JpaTransactionManager(this.emf);
    }

    private OrmInterceptor entityInterceptor() {
        return new OrmInterceptor(session, time);
    }

    protected <T> T tx(Supplier<T> callable) {
        return TxTemplate.of(txm).tx(() -> {
            T ret = callable.get();
            if (ret instanceof Entity) {
                ret.hashCode(); // for lazy loading
            }
            return ret;
        });
    }

    protected void tx(Runnable command) {
        tx(() -> {
            command.run();
            rep.flush();
            return true;
        });
    }

    public static class EntityTestFactory {
        private static Optional<DataSource> ds = Optional.empty();

        static synchronized DataSource dataSource() {
            return ds.orElseGet(() -> {
                ds = Optional.of(createDataSource());
                return ds.get();
            });
        }

        private static DataSource createDataSource() {
            DatasourceConfiguration config = new DatasourceConfiguration("default");
            config.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            return new HikariUrlDataSource(config);
        }
    }
    
    // for UT
    public static class TestRepository extends OrmRepository {
        
        public TestRepository(SessionFactory sf, DomainHelper dh, Optional<OrmInterceptor> interceptor) {
            super(sf, dh, interceptor);
        }
        
        /** {@inheritDoc} */
        @Override
        public EntityManager em() {
            return SharedEntityManagerCreator.createSharedEntityManager(this.sf());
        }
        
    }
    
}

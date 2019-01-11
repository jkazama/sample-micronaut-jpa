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
 * Spring コンテナを用いない JPA のみに特化した検証用途。
 * <p>model パッケージでのみ利用してください。
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

    /** テスト対象とするパッケージパス(通常はtargetEntitiesの定義を推奨) */
    private String packageToScan = "sample";
    /** テスト対象とするEntityクラス一覧 */
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

    /** 設定事前処理。repインスタンス生成前 */
    protected void setupPreset() {
        // 各Entity検証で上書きしてください
    }

    /** 事前処理。repインスタンス生成後 */
    protected void before() {
        // 各Entity検証で上書きしてください
    }

    /**
     * {@link #setupPreset()}内で対象Entityを指定してください。
     * (targetEntitiesといずれかを設定する必要があります)
     */
    protected void targetPackage(String packageToScan) {
        this.packageToScan = packageToScan;
    }

    /**
     * {@link #setupPreset()}内で対象Entityを指定してください。
     * (targetPackageといずれかを設定する必要があります)
     */
    protected void targetEntities(Class<?>... list) {
        if (list != null) {
            this.targetEntities = Arrays.asList(list);
        }
    }

    /**
     * {@link #setupPreset()}内で利用したいClockを指定してください。
     */
    protected void clock(Clock clock) {
        this.clock = clock;
    }

    /**
     * {@link #before()}内でモック設定値を指定してください。
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

    /** トランザクション処理を行います。 */
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

    // 簡易コンポーネントFactory
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

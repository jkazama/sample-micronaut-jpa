package sample.context.orm;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.SessionFactory;

import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.*;
import sample.context.Entity;

/**
 * JPA ( Hibernate ) の Repository 基底実装。
 * <p>本コンポーネントは Repository と Entity の 1-n 関係を実現するために SpringData の基盤を
 * 利用しない形で単純な ORM 実装を提供します。
 * <p>OrmRepository を継承して作成される Repository の粒度はデータソース単位となります。
 * <p>DomainHelper はその特性上循環参照を誘発しやすいため、ObjectProvider を用いて依存制約を緩めています。
 */
public abstract class OrmRepository implements Repository {

    private final SessionFactory sf;
    private final DomainHelper dh;
    private final Optional<OrmInterceptor> interceptor;
    
    public OrmRepository(SessionFactory sf, DomainHelper dh, Optional<OrmInterceptor> interceptor) {
        this.sf = sf;
        this.dh = dh;
        this.interceptor = interceptor;
    }

    /**
     * 管理するEntityManagerを返します。
     */
    public EntityManager em() {
        return sf.getCurrentSession();
    }

    /** {@inheritDoc} */
    @Override
    public DomainHelper dh() {
        return dh;
    }
    
    protected Optional<OrmInterceptor> interceptor() {
        return interceptor;
    }

    /**
     * ORM操作の簡易アクセサを生成します。
     * <p>OrmTemplateは呼出しの都度生成されます。
     */
    public OrmTemplate tmpl() {
        return new OrmTemplate(em());
    }
    
    public OrmTemplate tmpl(OrmQueryMetadata metadata) {
        return new OrmTemplate(em(), metadata);
    }

    /** 指定したEntityクラスを軸にしたCriteriaを生成します。 */
    public <T extends Entity> OrmCriteria<T> criteria(Class<T> clazz) {
        return OrmCriteria.of(em(), clazz);
    }

    /** 指定したEntityクラスにエイリアスを紐付けたCriteriaを生成します。 */
    public <T extends Entity> OrmCriteria<T> criteria(Class<T> clazz, String alias) {
        return OrmCriteria.of(em(), clazz, alias);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> Optional<T> get(Class<T> clazz, Serializable id) {
        T m = em().find(clazz, id);
        if (m != null) m.hashCode(); // force loading
        return Optional.ofNullable(m);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T load(Class<T> clazz, Serializable id) {
        try {
            T m = em().getReference(clazz, id);
            m.hashCode(); // force loading
            return m;
        } catch (EntityNotFoundException e) {
            throw new ValidationException(ErrorKeys.EntityNotFound);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T loadForUpdate(Class<T> clazz, Serializable id) {
        T m = em().find(clazz, id, LockModeType.PESSIMISTIC_WRITE);
        if (m == null) throw new ValidationException(ErrorKeys.EntityNotFound);
        m.hashCode(); // force loading
        return m;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> boolean exists(Class<T> clazz, Serializable id) {
        return get(clazz, id).isPresent();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> List<T> findAll(Class<T> clazz) {
        return tmpl().loadAll(clazz);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T save(T entity) {
        interceptor().ifPresent(i -> i.touchForCreate(entity));
        em().persist(entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T saveOrUpdate(T entity) {
        interceptor().ifPresent(i -> i.touchForUpdate(entity));
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T update(T entity) {
        interceptor().ifPresent(i -> i.touchForUpdate(entity));
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T delete(T entity) {
        em().remove(entity);
        return entity;
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティを全てDBと同期(SQL発行)します。
     * <p>SQL発行タイミングを明確にしたい箇所で呼び出すようにしてください。バッチ処理などでセッションキャッシュが
     * メモリを逼迫するケースでは#flushAndClearを定期的に呼び出してセッションキャッシュの肥大化を防ぐようにしてください。
     */
    public OrmRepository flush() {
        em().flush();
        return this;
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティをDBと同期化した上でセッションキャッシュを初期化します。
     * <p>大量の更新が発生するバッチ処理などでは暗黙的に保持されるセッションキャッシュがメモリを逼迫して
     * 大きな問題を引き起こすケースが多々見られます。定期的に本処理を呼び出してセッションキャッシュの
     * サイズを定量に維持するようにしてください。
     */
    public OrmRepository flushAndClear() {
        em().flush();
        em().clear();
        return this;
    }

}

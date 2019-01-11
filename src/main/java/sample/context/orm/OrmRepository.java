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
 * Repository base implementation of JPA (Hibernate).
 * <p>Repository made in succession to OrmRepository becomes the data source unit.
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
     * Return EntityManager to manage.
     */
    public EntityManager em() {
        return sf().getCurrentSession();
    }

    /**
     * Return SessionFactory to manage.
     */
    public SessionFactory sf() {
        return sf;
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
     * Return the simple accessor of the ORM operation.
     * <p>OrmTemplate is created each call.
     */
    public OrmTemplate tmpl() {
        return new OrmTemplate(em());
    }
    
    public OrmTemplate tmpl(OrmQueryMetadata metadata) {
        return new OrmTemplate(em(), metadata);
    }

    /** Create Criteria centering on the Entity class. */
    public <T extends Entity> OrmCriteria<T> criteria(Class<T> clazz) {
        return OrmCriteria.of(em(), clazz);
    }

    /** Create Criteria which related alias with the Entity class. */
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
     * Perform DB and synchronization of all the entities
     *  which are not perpetuated in a session cache (SQL execution).
     * <p>Please call it at the point that wants to make an SQL execution timing clear.
     * You call #flushAndClear with the case that session cash is tight by batch processing in memory regularly,
     *  and please prevent enlargement of the session cash.
     */
    public OrmRepository flush() {
        em().flush();
        return this;
    }

    /**
     * Initialize session cash after having synchronized the entities 
     * which is not perpetuated in a session cache with DB.
     * <p>Session cash maintained implicitly is tight by the batch processing that mass update produces
     *  in memory and often causes a big problem and is seen.
     * You call this processing regularly, and please maintain size of the session cash in fixed-quantity.
     */
    public OrmRepository flushAndClear() {
        em().flush();
        em().clear();
        return this;
    }

}

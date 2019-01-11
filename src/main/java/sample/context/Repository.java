package sample.context;

import java.io.Serializable;
import java.util.*;

/**
 * It is general-purpose Repository which does not depend on the specific domain object.
 * <p>You can use it as Repository where a type is not safe.
 */
public interface Repository {

    /**
     * Return a helper utility to provide the access to an infrastructure layer component in the domain layer.
     */
    DomainHelper dh();

    <T extends Entity> Optional<T> get(final Class<T> clazz, final Serializable id);

    <T extends Entity> T load(final Class<T> clazz, final Serializable id);

    /**
     * Please be conscious of deadlock avoidance to perform the (for update) acquisition working under Lock.
     */
    <T extends Entity> T loadForUpdate(final Class<T> clazz, final Serializable id);

    <T extends Entity> boolean exists(final Class<T> clazz, final Serializable id);

    <T extends Entity> List<T> findAll(final Class<T> clazz);

    <T extends Entity> T save(final T entity);

    <T extends Entity> T saveOrUpdate(final T entity);

    <T extends Entity> T update(final T entity);

    <T extends Entity> T delete(final T entity);

}

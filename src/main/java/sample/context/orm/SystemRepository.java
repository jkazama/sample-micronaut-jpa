package sample.context.orm;

import java.util.Optional;

import javax.inject.Singleton;
import javax.persistence.*;

import lombok.Setter;
import sample.context.DomainHelper;

/** システムスキーマのRepositoryを表現します。 */
@Singleton
@Setter
public class SystemRepository extends OrmRepository {
    public static final String Name = "system";

    @PersistenceContext(name = Name)
    private EntityManager em;

    public SystemRepository(
            DomainHelper dh,
            Optional<OrmInterceptor> interceptor) {
        super(dh, interceptor);
    }

    @Override
    public EntityManager em() {
        return em;
    }

}

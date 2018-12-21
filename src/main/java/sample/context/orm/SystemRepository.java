package sample.context.orm;

import java.util.Optional;

import javax.inject.*;
import javax.persistence.*;

import lombok.Setter;
import sample.context.DomainHelper;

/** システムスキーマのRepositoryを表現します。 */
@Singleton
@Named(SystemRepository.Name)
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

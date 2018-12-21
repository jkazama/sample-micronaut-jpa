package sample.context.orm;

import java.util.Optional;

import javax.inject.*;
import javax.persistence.*;

import io.micronaut.context.annotation.Primary;
import lombok.Setter;
import sample.context.DomainHelper;

/** 標準スキーマのRepositoryを表現します。 */
@Singleton
@Primary
@Named(DefaultRepository.Name)
@Setter
public class DefaultRepository extends OrmRepository {
    public static final String Name = "default";

    @PersistenceContext(name = Name)
    private EntityManager em;

    public DefaultRepository(
            DomainHelper dh,
            Optional<OrmInterceptor> interceptor) {
        super(dh, interceptor);
    }
    
    @Override
    public EntityManager em() {
        return em;
    }
    
}

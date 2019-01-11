package sample.context.orm;

import java.util.Optional;

import javax.inject.*;

import org.hibernate.SessionFactory;

import io.micronaut.context.annotation.Primary;
import lombok.Setter;
import sample.context.DomainHelper;

/** Repository of the standard schema. */
@Singleton
@Primary
@Named(DefaultRepository.Name)
@Setter
public class DefaultRepository extends OrmRepository {
    public static final String Name = "default";

    public DefaultRepository(
            @Named(Name)
            SessionFactory sf,
            DomainHelper dh,
            Optional<OrmInterceptor> interceptor) {
        super(sf, dh, interceptor);
    }
    
}

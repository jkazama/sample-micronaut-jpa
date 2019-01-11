package sample.context.orm;

import java.util.Optional;

import javax.inject.*;

import org.hibernate.SessionFactory;

import sample.context.DomainHelper;

/** Repository of the system schema. */
@Singleton
@Named(SystemRepository.Name)
public class SystemRepository extends OrmRepository {
    public static final String Name = "system";

    public SystemRepository(
            @Named(Name)
            SessionFactory sf,
            DomainHelper dh,
            Optional<OrmInterceptor> interceptor) {
        super(sf, dh, interceptor);
    }

}

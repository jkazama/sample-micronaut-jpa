package sample.usecase.security;

import java.util.*;

import javax.inject.Singleton;

import org.reactivestreams.Publisher;
import org.springframework.transaction.PlatformTransactionManager;

import io.micronaut.context.annotation.Requires;
import io.micronaut.security.authentication.providers.*;
import io.reactivex.Flowable;
import sample.ValidationException;
import sample.context.orm.*;
import sample.model.account.Login;

/** UserFetcher for account */
@Singleton
@Requires(property = SecurityConstants.KeyAdmin, value = "false", defaultValue = "false")
public class AccountFetcher implements UserFetcher {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;

    public AccountFetcher(DefaultRepository rep, PlatformTransactionManager txm) {
        this.rep = rep;
        this.txm = txm;
    }

    /** {@inheritDoc} */
    @Override
    public Publisher<UserState> findByUsername(String username) {
        UserState user = TxTemplate.of(txm).readOnly().tx(() -> Login.getByLoginId(rep, username)
                .orElseThrow(() -> new ValidationException("error.login")));
        return Flowable.just(user);
    }

    /** AuthoritiesFetcher for account */
    @Singleton
    @Requires(property = SecurityConstants.KeyAdmin, value = "false", defaultValue = "false")
    public static class AccountAuthoritiesFetcher implements AuthoritiesFetcher {
        /** {@inheritDoc} */
        @Override
        public Publisher<List<String>> findAuthoritiesByUsername(String username) {
            return Flowable.just(Arrays.asList("ROLE_USER"));
        }
    }

}

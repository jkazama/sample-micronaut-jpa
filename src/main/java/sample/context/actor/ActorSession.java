package sample.context.actor;

import java.util.*;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import io.micronaut.security.utils.SecurityService;
import sample.context.actor.Actor.ActorRoleType;
import sample.usecase.security.SecurityConstants;

/**
 * The actor session of the thread local scope.
 * <p>When SecurityService is enabled, it takes precedence
 */
@Singleton
public class ActorSession {

    private final ThreadLocal<Actor> actorLocal = new ThreadLocal<>();
    private final Optional<SecurityService> security;
    private final String dummyUsername;

    public ActorSession(
            Optional<SecurityService> security,
            @Value(SecurityConstants.KeyDummyUsernameEL) String dummyUsername) {
        this.security = security;
        this.dummyUsername = dummyUsername;
    }

    /** Relate a actor with a actor session. */
    public ActorSession bind(final Actor actor) {
        actorLocal.set(actor);
        return this;
    }

    /** Unbind a actor session. */
    public ActorSession unbind() {
        actorLocal.remove();
        return this;
    }

    /**
     * Return an effective actor. When You are not related, an anonymous is returned.
     * <p>At the time of development etc. "When the authentication service is invalid and dummyUsername is valid" returns dummyUsername user
     */
    public Actor actor() {
        if (actorLocal.get() != null) {
            return actorLocal.get();
        }
        if (security.isPresent()) {
            return security.get().getAuthentication().map(auth -> {
                @SuppressWarnings("unchecked") // see AuthenticationUserDetailsAdapter
                Collection<String> roles = (Collection<String>) auth.getAttributes().get("roles");
                Actor actor;
                if (roles.contains("ROLE_ADMIN")) {
                    actor = new Actor(auth.getName(), ActorRoleType.Administrator);
                } else {
                    actor = new Actor(auth.getName(), ActorRoleType.User);
                }
                actor.getAuthorities().addAll(roles);
                return actor;
            }).orElse(Actor.Anonymous);
        } else {
            if (StringUtils.isNotEmpty(this.dummyUsername)) {
                Actor dummy = new Actor(this.dummyUsername, ActorRoleType.User);
                dummy.getAuthorities().add("ROLE_USER");
                dummy.getAuthorities().add("ROLE_ADMIN");
                return dummy;                
            } else {
                return Actor.Anonymous;
            }
        }
    }

}

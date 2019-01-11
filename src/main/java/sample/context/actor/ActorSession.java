package sample.context.actor;

import java.util.*;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import io.micronaut.security.utils.SecurityService;
import sample.context.actor.Actor.ActorRoleType;
import sample.usecase.security.SecurityConstants;

/**
 * スレッドローカルスコープの利用者セッション。
 * <p>SecurityService 利用時はそちらの情報が優先されます。
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

    /** 利用者セッションへ利用者を紐付けます。 */
    public ActorSession bind(final Actor actor) {
        actorLocal.set(actor);
        return this;
    }

    /** 利用者セッションを破棄します。 */
    public ActorSession unbind() {
        actorLocal.remove();
        return this;
    }

    /**
     * 有効な利用者を返します。認証サービスが紐付けされていない時は匿名者が返されます。
     * <p>開発時等で「認証サービスが無効かつ dummyUsername が有効な時」は、dummyUsername な利用者を返します。
     */
    public Actor actor() {
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
                Actor actor = actorLocal.get();
                return actor != null ? actor : Actor.Anonymous;
            }
        }
    }

}

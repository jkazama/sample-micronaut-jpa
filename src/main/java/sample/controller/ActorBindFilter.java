package sample.controller;

import java.util.*;

import org.reactivestreams.Publisher;

import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.*;
import io.micronaut.security.authentication.*;
import io.micronaut.security.session.SessionAuthenticationFetcher;
import io.reactivex.*;
import io.reactivex.Observable;
import sample.context.actor.*;
import sample.context.actor.Actor.ActorRoleType;

@Filter("/api/**")
public class ActorBindFilter implements HttpServerFilter {

    private final SessionAuthenticationFetcher auth;
    private final ActorSession session;
    
    public ActorBindFilter(SessionAuthenticationFetcher auth, ActorSession session) {
        this.auth = auth;
        this.session = session;
    }
    
    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        try {
            bind(request);
            return chain.proceed(request);
        } finally {
            unbind(request);
        }
    }
    
    private void bind(HttpRequest<?> request) {
        Publisher<Authentication> fetch = auth.fetchAuthentication(request);
        Observable.fromPublisher(fetch).blockingForEach(auth -> {
            String username = (String)auth.getAttributes().get("username");
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>)auth.getAttributes().get("roles");
            ActorRoleType roleType = roles.contains("ROLE_ADMIN") ? ActorRoleType.Administrator : ActorRoleType.User; 
            Actor actor = new Actor(username, username, roleType);
            this.session.bind(actor);
        });
    }
    
    private void unbind(HttpRequest<?> request) {
        this.session.unbind();
    }
    
}

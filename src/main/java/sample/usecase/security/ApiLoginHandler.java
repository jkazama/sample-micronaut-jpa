package sample.usecase.security;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.authentication.*;
import io.micronaut.security.session.*;
import io.micronaut.session.*;

/**
 * API correspondence of SessionLoginHandler.
 */
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Replaces(SessionLoginHandler.class)
public class ApiLoginHandler extends SessionLoginHandler {

    public ApiLoginHandler(SecuritySessionConfiguration securitySessionConfiguration,
            SessionStore<Session> sessionStore) {
        super(securitySessionConfiguration, sessionStore);
    }
    
    /** {@inheritDoc} */
    @Override
    public HttpResponse<?> loginSuccess(UserDetails userDetails, HttpRequest<?> request) {
        super.loginSuccess(userDetails, request);
        return HttpResponse.ok();
    }
    
    /** {@inheritDoc} */
    @Override
    public HttpResponse<?> loginFailed(AuthenticationFailed authenticationFailed) {
        return HttpResponse.unauthorized();
    }
    
}

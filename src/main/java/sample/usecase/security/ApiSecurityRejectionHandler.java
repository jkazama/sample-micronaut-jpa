package sample.usecase.security;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.security.handlers.HttpStatusCodeRejectionHandler;
import io.micronaut.security.session.SessionSecurityfilterRejectionHandler;

/**
 * API correspondence of SessionSecurityfilterRejectionHandler
 */
@Singleton
@Replaces(SessionSecurityfilterRejectionHandler.class)
public class ApiSecurityRejectionHandler extends HttpStatusCodeRejectionHandler {
    
}

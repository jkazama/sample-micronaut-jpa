package sample.controller;

import java.util.*;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import lombok.*;
import sample.context.actor.*;
import sample.usecase.AccountService;

/**
 * API controller of the account domain.
 */
@Controller("/api/account")
@Validated
public class AccountController {
    
    @SuppressWarnings("unused")
    private final AccountService service;
    private final ActorSession session;
    
    public AccountController(AccountService service, ActorSession session) {
        this.service = service;
        this.session = session;
    }
    
    @Get("/loginStatus")
    public HttpResponse<Void> loginStatus() {
        return HttpResponse.ok();
    }
    
    @Get("/loginAccount")
    public LoginAccount loginAccount() {
        return LoginAccount.of(session.actor());
    }

    /** Parameters targeted for client usage */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginAccount {
        private String id;
        private String name;
        private Collection<String> authorities;
        
        public static LoginAccount of(Actor actor) {
            return new LoginAccount(actor.getId(), actor.getName(), actor.getAuthorities());
        }
    }

}

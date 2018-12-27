package sample.controller;

import java.util.*;

import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.session.SessionAuthenticationFetcher;
import lombok.*;
import sample.usecase.AccountService;

@Controller("/api/account")
public class AccountController {
    
    @SuppressWarnings("unused")
    private final AccountService service;
    private final SessionAuthenticationFetcher auth;
    
    public AccountController(AccountService service, SessionAuthenticationFetcher auth) {
        this.service = service;
        this.auth = auth;
    }
    
    /** ログイン状態を確認します。 */
    @Get("/loginStatus")
    public HttpResponse<Void> loginStatus() {
        return HttpResponse.ok();
    }
    
    @Get("/loginAccount")
    public LoginAccount loginAccount(HttpRequest<?> req) {
        auth.fetchAuthentication(req);
        return new LoginAccount("sample", "sample", new ArrayList<>());
    }

    /** クライアント利用用途に絞ったパラメタ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginAccount {
        private String id;
        private String name;
        private Collection<String> authorities;
    }

}

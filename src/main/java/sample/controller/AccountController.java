package sample.controller;

import java.util.Collection;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import lombok.*;
import sample.usecase.AccountService;

@Controller("/api/account")
public class AccountController {
    
    @SuppressWarnings("unused")
    private final AccountService service;
    
    public AccountController(AccountService service) {
        this.service = service;
    }
    
    /** ログイン状態を確認します。 */
    @Get("/loginStatus")
    public HttpResponse<Void> loginStatus() {
        return HttpResponse.ok();
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

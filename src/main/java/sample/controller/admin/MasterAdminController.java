package sample.controller.admin;

import java.util.*;

import javax.validation.Valid;

import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import lombok.*;
import sample.model.master.Holiday.RegHoliday;
import sample.usecase.MasterAdminService;

/**
 * マスタに関わる社内のUI要求を処理します。
 */
@Controller("/api/admin/master")
@Validated
public class MasterAdminController {

    private final MasterAdminService service;

    public MasterAdminController(MasterAdminService service) {
        this.service = service;
    }

    /** 社員ログイン状態を確認します。 */
    @Get("/loginStatus")
    public HttpResponse<Void> loginStatus() {
        return HttpResponse.ok();
    }

    /** 社員ログイン情報を取得します。 */
    @Get("/loginStaff")
    public LoginStaff loadLoginStaff() {
        return new LoginStaff("sample", "sample", new ArrayList<>());
    }

    /** クライアント利用用途に絞ったパラメタ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginStaff {
        private String id;
        private String name;
        private Collection<String> authorities;
    }

    /** 休日を登録します。 */
    @Post("/holiday/")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public HttpResponse<Void> registerHoliday(@Valid RegHoliday p) {
        service.registerHoliday(p);
        return HttpResponse.ok();
    }

}

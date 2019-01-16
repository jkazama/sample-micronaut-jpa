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
 * API controller of the master domain in the organization.
 */
@Controller("/api/admin/master")
@Validated
public class MasterAdminController {

    private final MasterAdminService service;

    public MasterAdminController(MasterAdminService service) {
        this.service = service;
    }

    @Get("/loginStatus")
    public HttpResponse<Void> loginStatus() {
        return HttpResponse.ok();
    }

    @Get("/loginStaff")
    public LoginStaff loadLoginStaff() {
        return new LoginStaff("sample", "sample", new ArrayList<>());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginStaff {
        private String id;
        private String name;
        private Collection<String> authorities;
    }

    @Post("/holiday")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public HttpResponse<Void> registerHoliday(@Valid @Body RegHoliday p) {
        service.registerHoliday(p);
        return HttpResponse.ok();
    }

}

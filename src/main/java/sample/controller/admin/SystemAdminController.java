package sample.controller.admin;

import java.util.List;

import javax.validation.Valid;

import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import sample.context.AppSetting;
import sample.context.AppSetting.FindAppSetting;
import sample.context.audit.*;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.orm.PagingList;
import sample.usecase.SystemAdminService;

/**
 * API controller of the system domain in the organization.
 */
@Controller("/api/admin/system")
@Validated
public class SystemAdminController {

    private final SystemAdminService service;

    public SystemAdminController(SystemAdminService service) {
        this.service = service;
    }

    @Get(value = "/audit/actor/")
    public PagingList<AuditActor> findAuditActor(@Valid FindAuditActor p) {
        return service.findAuditActor(p);
    }

    @Get(value = "/audit/event/")
    public PagingList<AuditEvent> findAuditEvent(@Valid FindAuditEvent p) {
        return service.findAuditEvent(p);
    }

    @Get(value = "/setting/")
    public List<AppSetting> findAppSetting(@Valid FindAppSetting p) {
        return service.findAppSetting(p);
    }

    @Post("/setting/{id}")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public HttpResponse<Void> changeAppSetting(String id, String value) {
        service.changeAppSetting(id, value);
        return HttpResponse.ok();
    }

}

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
 * システムに関わる社内のUI要求を処理します。
 */
@Controller("/api/admin/system")
@Validated
public class SystemAdminController {

    private final SystemAdminService service;

    public SystemAdminController(SystemAdminService service) {
        this.service = service;
    }

    /** 利用者監査ログを検索します。 */
    @Get(value = "/audit/actor/")
    public PagingList<AuditActor> findAuditActor(@Valid FindAuditActor p) {
        return service.findAuditActor(p);
    }

    /** イベント監査ログを検索します。 */
    @Get(value = "/audit/event/")
    public PagingList<AuditEvent> findAuditEvent(@Valid FindAuditEvent p) {
        return service.findAuditEvent(p);
    }

    /** アプリケーション設定一覧を検索します。 */
    @Get(value = "/setting/")
    public List<AppSetting> findAppSetting(@Valid FindAppSetting p) {
        return service.findAppSetting(p);
    }

    /** アプリケーション設定情報を変更します。 */
    @Post("/setting/{id}")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public HttpResponse<Void> changeAppSetting(String id, String value) {
        service.changeAppSetting(id, value);
        return HttpResponse.ok();
    }

}

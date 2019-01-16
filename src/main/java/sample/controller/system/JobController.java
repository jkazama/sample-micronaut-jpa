package sample.controller.system;

import java.util.function.Supplier;

import javax.validation.Valid;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import sample.context.actor.*;
import sample.context.audit.AuditEvent;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.orm.PagingList;
import sample.usecase.*;

/**
 * API controller of the system job.
 * <p>the URL after "/api/system" assumes what is carried out from job scheduler,
 * it is necessary to make it inaccessible from the outside in L/B.
 */
@Controller("/api/system/job")
@Secured(SecurityRule.IS_ANONYMOUS)
@Validated
public class JobController {

    private final AssetAdminService asset;
    private final SystemAdminService system;
    private final ActorSession session;

    public JobController(
            AssetAdminService asset,
            SystemAdminService system,
            ActorSession session) {
        this.asset = asset;
        this.system = system;
        this.session = session;
    }

    @Post("/daily/processDay")
    public HttpResponse<Void> processDay() {
        return systemAction(() ->  {
            system.processDay();
            return HttpResponse.ok(); 
        });
    }
    
    private <T> T systemAction(Supplier<T> supplier) {
        this.session.bind(Actor.System);
        try {
            return supplier.get();
        } finally {
            this.session.unbind();
        }
    }

    @Post("/daily/closingCashOut")
    public HttpResponse<Void> closingCashOut() {
        return systemAction(() ->  {
            asset.closingCashOut();
            return HttpResponse.ok(); 
        });
    }

    @Post("/daily/realizeCashflow")
    public HttpResponse<Void> realizeCashflow() {
        return systemAction(() ->  {
            asset.realizeCashflow();
            return HttpResponse.ok(); 
        });
    }

    @Get(value = "/audit/event{?p*}")
    public PagingList<AuditEvent> findAuditEvent(@Valid FindAuditEvent p) {
        return system.findAuditEvent(p);
    }
    
}

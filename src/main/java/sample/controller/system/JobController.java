package sample.controller.system;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import sample.usecase.*;

/**
 * API controller of the system job.
 * <p>the URL after "/api/system" assumes what is carried out from job scheduler,
 * it is necessary to make it inaccessible from the outside in L/B.
 */
@Controller("/api/system/job")
@Validated
public class JobController {

    private final AssetAdminService asset;
    private final SystemAdminService system;

    public JobController(
            AssetAdminService asset,
            SystemAdminService system) {
        this.asset = asset;
        this.system = system;
    }

    @Post("/daily/processDay")
    public HttpResponse<Void> processDay() {
        system.processDay();
        return HttpResponse.ok();
    }

    @Post("/daily/closingCashOut")
    public HttpResponse<Void> closingCashOut() {
        asset.closingCashOut();
        return HttpResponse.ok();
    }

    @Post("/daily/realizeCashflow")
    public HttpResponse<Void> realizeCashflow() {
        asset.realizeCashflow();
        return HttpResponse.ok();
    }

}

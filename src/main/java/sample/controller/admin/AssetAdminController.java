package sample.controller.admin;

import java.util.List;

import javax.validation.Valid;

import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.usecase.AssetAdminService;

/**
 * API controller of the asset domain in the organization.
 */
@Controller("/api/admin/asset")
@Validated
public class AssetAdminController {

    private final AssetAdminService service;

    public AssetAdminController(AssetAdminService service) {
        this.service = service;
    }

    /** 未処理の振込依頼情報を検索します。 */
    @Get("/cio")
    public List<CashInOut> findCashInOut(@Valid FindCashInOut p) {
        return service.findCashInOut(p);
    }

}

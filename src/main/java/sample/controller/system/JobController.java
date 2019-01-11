package sample.controller.system;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import sample.usecase.*;

/**
 * システムジョブのUI要求を処理します。
 * <p>/api/system 以降の URL はジョブスケジューラから実行される事を想定しているため、 L/B 等で外部からアクセス不可にしておく必要があります。
 * ( よりベターなアプローチは該当処理のみを持ったバッチプロセスとして切り出すか個別認証をかける )
 * low: 通常はバッチプロセス(または社内プロセスに内包)を別途作成して、ジョブスケジューラから実行される方式になります。
 * ジョブの負荷がオンライン側へ影響を与えないよう事前段階の設計が重要になります。
 * low: 社内/バッチプロセス切り出す場合はVM分散時の情報/排他同期を意識する必要があります。(DB同期/メッセージング同期/分散製品の利用 等)
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

    /** 営業日を進めます。 */
    @Post("/daily/processDay")
    public HttpResponse<Void> processDay() {
        system.processDay();
        return HttpResponse.ok();
    }

    /** 振込出金依頼を締めます。 */
    @Post("/daily/closingCashOut")
    public HttpResponse<Void> closingCashOut() {
        asset.closingCashOut();
        return HttpResponse.ok();
    }

    /** キャッシュフローを実現します。 */
    @Post("/daily/realizeCashflow")
    public HttpResponse<Void> realizeCashflow() {
        asset.realizeCashflow();
        return HttpResponse.ok();
    }

}

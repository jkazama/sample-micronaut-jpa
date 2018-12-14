package sample.usecase;

import java.util.List;

import javax.inject.*;

import org.springframework.transaction.PlatformTransactionManager;

import sample.context.AppSetting;
import sample.context.AppSetting.FindAppSetting;
import sample.context.audit.*;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.orm.*;
import sample.model.BusinessDayHandler;

/**
 * システムドメインに対する社内ユースケース処理。
 */
@Singleton
public class SystemAdminService {

    private final SystemRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final BusinessDayHandler businessDay;

    public SystemAdminService(
            SystemRepository rep,
            @Named(SystemRepository.Name) PlatformTransactionManager txm,
            AuditHandler audit,
            BusinessDayHandler businessDay) {
        this.rep = rep;
        this.txm = txm;
        this.audit = audit;
        this.businessDay = businessDay;
    }

    /** 利用者監査ログを検索します。 */
    public PagingList<AuditActor> findAuditActor(FindAuditActor p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AuditActor.find(rep, p));
    }

    /** イベント監査ログを検索します。 */
    public PagingList<AuditEvent> findAuditEvent(FindAuditEvent p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AuditEvent.find(rep, p));
    }

    /** アプリケーション設定一覧を検索します。 */
    public List<AppSetting> findAppSetting(FindAppSetting p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AppSetting.find(rep, p));
    }

    public void changeAppSetting(String id, String value) {
        audit.audit("アプリケーション設定情報を変更する", () -> rep.dh().settingSet(id, value));
    }

    public void processDay() {
        audit.audit("営業日を進める", () -> rep.dh().time().proceedDay(businessDay.day(1)));
    }

}

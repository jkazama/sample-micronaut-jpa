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
 * The use case processing for the system domain in the organization.
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

    public PagingList<AuditActor> findAuditActor(FindAuditActor p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AuditActor.find(rep, p));
    }

    public PagingList<AuditEvent> findAuditEvent(FindAuditEvent p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AuditEvent.find(rep, p));
    }

    public List<AppSetting> findAppSetting(FindAppSetting p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> AppSetting.find(rep, p));
    }

    public void changeAppSetting(String id, String value) {
        audit.audit("Change application setting information.", () -> rep.dh().settingSet(id, value));
    }

    public void processDay() {
        audit.audit("Forward day.", () -> rep.dh().time().proceedDay(businessDay.day(1)));
    }

}

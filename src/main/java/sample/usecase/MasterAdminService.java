package sample.usecase;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.audit.AuditHandler;
import sample.context.orm.*;
import sample.model.master.*;
import sample.model.master.Holiday.RegHoliday;

/**
 * サービスマスタドメインに対する社内ユースケース処理。
 */
@Service
public class MasterAdminService {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;

    public MasterAdminService(
            DefaultRepository rep,
            PlatformTransactionManager txm,
            AuditHandler audit) {
        this.rep = rep;
        this.txm = txm;
        this.audit = audit;
    }

    /** 社員を取得します。 */
    public Optional<Staff> getStaff(String id) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Staff.get(rep, id));
    }

    /** 社員権限を取得します。 */
    public List<StaffAuthority> findStaffAuthority(String staffId) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> StaffAuthority.find(rep, staffId));
    }

    public void registerHoliday(final RegHoliday p) {
        audit.audit("休日情報を登録する", () -> {
            TxTemplate.of(txm).tx(() -> Holiday.register(rep, p));
        });
    }

}

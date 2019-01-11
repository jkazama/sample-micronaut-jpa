package sample.usecase;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.audit.AuditHandler;
import sample.context.orm.*;
import sample.model.master.*;
import sample.model.master.Holiday.RegHoliday;

/**
 * The use case processing for the master domain in the organization.
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

    public Optional<Staff> getStaff(String id) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Staff.get(rep, id));
    }

    public List<StaffAuthority> findStaffAuthority(String staffId) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> StaffAuthority.find(rep, staffId));
    }

    public void registerHoliday(final RegHoliday p) {
        audit.audit("Register holiday information.", () -> {
            TxTemplate.of(txm).tx(() -> Holiday.register(rep, p));
        });
    }

}

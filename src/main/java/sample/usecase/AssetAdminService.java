package sample.usecase;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Singleton;

import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.lock.IdLockHandler.LockType;
import sample.context.orm.*;
import sample.model.asset.*;
import sample.model.asset.CashInOut.FindCashInOut;

/**
 * The use case processing for the asset domain in the organization.
 */
@Singleton
@Slf4j
public class AssetAdminService {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final IdLockHandler idLock;

    public AssetAdminService(
            DefaultRepository rep,
            PlatformTransactionManager txm,
            AuditHandler audit,
            IdLockHandler idLock) {
        this.rep = rep;
        this.txm = txm;
        this.audit = audit;
        this.idLock = idLock;
    }

    public List<CashInOut> findCashInOut(final FindCashInOut p) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> CashInOut.find(rep, p));
    }

    public void closingCashOut() {
        audit.audit("Closing cash out.", () -> {
            TxTemplate.of(txm).tx(() -> closingCashOutInTx());
        });
    }

    private void closingCashOutInTx() {
        //low: It is desirable to handle it to an account unit in a mass.
        //low: Divide paging by id sort and carry it out for a difference
        // because heaps overflow when just do it in large quantities.
        CashInOut.findUnprocessed(rep).forEach(cio -> {
            idLock.call(cio.getAccountId(), LockType.Write, () -> {
                try {
                    cio.process(rep);
                    //low: Guarantee that SQL is carried out.
                    rep.flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cio.getId() + "] Failure closing cash out.", e);
                    try {
                        cio.error(rep);
                        rep.flush();
                    } catch (Exception ex) {
                        //low: Keep it for a mention only for logger which is a double obstacle. (probably DB is caused)
                    }
                }
            });
        });
    }

    /**
     * <p>Reflect the cashflow that reached an account day in the balance.
     */
    public void realizeCashflow() {
        audit.audit("Realize cashflow.", () -> {
            TxTemplate.of(txm).tx(() -> realizeCashflowInTx());
        });
    }

    private void realizeCashflowInTx() {
      //low: Expect the practice after the rollover day.
        LocalDate day = rep.dh().time().day();
        for (final Cashflow cf : Cashflow.findDoRealize(rep, day)) {
            idLock.call(cf.getAccountId(), LockType.Write, () -> {
                try {
                    cf.realize(rep);
                    rep.flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cf.getId() + "] Failure realize cashflow.", e);
                    try {
                        cf.error(rep);
                        rep.flush();
                    } catch (Exception ex) {
                    }
                }
            });
        }
    }

}

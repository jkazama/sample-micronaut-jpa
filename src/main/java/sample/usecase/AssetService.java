package sample.usecase;

import java.util.List;

import javax.inject.Singleton;

import org.springframework.transaction.PlatformTransactionManager;

import io.micronaut.context.event.ApplicationEventPublisher;
import sample.context.actor.*;
import sample.context.audit.AuditHandler;
import sample.context.lock.IdLockHandler;
import sample.context.orm.*;
import sample.model.BusinessDayHandler;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.usecase.event.AppMailEvent;
import sample.usecase.event.AppMailEvent.AppMailType;

/**
 * The customer use case processing for the asset domain.
 */
@Singleton
public class AssetService {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final ActorSession actorSession;
    private final AuditHandler audit;
    private final IdLockHandler idLock;
    private final BusinessDayHandler businessDay;
    private final ApplicationEventPublisher event;

    public AssetService(
            DefaultRepository rep,
            PlatformTransactionManager txm,
            ActorSession actorSession,
            AuditHandler audit,
            IdLockHandler idLock,
            BusinessDayHandler businessDay,
            ApplicationEventPublisher event) {
        this.rep = rep;
        this.txm = txm;
        this.actorSession = actorSession;
        this.audit = audit;
        this.idLock = idLock;
        this.businessDay = businessDay;
        this.event = event;
    }

    public List<CashInOut> findUnprocessedCashOut() {
        final String accId = actor().getId();
        return TxTemplate.of(txm).readIdLock(idLock, accId).tx(() -> {
            return CashInOut.findUnprocessed(rep, accId);
        });
    }

    private Actor actor() {
        return ServiceUtils.actorUser(actorSession.actor());
    }

    public Long withdraw(final RegCashOut p) {
        return audit.audit("requesting a withdrawal", () -> {
            p.setAccountId(actor().getId());  // The customer side overwrites in login users forcibly
            //low: Take account ID lock (WRITE) and transaction and handle transfer
            CashInOut cio = TxTemplate.of(txm).writeIdLock(idLock, actor().getId()).tx(() -> {
                return CashInOut.withdraw(rep, businessDay, p);
            });
            //low: this service e-mail it and notify user.
            event.publishEvent(AppMailEvent.of(AppMailType.FinishRequestWithdraw, cio));
            return cio.getId();
        });
    }

}

package sample.usecase.mail;

import javax.inject.Singleton;

import io.micronaut.runtime.event.annotation.EventListener;
import lombok.extern.slf4j.Slf4j;
import sample.context.mail.MailHandler;
import sample.model.asset.CashInOut;
import sample.usecase.event.AppMailEvent;

/**
 * Mail deliver of the application layer.
 * <p>Manage the transaction originally, please be careful not to call it in the transaction of the service.
 */
@Singleton
@Slf4j
public class ServiceMailDeliver {
    @SuppressWarnings("unused")
    private final MailHandler mail;

    public ServiceMailDeliver(MailHandler mail) {
        this.mail = mail;
    }

    @EventListener
    public void handleEvent(AppMailEvent<?> event) {
        switch (event.getMailType()) {
        case FinishRequestWithdraw:
            sendFinishRequestWithdraw((CashInOut)event.getValue());
            break;
        default:
            throw new IllegalStateException("Unsupported mail type. [" + event + "]");
        }
    }

    private void sendFinishRequestWithdraw(CashInOut cio) {
        //low: In this example, only log output
        log.info("Mail transmission was done. [" + cio + "]");
    }

}

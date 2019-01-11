package sample.usecase;

import java.util.Optional;

import javax.inject.*;

import org.springframework.transaction.PlatformTransactionManager;

import sample.context.orm.*;
import sample.model.account.*;

/**
 * The customer use case processing for the account domain.
 */
@Singleton
public class AccountService {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    
    public AccountService(
            DefaultRepository rep,
            PlatformTransactionManager txm) {
        this.rep = rep;
        this.txm = txm;
    }
    
    public Optional<Login> getLoginByLoginId(String loginId) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Login.getByLoginId(rep, loginId));
    }

    public Optional<Account> getAccount(String id) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Account.getValid(rep, id));
    }
    
}

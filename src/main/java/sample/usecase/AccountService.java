package sample.usecase;

import java.util.Optional;

import javax.inject.*;

import org.springframework.transaction.PlatformTransactionManager;

import sample.context.orm.*;
import sample.model.account.*;

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
    
    /** ログイン情報を取得します。 */
    public Optional<Login> getLoginByLoginId(String loginId) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Login.getByLoginId(rep, loginId));
    }

    /** 有効な口座情報を取得します。 */
    public Optional<Account> getAccount(String id) {
        return TxTemplate.of(txm).readOnly().tx(
                () -> Account.getValid(rep, id));
    }
    
}

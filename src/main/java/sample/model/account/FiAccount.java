package sample.model.account;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

/**
 * the financial institution account in an account.
 * <p>Use it by an account activity.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class FiAccount extends OrmActiveRecord<FiAccount> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    @IdStr
    private String accountId;
    @Category
    private String category;
    @Currency
    private String currency;
    @IdStr
    private String fiCode;
    @IdStr
    private String fiAccountId;

    public static FiAccount load(final OrmRepository rep, String accountId, String category, String currency) {
        return rep.tmpl().load("from FiAccount a where a.accountId=?1 and a.category=?2 and a.currency=?3", accountId,
                category, currency);
    }
}

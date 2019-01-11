package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runners.MethodSorters;

import sample.*;
import sample.ValidationException.ErrorKeys;
import sample.model.account.Account.*;
import sample.model.account.type.AccountStatusType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Account.class, Login.class);
    }

    @Override
    protected void before() {
        tx(() -> {
            fixtures.acc("normal").save(rep);
        });
    }

    @Test
    public void 口座情報を登録する() {
        tx(() -> {
            // 通常登録
            assertFalse(Account.get(rep, "new").isPresent());
            Account.register(rep, encoder, new RegAccount("new", "name", "new@example.com", "password"));
            assertThat(Account.load(rep, "new"), allOf(
                    hasProperty("name", is("name")),
                    hasProperty("mail", is("new@example.com"))));
            Login login = Login.load(rep, "new");
            assertTrue(encoder.matches("password", login.getPassword()));
            // 同一ID重複
            try {
                Account.register(rep, encoder, new RegAccount("normal", "name", "new@example.com", "password"));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(ErrorKeys.DuplicateId));
            }
        });
    }

    @Test
    public void 口座情報を変更する() {
        tx(() -> {
            Account.load(rep, "normal").change(rep, new ChgAccount("changed", "changed@example.com"));
            assertThat(Account.load(rep, "normal"), allOf(
                    hasProperty("name", is("changed")),
                    hasProperty("mail", is("changed@example.com"))));
        });
    }

    @Test
    public void 有効口座を取得する() {
        tx(() -> {
            // 通常時取得
            assertThat(Account.loadValid(rep, "normal"), allOf(
                    hasProperty("id", is("normal")),
                    hasProperty("statusType", is(AccountStatusType.Normal))));

            // 退会時取得
            Account withdrawal = fixtures.acc("withdrawal");
            withdrawal.setStatusType(AccountStatusType.Withdrawal);
            withdrawal.save(rep);
            try {
                Account.loadValid(rep, "withdrawal");
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.Account.loadValid"));
            }
        });
    }
}

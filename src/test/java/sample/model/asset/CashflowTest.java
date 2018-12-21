package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import sample.*;
import sample.ValidationException.ErrorKeys;

//low: 簡易な正常系検証が中心。依存するCashBalanceの単体検証パスを前提。
public class CashflowTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Cashflow.class, CashBalance.class);
    }

    @Test
    public void キャッシュフローを登録する() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        LocalDate basePlus1Day = businessDay.day(1);
        tx(() -> {
            // 過去日付の受渡でキャッシュフロー発生 [例外]
            try {
                Cashflow.register(rep, fixtures.cfReg("test1", "1000", baseMinus1Day));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashflowBeforeEqualsDay));
            }
            // 翌日受渡でキャッシュフロー発生
            assertThat(Cashflow.register(rep, fixtures.cfReg("test1", "1000", basePlus1Day)),
                    allOf(
                            hasProperty("amount", is(new BigDecimal("1000"))),
                            hasProperty("statusType", is(ActionStatusType.Unprocessed)),
                            hasProperty("eventDay", is(baseDay)),
                            hasProperty("valueDay", is(basePlus1Day))));
        });
    }

    @Test
    public void 未実現キャッシュフローを実現する() {
        LocalDate baseDay = businessDay.day();
        LocalDate baseMinus1Day = businessDay.day(-1);
        LocalDate baseMinus2Day = businessDay.day(-2);
        LocalDate basePlus1Day = businessDay.day(1);
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");

            // 未到来の受渡日 [例外]
            Cashflow cfFuture = fixtures.cf("test1", "1000", baseDay, basePlus1Day).save(rep);
            try {
                cfFuture.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(AssetErrorKeys.CashflowRealizeDay));
            }

            // キャッシュフローの残高反映検証。  0 + 1000 = 1000
            Cashflow cfNormal = fixtures.cf("test1", "1000", baseMinus1Day, baseDay).save(rep);
            assertThat(cfNormal.realize(rep), hasProperty("statusType", is(ActionStatusType.Processed)));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("1000"))));

            // 処理済キャッシュフローの再実現 [例外]
            try {
                cfNormal.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is(ErrorKeys.ActionUnprocessing));
            }

            // 過日キャッシュフローの残高反映検証。 1000 + 2000 = 3000
            Cashflow cfPast = fixtures.cf("test1", "2000", baseMinus2Day, baseMinus1Day).save(rep);
            assertThat(cfPast.realize(rep), hasProperty("statusType", is(ActionStatusType.Processed)));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("3000"))));
        });
    }

    @Test
    public void 発生即実現のキャッシュフローを登録する() {
        LocalDate baseDay = businessDay.day();
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");
            // 発生即実現
            Cashflow.register(rep, fixtures.cfReg("test1", "1000", baseDay));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("1000"))));
        });
    }

}

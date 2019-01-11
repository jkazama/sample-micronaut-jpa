package sample.model.asset;

/**
 * asset-domain error key constants.
 */
public interface AssetErrorKeys {

    String CashflowRealizeDay = "error.Cashflow.realizeDay";
    String CashflowBeforeEqualsDay = "error.Cashflow.beforeEqualsDay";

    String CashInOutAfterEqualsDay = "error.CashInOut.afterEqualsDay";
    String CashInOutBeforeEqualsDay = "error.CashInOut.beforeEqualsDay";
    String CashInOutWithdrawAmount = "error.CashInOut.withdrawAmount";
}

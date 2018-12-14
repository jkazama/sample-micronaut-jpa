package sample.util;

import java.math.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 計算ユーティリティ。
 * <p>単純計算の簡易化を目的とした割り切った実装なのでスレッドセーフではありません。
 */
public final class Calculator {

    private final AtomicReference<BigDecimal> value = new AtomicReference<>();
    /** 小数点以下桁数 */
    private int scale = 0;
    /** 端数定義。標準では切り捨て */
    private RoundingMode mode = RoundingMode.DOWN;
    /** 計算の都度端数処理をする時はtrue */
    private boolean roundingAlways = false;
    /** scale未設定時の除算scale値 */
    private int defaultScale = 18;

    private Calculator(Number v) {
        try {
            this.value.set(new BigDecimal(v.toString()));
        } catch (NumberFormatException e) {
            this.value.set(BigDecimal.ZERO);
        }
    }

    private Calculator(BigDecimal v) {
        this.value.set(v);
    }

    /**
     * 計算前処理定義。
     * @param scale 小数点以下桁数　
     * @return 自身のインスタンス
     */
    public Calculator scale(int scale) {
        return scale(scale, RoundingMode.DOWN);
    }

    /**
     * 計算前処理定義。
     * @param scale 小数点以下桁数
     * @param mode 端数定義
     */
    public Calculator scale(int scale, RoundingMode mode) {
        this.scale = scale;
        this.mode = mode;
        return this;
    }

    /**
     * 計算前の端数処理定義をします。
     * @param roundingAlways 計算の都度端数処理をする時はtrue
     */
    public Calculator roundingAlways(boolean roundingAlways) {
        this.roundingAlways = roundingAlways;
        return this;
    }

    /** 与えた計算値を自身が保持する値に加えます。 */
    public Calculator add(Number v) {
        try {
            add(new BigDecimal(v.toString()));
        } catch (NumberFormatException e) {
        }
        return this;
    }

    /** 与えた計算値を自身が保持する値に加えます。*/
    public Calculator add(BigDecimal v) {
        value.set(rounding(value.get().add(v)));
        return this;
    }

    private BigDecimal rounding(BigDecimal v) {
        return roundingAlways ? v.setScale(scale, mode) : v;
    }

    /** 自身が保持する値へ与えた計算値を引きます。*/
    public Calculator subtract(Number v) {
        try {
            subtract(new BigDecimal(v.toString()));
        } catch (NumberFormatException e) {
        }
        return this;
    }

    /** 自身が保持する値へ与えた計算値を引きます。 */
    public Calculator subtract(BigDecimal v) {
        value.set(rounding(value.get().subtract(v)));
        return this;
    }

    /** 自身が保持する値へ与えた計算値を掛けます。*/
    public Calculator multiply(Number v) {
        try {
            multiply(new BigDecimal(v.toString()));
        } catch (NumberFormatException e) {
        }
        return this;
    }

    /** 自身が保持する値へ与えた計算値を掛けます。*/
    public Calculator multiply(BigDecimal v) {
        value.set(rounding(value.get().multiply(v)));
        return this;
    }

    /** 与えた計算値で自身が保持する値を割ります。*/
    public Calculator divideBy(Number v) {
        try {
            divideBy(new BigDecimal(v.toString()));
        } catch (NumberFormatException e) {
        }
        return this;
    }

    /** 与えた計算値で自身が保持する値を割ります。*/
    public Calculator divideBy(BigDecimal v) {
        BigDecimal ret = roundingAlways ? value.get().divide(v, scale, mode)
                : value.get().divide(v, defaultScale, mode);
        value.set(ret);
        return this;
    }

    /** 計算結果をint型で返します。*/
    public int intValue() {
        return decimal().intValue();
    }

    /** 計算結果をlong型で返します。*/
    public long longValue() {
        return decimal().longValue();
    }

    /** 計算結果をBigDecimal型で返します。*/
    public BigDecimal decimal() {
        BigDecimal v = value.get();
        return v != null ? v.setScale(scale, mode) : BigDecimal.ZERO;
    }

    /** 開始値0で初期化されたCalculator */
    public static Calculator init() {
        return new Calculator(BigDecimal.ZERO);
    }

    /**
     * @param v 初期値
     * @return 初期化されたCalculator
     */
    public static Calculator of(Number v) {
        return new Calculator(v);
    }

    /**
     * @param v 初期値
     * @return 初期化されたCalculator
     */
    public static Calculator of(BigDecimal v) {
        return new Calculator(v);
    }

}

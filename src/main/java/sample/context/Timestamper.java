package sample.context;

import java.time.*;

import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import sample.util.*;

/**
 * 日時ユーティリティコンポーネント。
 */
@Singleton
@Setter
public class Timestamper {
    public static final String KeyDay = "system.businessDay.day";

    @Autowired(required = false)
    private AppSettingHandler setting;

    private final Clock clock;

    public Timestamper() {
        clock = Clock.systemDefaultZone();
    }

    public Timestamper(final Clock clock) {
        this.clock = clock;
    }

    /** 営業日を返します。 */
    public LocalDate day() {
        return setting == null ? LocalDate.now(clock) : DateUtils.day(setting.setting(KeyDay).str());
    }

    /** 日時を返します。 */
    public LocalDateTime date() {
        return LocalDateTime.now(clock);
    }

    /** 営業日/日時を返します。 */
    public TimePoint tp() {
        return TimePoint.of(day(), date());
    }

    /**
     * 営業日を指定日へ進めます。
     * <p>AppSettingHandlerを設定時のみ有効です。
     * @param day 更新営業日
     */
    public Timestamper proceedDay(LocalDate day) {
        if (setting != null)
            setting.update(KeyDay, DateUtils.dayFormat(day));
        return this;
    }

}

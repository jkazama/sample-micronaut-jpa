package sample.context.audit;

import java.time.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.criterion.MatchMode;

import lombok.*;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.orm.*;
import sample.context.orm.Sort.SortOrder;
import sample.model.constraints.*;
import sample.util.*;

/**
 * システム利用者の監査ログを表現します。
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditActor extends OrmActiveRecord<AuditActor> {
    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue
    private Long id;
    /** 利用者ID */
    @IdStr
    private String actorId;
    /** 利用者役割 */
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActorRoleType roleType;
    /** 利用者ソース(IP等) */
    private String source;
    /** カテゴリ */
    @Category
    private String category;
    /** メッセージ */
    private String message;
    /** 処理ステータス */
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActionStatusType statusType;
    /** エラー事由 */
    @DescriptionEmpty
    private String errorReason;
    /** 処理時間(msec) */
    private Long time;
    /** 開始日時 */
    @NotNull
    private LocalDateTime startDate;
    /** 終了日時(未完了時はnull) */
    private LocalDateTime endDate;

    /** 利用者監査ログを完了状態にします。 */
    public AuditActor finish(final OrmRepository rep) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Processed);
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    /** 利用者監査ログを取消状態にします。 */
    public AuditActor cancel(final OrmRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Cancelled);
        setErrorReason(errorReason);
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    /** 利用者監査ログを例外状態にします。 */
    public AuditActor error(final OrmRepository rep, String errorReason) {
        LocalDateTime now = rep.dh().time().date();
        setStatusType(ActionStatusType.Error);
        setErrorReason(errorReason);
        setEndDate(now);
        setTime(DateUtils.between(startDate, endDate).get().toMillis());
        return update(rep);
    }

    /** 利用者監査ログを登録します。 */
    public static AuditActor register(final OrmRepository rep, final RegAuditActor p) {
        return p.create(rep.dh().actor(), rep.dh().time().date()).save(rep);
    }

    /** 利用者監査ログを検索します。 */
    public static PagingList<AuditActor> find(final OrmRepository rep, final FindAuditActor p) {
        return rep.tmpl().find(AuditActor.class, (criteria) -> {
            return criteria
                    .like(new String[] { "actorId", "source" }, p.actorId, MatchMode.ANYWHERE)
                    .equal("category", p.category)
                    .equal("roleType", p.roleType)
                    .equal("statusType", p.statusType)
                    .like(new String[] { "message", "errorReason" }, p.keyword, MatchMode.ANYWHERE)
                    .between("startDate", p.fromDay.atStartOfDay(), DateUtils.dateTo(p.toDay));
        }, p.page.sortIfEmpty(SortOrder.desc("startDate")));
    }

    /** 検索パラメタ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindAuditActor implements Dto {
        private static final long serialVersionUID = 1l;
        @IdStrEmpty
        private String actorId;
        @CategoryEmpty
        private String category;
        @DescriptionEmpty
        private String keyword;
        @NotNull
        private ActorRoleType roleType = ActorRoleType.User;
        private ActionStatusType statusType;
        @ISODate
        private LocalDate fromDay;
        @ISODate
        private LocalDate toDay;
        @NotNull
        private Pagination page = new Pagination();
    }

    /** 登録パラメタ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegAuditActor implements Dto {
        private static final long serialVersionUID = 1l;
        private String category;
        private String message;

        public AuditActor create(final Actor actor, LocalDateTime now) {
            AuditActor audit = new AuditActor();
            audit.setActorId(actor.getId());
            audit.setRoleType(actor.getRoleType());
            audit.setSource(actor.getSource());
            audit.setCategory(category);
            audit.setMessage(ConvertUtils.left(message, 300));
            audit.setStatusType(ActionStatusType.Processing);
            audit.setStartDate(now);
            return audit;
        }

        public static RegAuditActor of(String message) {
            return of("default", message);
        }

        public static RegAuditActor of(String category, String message) {
            return new RegAuditActor(category, message);
        }
    }

}

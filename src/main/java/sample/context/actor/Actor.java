package sample.context.actor;

import java.util.Locale;

import lombok.*;
import sample.context.Dto;

/**
 * ユースケースにおける利用者を表現します。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actor implements Dto {
    private static final long serialVersionUID = 1L;

    /** 匿名利用者定数 */
    public static Actor Anonymous = new Actor("unknown", ActorRoleType.Anonymous);
    /** システム利用者定数 */
    public static Actor System = new Actor("system", ActorRoleType.System);

    /** 利用者ID */
    private String id;
    /** 利用者名称 */
    private String name;
    /** 利用者が持つ{@link ActorRoleType} */
    private ActorRoleType roleType;
    /** 利用者が使用する{@link Locale} */
    private Locale locale;
    /** 利用者の接続チャネル名称 */
    private String channel;
    /** 利用者を特定する外部情報。(IPなど) */
    private String source;

    public Actor(String id, ActorRoleType roleType) {
        this(id, id, roleType);
    }

    public Actor(String id, String name, ActorRoleType roleType) {
        this(id, name, roleType, Locale.getDefault(), null, null);
    }

    /**
     * 利用者の役割を表現します。
     */
    public static enum ActorRoleType {
        /** 匿名利用者(ID等の特定情報を持たない利用者) */
        Anonymous,
        /** 利用者(主にBtoCの顧客, BtoB提供先社員) */
        User,
        /** 内部利用者(主にBtoCの社員, BtoB提供元社員) */
        Internal,
        /** システム管理者(ITシステム担当社員またはシステム管理会社の社員) */
        Administrator,
        /** システム(システム上の自動処理) */
        System;

        public boolean isAnonymous() {
            return this == Anonymous;
        }

        public boolean isSystem() {
            return this == System;
        }

        public boolean notSystem() {
            return !isSystem();
        }
    }
}

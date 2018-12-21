package sample.context;

import javax.inject.*;

import sample.context.actor.*;

/**
 * ドメイン処理を行う上で必要となるインフラ層コンポーネントへのアクセサを提供します。
 * <p>本コンポーネントは循環参照のリスクがあるため DI 時は Provider を利用します。
 */
@Singleton
public class DomainHelper {

    private final Provider<ActorSession> actorSession;
    private final Provider<Timestamper> time;
    private final Provider<AppSettingHandler> settingHandler;
    
    public DomainHelper(
            Provider<ActorSession> actorSession,
            Provider<Timestamper> time,
            Provider<AppSettingHandler> settingHandler) {
        this.actorSession = actorSession;
        this.time = time;
        this.settingHandler = settingHandler;
    }

    /** ログイン中のユースケース利用者を取得します。 */
    public Actor actor() {
        return actorSession().actor();
    }

    /** スレッドローカルスコープの利用者セッションを取得します。 */
    public ActorSession actorSession() {
        return actorSession.get();
    }

    /** 日時ユーティリティを取得します。 */
    public Timestamper time() {
        return time.get();
    }

    /** アプリケーション設定情報を取得します。 */
    public AppSetting setting(String id) {
        return settingHandler.get().setting(id);
    }

    /** アプリケーション設定情報を設定します。 */
    public AppSetting settingSet(String id, String value) {
        return settingHandler.get().update(id, value);
    }

}

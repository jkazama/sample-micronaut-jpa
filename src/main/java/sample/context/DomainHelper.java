package sample.context;

import javax.inject.*;

import sample.context.actor.*;

/**
 * The access to the domain infrastructure layer component which is necessary in handling it.
 * <p>this component has risk of circular reference, therefore use Provider.
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

    /** Return a login user. */
    public Actor actor() {
        return actorSession().actor();
    }

    public ActorSession actorSession() {
        return actorSession.get();
    }

    public Timestamper time() {
        return time.get();
    }

    public AppSetting setting(String id) {
        return settingHandler.get().setting(id);
    }

    public AppSetting settingSet(String id, String value) {
        return settingHandler.get().update(id, value);
    }

}

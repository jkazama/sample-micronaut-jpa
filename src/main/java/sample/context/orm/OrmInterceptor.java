package sample.context.orm;

import java.time.LocalDateTime;

import javax.inject.Singleton;

import sample.context.Timestamper;
import sample.context.actor.*;

/**
 * Interceptor to insert AOP processing in a permanency timing of Entity.
 */
@Singleton
public class OrmInterceptor {

    private final ActorSession session;
    private final Timestamper time;
    
    public OrmInterceptor(ActorSession session, Timestamper time) {
        this.session = session;
        this.time = time;
    }

    public void touchForCreate(Object entity) {
        if (entity instanceof OrmActiveMetaRecord) {
            Actor staff = session.actor();
            LocalDateTime now = time.date();
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            metaEntity.setCreateId(staff.getId());
            metaEntity.setCreateDate(now);
            metaEntity.setUpdateId(staff.getId());
            metaEntity.setUpdateDate(now);
        }
    }

    public boolean touchForUpdate(final Object entity) {
        if (entity instanceof OrmActiveMetaRecord) {
            Actor staff = session.actor();
            LocalDateTime now = time.date();
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            if (metaEntity.getCreateDate() == null) {
                metaEntity.setCreateId(staff.getId());
                metaEntity.setCreateDate(now);
            }
            metaEntity.setUpdateId(staff.getId());
            metaEntity.setUpdateDate(now);
        }
        return false;
    }

}

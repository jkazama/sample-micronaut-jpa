package sample.usecase;

import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;

/**
 * Service Utilities.
 */
public abstract class ServiceUtils {

    public static Actor actorUser(Actor actor) {
        if (actor.getRoleType().isAnonymous()) {
            throw new ValidationException(ErrorKeys.Authentication);
        }
        return actor;
    }

}

package sample.controller;

import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.OptimisticLockException;
import javax.validation.*;

import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import lombok.extern.slf4j.Slf4j;
import sample.ValidationException;
import sample.ValidationException.*;
import sample.context.MessageHandler;
import sample.context.actor.ActorSession;

/**
 * Exception Map conversion support for RestController.
 * <p>Insert an exception handling by AOP advice.
 */
@Controller("/api/error")
@Slf4j
public class RestErrorAdvice {

    private final MessageHandler msg;
    private final ActorSession session;

    public RestErrorAdvice(MessageHandler msg, ActorSession session) {
        this.msg = msg;
        this.session = session;
    }

    private Locale locale() {
        return session.actor().getLocale();
    }

    @Error(global = true, exception = UnsatisfiedRouteException.class)
    public HttpResponse<Map<String, String[]>> handleUnsatisfiedRoute(UnsatisfiedRouteException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init(e.getArgument().getName(), "javax.validation.constraints.NotNull.message");
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    @Error(global = true, exception = OptimisticLockException.class)
    public HttpResponse<Map<String, String[]>> handleOptimisticLock(OptimisticLockException e) {
        log.warn(e.getMessage(), e);
        return new ErrorHolder(msg, locale(), "error.OptimisticLockingFailure").result(HttpStatus.BAD_REQUEST);
    }
    
    @Error(global = true, exception = ConversionErrorException.class)
    public HttpResponse<Map<String, String[]>> handleConversionError(ConversionErrorException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init(e.getArgument().getName(), "typeMismatch." + e.getArgument().getTypeString(false));
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }    

    /** Bean Validation (JSR303) */
    @Error(global = true, exception = ConstraintViolationException.class)
    public HttpResponse<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        e.getConstraintViolations().forEach(v -> warns.add(propKey(v), v.getMessage()));
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }
    
    // Constraint exception key (Please correct as necessary)
    private String propKey(ConstraintViolation<?> v) {
        String key = v.getPropertyPath().toString();
        if (0 > key.indexOf('.')) {
            return key;
        }
        return key.substring(key.lastIndexOf('.') + 1, key.length());
    }

    @Error(global = true, exception = ValidationException.class)
    public HttpResponse<Map<String, String[]>> handleValidation(ValidationException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), e).result(HttpStatus.BAD_REQUEST);
    }

    @Error(global = true, exception = Throwable.class)
    public HttpResponse<Map<String, String[]>> handleThrowable(Throwable e) {
        log.error("An unexpected exception occurred.", e);
        return new ErrorHolder(msg, locale(), ErrorKeys.Exception, "A problem might occur in a server side.")
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * The stack of the exception information.
     * <p> can convert the exception information that I stacked into ResponseEntity having Map by calling {@link #result(HttpStatus)}.
     * <p>The key when You registered in {@link #errorGlobal} becomes the null.
     * <p>The client-side receives a return value in [{"fieldA": "messageA"}, {"fieldB": "messageB"}].
     */
    public static class ErrorHolder {
        private Map<String, List<String>> errors = new HashMap<>();
        private MessageHandler msg;
        private Locale locale;

        public ErrorHolder(final MessageHandler msg, final Locale locale) {
            this.msg = msg;
            this.locale = locale;
        }

        public ErrorHolder(final MessageHandler msg, final Locale locale, final ValidationException e) {
            this(msg, locale, e.list());
        }

        public ErrorHolder(final MessageHandler msg, final Locale locale, final List<Warn> warns) {
            this.msg = msg;
            this.locale = locale;
            warns.forEach((warn) -> {
                if (warn.global())
                    errorGlobal(warn.getMessage());
                else
                    error(warn.getField(), warn.getMessage(), warn.getMessageArgs());
            });
        }

        public ErrorHolder(final MessageHandler msg, final Locale locale, String globalMsgKey,
                String... msgArgs) {
            this.msg = msg;
            this.locale = locale;
            errorGlobal(globalMsgKey, msgArgs);
        }

        public ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
            if (!errors.containsKey("")) {
                errors.put("", new ArrayList<>());
            }
            errors.get("").add(msg.message(msgKey, msgArgs, defaultMsg, locale));
            return this;
        }

        public ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
            return errorGlobal(msgKey, msgKey, msgArgs);
        }

        public ErrorHolder error(String field, String msgKey, String... msgArgs) {
            if (!errors.containsKey(field)) {
                errors.put(field, new ArrayList<>());
            }
            errors.get(field).add(msg.message(msgKey, msgArgs, msgKey, locale));
            return this;
        }

        /** Convert exception information to hold it into HttpResponse. */
        public HttpResponse<Map<String, String[]>> result(HttpStatus status) {
            Map<String, String[]> body = errors.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().toArray(new String[0])));
            return HttpResponse.<Map<String, String[]>> status(status).body(body);
        }
    }

}

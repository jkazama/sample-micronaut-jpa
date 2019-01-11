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
 * REST用の例外Map変換サポート。
 * <p>AOPアドバイスで全てのRestControllerに対して例外処理を当て込みます。
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

    /** リクエストマッピング時の例外 */
    @Error(global = true, exception = UnsatisfiedRouteException.class)
    public HttpResponse<Map<String, String[]>> handleUnsatisfiedRoute(UnsatisfiedRouteException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init(e.getArgument().getName(), "javax.validation.constraints.NotNull.message");
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    /** 楽観的排他(バージョンチェック)の例外 */
    @Error(global = true, exception = OptimisticLockException.class)
    public HttpResponse<Map<String, String[]>> handleOptimisticLock(OptimisticLockException e) {
        log.warn(e.getMessage(), e);
        return new ErrorHolder(msg, locale(), "error.OptimisticLockingFailure").result(HttpStatus.BAD_REQUEST);
    }
    
    /** 型マッピング時の例外 */
    @Error(global = true, exception = ConversionErrorException.class)
    public HttpResponse<Map<String, String[]>> handleConversionError(ConversionErrorException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init(e.getArgument().getName(), "typeMismatch." + e.getArgument().getTypeString(false));
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }    

    /** BeanValidation(JSR303)の制約例外 */
    @Error(global = true, exception = ConstraintViolationException.class)
    public HttpResponse<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        e.getConstraintViolations().forEach(v -> warns.add(propKey(v), v.getMessage()));
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }
    
    // 制約例外時のキー（必要に応じて修正してください）
    private String propKey(ConstraintViolation<?> v) {
        String key = v.getPropertyPath().toString();
        if (0 > key.indexOf('.')) {
            return key;
        }
        return key.substring(key.lastIndexOf('.') + 1, key.length());
    }

    /** アプリケーション例外 */
    @Error(global = true, exception = ValidationException.class)
    public HttpResponse<Map<String, String[]>> handleValidation(ValidationException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), e).result(HttpStatus.BAD_REQUEST);
    }

    /** 汎用例外 */
    @Error(global = true, exception = Throwable.class)
    public HttpResponse<Map<String, String[]>> handleThrowable(Throwable e) {
        log.error("予期せぬ例外が発生しました。", e);
        return new ErrorHolder(msg, locale(), ErrorKeys.Exception, "サーバー側で問題が発生した可能性があります。")
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 例外情報のスタックを表現します。
     * <p>スタックした例外情報は{@link #result(HttpStatus)}を呼び出す事でMapを持つHttpResponseへ変換可能です。
     * Mapのkeyはfiled指定値、valueはメッセージキーの変換値(messages-validation.properties)が入ります。
     * <p>{@link #errorGlobal}で登録した場合のキーは空文字となります。
     * <p>クライアント側は戻り値を [{"fieldA": "messageA"}, {"fieldB": "messageB"}]で受け取ります。
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

        /** グローバルな例外(フィールドキーが空)を追加します。 */
        public ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
            if (!errors.containsKey("")) {
                errors.put("", new ArrayList<>());
            }
            errors.get("").add(msg.message(msgKey, msgArgs, defaultMsg, locale));
            return this;
        }

        /** グローバルな例外(フィールドキーが空)を追加します。 */
        public ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
            return errorGlobal(msgKey, msgKey, msgArgs);
        }

        /** フィールド単位の例外を追加します。 */
        public ErrorHolder error(String field, String msgKey, String... msgArgs) {
            if (!errors.containsKey(field)) {
                errors.put(field, new ArrayList<>());
            }
            errors.get(field).add(msg.message(msgKey, msgArgs, msgKey, locale));
            return this;
        }

        /** 保有する例外情報をHttpResponseへ変換します。 */
        public HttpResponse<Map<String, String[]>> result(HttpStatus status) {
            Map<String, String[]> body = errors.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().toArray(new String[0])));
            return HttpResponse.<Map<String, String[]>> status(status).body(body);
        }
    }

}

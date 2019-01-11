package sample;

import javax.validation.MessageInterpolator;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import io.micronaut.context.annotation.*;
import io.micronaut.security.authentication.providers.PasswordEncoder;
import sample.context.MessageHandler;
import sample.usecase.security.HashPasswordEncoder;

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 */
@Factory
public class ApplicationConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new HashPasswordEncoder();
    }

    /** HibernateのLazyLoading回避対応。  see JacksonAutoConfiguration */
    @Bean
    public Hibernate5Module jsonHibernate5Module() {
        return new Hibernate5Module();
    }

    /** BeanValidationメッセージに対応したValidator。 */
    @Bean
    public MessageInterpolator defaultValidator(MessageHandler msg) {
        return new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(msg.origin()));
    }

}

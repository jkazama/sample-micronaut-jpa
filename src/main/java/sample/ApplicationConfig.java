package sample;

import javax.validation.MessageInterpolator;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import io.micronaut.context.annotation.*;
import sample.context.ResourceBundleHandler;
import sample.util.PasswordEncoder;
import sample.util.PasswordEncoder.RawPasswordEncoder;

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 */
@Factory
public class ApplicationConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        //TODO: BCryptへ変更予定
        return new RawPasswordEncoder();
    }

    /** HibernateのLazyLoading回避対応。  see JacksonAutoConfiguration */
    @Bean
    public Hibernate5Module jsonHibernate5Module() {
        return new Hibernate5Module();
    }

    /** BeanValidationメッセージに対応したValidator。 */
    @Bean
    public MessageInterpolator defaultValidator(ResourceBundleHandler resource) {
        return new ResourceBundleMessageInterpolator(resource.locator());
    }

}

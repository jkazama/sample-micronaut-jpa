package sample;

import javax.validation.MessageInterpolator;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import io.micronaut.context.annotation.*;
import io.micronaut.security.authentication.providers.PasswordEncoder;
import sample.context.MessageHandler;
import sample.usecase.security.HashPasswordEncoder;

@Factory
public class ApplicationConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new HashPasswordEncoder();
    }

    /** Invalidate Hibernate lazy loading. */
    @Bean
    public Hibernate5Module jsonHibernate5Module() {
        return new Hibernate5Module();
    }

    /** UTF8 to JSR303 message file. */
    @Bean
    public MessageInterpolator defaultValidator(MessageHandler msg) {
        return new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(msg.origin()));
    }

}

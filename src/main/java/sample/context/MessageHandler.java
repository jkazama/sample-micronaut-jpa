package sample.context;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.context.*;
import org.springframework.context.support.*;
import org.springframework.util.Assert;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * Provides easy access to ResourceBundle.
 * <p>Use this component in the message property of i18n purpose such as providing label list via API.
 * <p>Simply delegate processing to Spring's MessgeSource as internal processing.
 */
@ConfigurationProperties("extension.messages")
@Data
public class MessageHandler {

    /**
     * ResourceBundle List of resource names to be managed
     * <p>It is not necessary to include extensions (.properties).
     */
    private List<String> basename = new ArrayList<>();
    private String encoding = StandardCharsets.UTF_8.name();

    private final Map<String, ResourceBundle> bundleMap = new ConcurrentHashMap<>();

    private ReloadableResourceBundleMessageSource msg;

    @PostConstruct
    public MessageHandler build() {
        this.msg = new ReloadableResourceBundleMessageSource();
        this.basename.forEach(this.msg::addBasenames);
        this.msg.setDefaultEncoding(encoding);
        return this;
    }
    
    public MessageSource origin() {
        return this.msg;
    }

    public String message(String key) {
        return message(key, Locale.getDefault());
    }

    public String message(String key, Object[] args) {
        return messageOpt(key, args, Locale.getDefault()).orElse("");
    }

    public String message(String key, Locale locale) {
        return messageOpt(key, locale).orElse("");
    }

    public String message(String key, Object[] args, Locale locale) {
        return messageOpt(key, args, locale).orElse("");
    }

    public String message(String key, String defaultValue) {
        return message(key, null, defaultValue, Locale.getDefault());
    }

    public String message(String key, String defaultValue, Locale locale) {
        return message(key, null, defaultValue, locale);
    }

    public String message(String key, Object[] args, String defaultValue) {
        return message(key, args, defaultValue, Locale.getDefault());
    }

    public String message(String key, Object[] args, String defaultValue, Locale locale) {
        return messageOpt(key, args, locale).orElse(defaultValue);
    }

    public Optional<String> messageOpt(String key) {
        return messageOpt(key, Locale.getDefault());
    }

    public Optional<String> messageOpt(String key, Object[] args) {
        return messageOpt(key, args, Locale.getDefault());
    }

    public Optional<String> messageOpt(String key, Locale locale) {
        return messageOpt(key, null, locale);
    }

    public Optional<String> messageOpt(String key, Object[] args, Locale locale) {
        Assert.notNull(this.msg, "msg is required. Please invoke #build.");
        Assert.notNull(key, "key is required.");
        try {
            return Optional.ofNullable(msg.getMessage(key, args, locale));
        } catch (NoSuchMessageException e) {
            return Optional.empty();
        }
    }

    public ResourceBundle bundle(String basename) {
        return bundle(basename, Locale.getDefault());
    }

    public synchronized ResourceBundle bundle(String basename, Locale locale) {
        bundleMap.putIfAbsent(keyname(basename, locale), ResourceBundleFactory.create(basename, locale, encoding));
        return bundleMap.get(keyname(basename, locale));
    }

    private String keyname(String basename, Locale locale) {
        return basename + "_" + locale.toLanguageTag();
    }

    public Map<String, String> labels(String basename) {
        return labels(basename, Locale.getDefault());
    }

    public Map<String, String> labels(String basename, Locale locale) {
        ResourceBundle bundle = bundle(basename, locale);
        return bundle.keySet().stream().collect(Collectors.toMap(
                key -> key,
                key -> bundle.getString(key)));
    }

    /**
     * Factory to get ResourceBundle via Spring's MessageSource.
     * <p>Enables encoding specification of property file.
     */
    public static class ResourceBundleFactory extends ResourceBundleMessageSource {
        public static ResourceBundle create(String basename, Locale locale, String encoding) {
            ResourceBundleFactory factory = new ResourceBundleFactory();
            factory.setDefaultEncoding(encoding);
            return Optional.ofNullable(factory.getResourceBundle(basename, locale))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Not found resource file. basename: " + basename + ", locale: " + locale));
        }
    }

}

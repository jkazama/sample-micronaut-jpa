package sample.context;

import java.io.UnsupportedEncodingException;
import java.nio.charset.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.springframework.util.Assert;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * ResourceBundleに対する簡易アクセスを提供します。
 * <p>本コンポーネントはAPI経由でのラベル一覧の提供等、i18n用途のメッセージプロパティで利用してください。
 */
@ConfigurationProperties("extension.messages")
@Data
public class ResourceBundleHandler {
    public static final String Default = "messages";

    /**
     * ResourceBundle 管理対象とするリソース名一覧
     * <p>拡張子(.properties)を含める必要はありません。
     */
    private List<String> basename = new ArrayList<>();
    /** エンコーディング */
    private String encoding = StandardCharsets.UTF_8.name();

    private AggregateResourceBundleLocator locator;

    @PostConstruct
    public ResourceBundleHandler build() {
        if (basename.isEmpty()) {
            this.locator = new AggregateResourceBundleLocator(Arrays.asList(Default));
        } else {
            this.locator = new AggregateResourceBundleLocator(basename);
        }
        return this;
    }

    /** 保有するResourceBundleからメッセージキーに合致する値を返します。  */
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
        Assert.notNull(key, "key is required.");
        try {
            return Optional.ofNullable(bundle(locale).getString(key))
                    .map(v -> formatMessage(v, args, locale));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }

    private String formatMessage(String v, Object[] args, Locale locale) {
        if (args == null || args.length == 0) {
            return encode(v);
        }
        return encode(new MessageFormat(v, locale).format(args));
    }
    
    private String encode(String v) {
        if (v == null) {
            return null;
        }
        try {
            return new String(v.getBytes(StandardCharsets.ISO_8859_1), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } 
    }

    /** 保有するResourceBundleを返します。  */
    public ResourceBundle bundle() {
        return bundle(Locale.getDefault());
    }

    /** 保有するResourceBundleを返します。  */
    public synchronized ResourceBundle bundle(Locale locale) {
        Assert.notNull(locator, "locator is required. Please invoke #build.");
        ResourceBundle bundle = locator.getResourceBundle(locale);
        Assert.notNull(bundle, "「extension.messages.basename」 is required.");
        return bundle;
    }

    /** 保有するResourceBundleのメッセージキー、値のMapを返します。  */
    public Map<String, String> messages() {
        return messages(Locale.getDefault());
    }

    /** 保有するResourceBundleのメッセージキー、値のMapを返します。  */
    public Map<String, String> messages(Locale locale) {
        ResourceBundle bundle = bundle(locale);
        return bundle.keySet().stream().collect(Collectors.toMap(
                key -> key,
                key -> encode(bundle.getString(key))));
    }

    /** 内部に保有する ResourceBundleLocator を返します。 */
    public ResourceBundleLocator locator() {
        return this.locator;
    }

}

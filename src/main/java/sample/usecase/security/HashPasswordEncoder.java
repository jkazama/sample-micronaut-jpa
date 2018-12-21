package sample.usecase.security;

import java.nio.charset.StandardCharsets;
import java.security.*;

import javax.inject.Singleton;
import javax.xml.bind.DatatypeConverter;

import io.micronaut.context.annotation.Value;
import io.micronaut.security.authentication.providers.PasswordEncoder;

/** SHA-512 なハッシュエンコーダ */
@Singleton
public class HashPasswordEncoder implements PasswordEncoder {
    
    private final boolean enabled;
    
    public HashPasswordEncoder(@Value(SecurityConstants.KeyEnabled) boolean enabled) {
        this.enabled = enabled;
    }
    
    /** {@inheritDoc} */
    @Override
    public String encode(String rawPassword) {
        return rawPassword == null ? rawPassword : hash(rawPassword);
    }

    private String hash(String rawPassword) {
        try {
            return DatatypeConverter.printHexBinary(
                    MessageDigest.getInstance("SHA-512").digest(rawPassword.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (!enabled) {
            return true; // セキュリティ無効時は常に true
        }
        if (rawPassword == null) {
            return false;
        }
        return encodedPassword.equals(hash(rawPassword));
    }

}

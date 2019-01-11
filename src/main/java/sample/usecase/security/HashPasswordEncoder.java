package sample.usecase.security;

import java.nio.charset.StandardCharsets;
import java.security.*;

import javax.inject.Singleton;
import javax.xml.bind.DatatypeConverter;

import io.micronaut.security.authentication.providers.PasswordEncoder;

/** Hash encoder */
@Singleton
public class HashPasswordEncoder implements PasswordEncoder {

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
        if (rawPassword == null) {
            return false;
        }
        return encodedPassword.equals(hash(rawPassword));
    }

}

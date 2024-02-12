package bg.uni.sofia.fmi.mjt.wallet.server.database.password;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MDPasswordHasher implements PasswordHasherAPI{
    private static final String HASH_ALGO = "SHA-256";
    @Override
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while hashing the password!", e);
        }
    }
}

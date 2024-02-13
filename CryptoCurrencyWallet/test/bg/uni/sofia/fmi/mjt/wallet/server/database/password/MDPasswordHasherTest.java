package bg.uni.sofia.fmi.mjt.wallet.server.database.password;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MDPasswordHasherTest {

    @InjectMocks
    private MDPasswordHasher passwordHasher;

    @Test
    void testHashPassword() {
        String password = "testPassword";
        byte[] mockHashBytes = "mockHashBytes".getBytes();
        String hashAlgo = "SHA-256";
        MessageDigest mockMessageDigest = mock(MessageDigest.class);
        when(mockMessageDigest.digest(password.getBytes(StandardCharsets.UTF_8))).thenReturn(mockHashBytes);

        try (MockedStatic<MessageDigest> messageDigestMockedStatic = mockStatic(MessageDigest.class)) {
            messageDigestMockedStatic.when(() -> MessageDigest.getInstance(hashAlgo))
                .thenReturn(mockMessageDigest);

            String hashedPassword = passwordHasher.hashPassword(password);
            assertNotNull(hashedPassword);
            assertEquals(Base64.getEncoder().encodeToString(mockHashBytes), hashedPassword);
            messageDigestMockedStatic.verify(() -> MessageDigest.getInstance(hashAlgo), times(1));
        }
    }
}

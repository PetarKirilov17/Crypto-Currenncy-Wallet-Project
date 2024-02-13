package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.CryptoConsumerAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.SyncCryptoConsumer;
import bg.uni.sofia.fmi.mjt.wallet.server.database.Database;
import bg.uni.sofia.fmi.mjt.wallet.server.database.password.PasswordHasherAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.PasswordWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UsernameWrongFormatException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.nio.channels.SelectionKey;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CryptoWalletControllerTest {
    @Mock
    private SelectionKey selectionKey;
    @Mock
    private Database database;
    @Mock
    private CryptoAssetUpdater cryptoAssetUpdater;
    @InjectMocks
    private CryptoWalletController cryptoWalletController;

    @Test
    void testRegisterThrowsUsernameWrongFormatExceptionWhenUsernameIsNull() {
        assertThrows(UsernameWrongFormatException.class,
            () -> cryptoWalletController.register(selectionKey, null, "password"),"Register should throw UsernameWrongFormatException when username is null!");
    }

    @Test
    void testRegisterThrowsUsernameWrongFormatExceptionWhenUsernameIsInvalid(){
        assertThrows(UsernameWrongFormatException.class,
            () -> cryptoWalletController.register(selectionKey, "!InvalidUsername", "password"),"Register should throw UsernameWrongFormatException when username is not the desired format!");
    }

    @Test
    void testRegisterThrowsPasswordWrongFormatExceptionWhenPasswordIsNull(){
        assertThrows(PasswordWrongFormatException.class,
            () -> cryptoWalletController.register(selectionKey, "username", null),"Register should throw PasswordWrongFormatException when password is null!");
    }

    @Test
    void testRegisterThrowsPasswordWrongFormatExceptionWhenPasswordIsInvalid(){
        assertThrows(PasswordWrongFormatException.class,
            () -> cryptoWalletController.register(selectionKey, "username", "aa"),"Register should throw PasswordWrongFormatException when password is too short!");

        assertThrows(PasswordWrongFormatException.class,
            () -> cryptoWalletController.register(selectionKey, "username", "a".repeat(31)),"Register should throw PasswordWrongFormatException when password is too long!");
    }

    @Test
    void testRegisterThrowsUserAlreadyExistsExceptionIfUserExistsInDatabase() {
        when(database.checkIfUserExists("existingUser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
            () -> cryptoWalletController.register(selectionKey, "existingUser", "password"), "Register should throw UserAlreadyExistsException when user with this username already exists!");

        verify(database, never()).addUser(any());
        verify(selectionKey, never()).attach(any());
    }

    @Test
    void testRegisterSuccessfully()
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        String username = "testUser";
        String password = "testPassword";
        when(selectionKey.attachment()).thenReturn(null); // Simulating an unattached key
        // Executing the method
        cryptoWalletController.register(selectionKey, username, password);

        // Verifying database interactions
        verify(database, times(1)).checkIfUserExists(username);
        verify(database, times(1)).addUser(any(User.class));

        // Verifying key attachment
        verify(selectionKey, times(1)).attach(any(User.class));
        assertEquals(username, ((User)selectionKey.attachment()).getUsername());
    }
}

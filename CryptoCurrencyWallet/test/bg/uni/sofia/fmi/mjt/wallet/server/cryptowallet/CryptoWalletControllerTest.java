package bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.service.UserServiceAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.service.WalletServiceAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InsufficientBalanceException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidAssetIdException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UnauthorizedUserException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CryptoWalletControllerTest {
    @Mock
    private UserServiceAPI userService;

    @Mock
    private WalletServiceAPI walletService;

    @Mock
    private SelectionKey selectionKey;

    @InjectMocks
    private CryptoWalletController cryptoWalletController;

    @Test
    void testRegisterSuccessfully() throws Exception {
        String username = "testUsername";
        String password = "testPassword";
        User registeredUser = new User(username, "hashed_password");
        when(userService.register(username, password)).thenReturn(registeredUser);
        cryptoWalletController.register(selectionKey, username, password);
        verify(selectionKey, times(1)).attach(registeredUser);
    }

    @Test
    void testRegisterThrowsUserAlreadyExceptionException() throws Exception {
        String username = "testUsername";
        String password = "testPassword";

        when(userService.register(username, password)).thenThrow(UserAlreadyExistsException.class);
        assertThrows(UserAlreadyExistsException.class,
            () -> cryptoWalletController.register(selectionKey, username, password),
            "Register should throw UserAlreadyExistsException when userService throws one!");
    }

    @Test
    void testLoginSuccessfully() throws Exception {
        String username = "testUsername";
        String password = "testPassword";

        User loggedInUser = new User(username, "hashed_password");
        when(userService.login(username, password)).thenReturn(loggedInUser);
        cryptoWalletController.login(selectionKey, username, password);
        verify(selectionKey, times(1)).attach(loggedInUser);
    }

    @Test
    void testLoginThrowsUserNotFoundException() throws Exception {
        String username = "john_doe";
        String password = "secure_password";
        when(userService.login(username, password)).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
            () -> cryptoWalletController.login(selectionKey, username, password),
            "Login should throw UserNotFoundException when userService throws one!");
    }

    @Test
    void testDepositMoneyUnauthorizedUser() {
        when(selectionKey.attachment()).thenReturn(null);
        assertThrows(UnauthorizedUserException.class,
            () -> cryptoWalletController.depositMoney(selectionKey, 100.0),
            "Deposit money should throw UnauthorizedUserException for unauthorized user");
        verifyNoInteractions(walletService);
    }

    @Test
    void testDepositMoneySuccessfully() throws UnauthorizedUserException {
        User mockUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(mockUser);
        cryptoWalletController.depositMoney(selectionKey, 100.0);
        verify(walletService).depositMoney(mockUser, 100.0);
    }

    @Test
    void testListOfferingsUnauthorizedUser() {
        when(selectionKey.attachment()).thenReturn(null);
        assertThrows(UnauthorizedUserException.class,
            () -> cryptoWalletController.listOfferings(selectionKey, 1),
            "List offerings should throw UnauthorizedUserException for unauthorized user");
        verifyNoInteractions(walletService);
    }

    @Test
    void testListOfferingsSuccessfully() throws UnauthorizedUserException {
        User mockUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(mockUser);
        StringBuilder expectedOutput1 = new StringBuilder();
        expectedOutput1.append("Asset ID: BTC -> Price: ").append(String.format("%.4f", 50000.0)).append("$ per unit!")
            .append(System.lineSeparator());
        expectedOutput1.append("Asset ID: ETH -> Price: ").append(String.format("%.4f", 3000.0)).append("$ per unit!")
            .append(System.lineSeparator());
        expectedOutput1.append("Asset ID: XRP -> Price: ").append(String.format("%.4f", 1.0)).append("$ per unit!")
            .append(System.lineSeparator());
        when(walletService.listOfferings(1)).thenReturn(expectedOutput1.toString());

        String result = cryptoWalletController.listOfferings(selectionKey, 1);
        assertEquals(expectedOutput1.toString(), result, "Result should be the same as the expected");
        verify(walletService).listOfferings(1);
    }

    @Test
    void testBuyAssetUnauthorizedUser() {
        when(selectionKey.attachment()).thenReturn(null);
        assertThrows(UnauthorizedUserException.class,
            () -> cryptoWalletController.buyAsset(selectionKey, "assetId", 100.0),
            "Buy asset should throw UnauthorizedUserException for unauthorized user");
        verifyNoInteractions(walletService);
    }

    @Test
    void testBuyAssetSuccessfully() throws UnauthorizedUserException, InvalidAssetIdException,
        InsufficientBalanceException {
        User mockUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(mockUser);
        cryptoWalletController.buyAsset(selectionKey, "assetId", 100.0);
        verify(walletService).buyAsset(mockUser, "assetId", 100.0);
    }

    @Test
    void testSellAssetUnauthorizedUser() {
        when(selectionKey.attachment()).thenReturn(null);
        assertThrows(UnauthorizedUserException.class,
            () -> cryptoWalletController.sellAsset(selectionKey, "assetId"),
            "Sell asset should throw UnauthorizedUserException for unauthorized user");
        verifyNoInteractions(walletService);
    }

    @Test
    void testSellAssetSuccessfully() throws UnauthorizedUserException, InvalidAssetIdException {
        User mockUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(mockUser);
        cryptoWalletController.sellAsset(selectionKey, "assetId");
        verify(walletService).sellAsset(mockUser, "assetId");
    }

    @Test
    void testGetWalletSummaryUnauthorizedUser() {
        when(selectionKey.attachment()).thenReturn(null);
        assertThrows(UnauthorizedUserException.class,
            () -> cryptoWalletController.getWalletSummary(selectionKey),
            "Get wallet summary should throw UnauthorizedUserException for unauthorized user");
        verifyNoInteractions(walletService);
    }

    @Test
    void testGetWalletSummarySuccessfully() throws UnauthorizedUserException {
        User mockUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(mockUser);
        cryptoWalletController.getWalletSummary(selectionKey);
        verify(walletService).getWalletSummary(mockUser);
    }

    @Test
    void testGetWalletOverallSummaryUnauthorizedUser() {
        when(selectionKey.attachment()).thenReturn(null);
        assertThrows(UnauthorizedUserException.class,
            () -> cryptoWalletController.getWalletOverallSummary(selectionKey),
            "Get wallet overall summary should throw UnauthorizedUserException for unauthorized user");
        verifyNoInteractions(walletService);
    }

    @Test
    void testGetWalletOverallSummarySuccessfully() throws UnauthorizedUserException {
        User mockUser = mock(User.class);
        when(selectionKey.attachment()).thenReturn(mockUser);
        cryptoWalletController.getWalletOverallSummary(selectionKey);
        verify(walletService).getWalletOverallSummary(mockUser);
    }
}

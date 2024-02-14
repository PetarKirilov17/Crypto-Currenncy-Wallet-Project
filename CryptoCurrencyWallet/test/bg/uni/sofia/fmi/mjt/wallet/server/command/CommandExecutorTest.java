package bg.uni.sofia.fmi.mjt.wallet.server.command;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoWalletAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InsufficientBalanceException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidAssetIdException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.LoginAuthenticationException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.PasswordWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UnauthorizedUserException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserNotFoundException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UsernameWrongFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class CommandExecutorTest {
    private static final String RESPONSE_OK_MESSAGE = "Response should be OK!";
    private static final String RESPONSE_NOT_OK_MESSAGE = "Response should not be OK!";
    private static final String RESPONSE_AS_EXPECTED_MESSAGE = "Response message should be the same as expected!";
    @Mock
    private CryptoWalletAPI cryptoWallet;

    @Mock
    private SelectionKey selectionKey;

    @InjectMocks
    private CommandExecutor commandExecutor;

    @Test
    void testExecuteCommandIsNull(){
        Response result = commandExecutor.execute(selectionKey, null);
        assertFalse(result.isOk(), RESPONSE_OK_MESSAGE);
        assertEquals("Error: Command cannot be null!", result.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        Command nullCommand = mock(Command.class);
        when(nullCommand.commandLabel()).thenReturn(null);

        result = commandExecutor.execute(selectionKey, nullCommand);
        assertFalse(result.isOk(), RESPONSE_OK_MESSAGE);
        assertEquals("Error: Command cannot be null!", result.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);
    }

    @Test
    void testExecuteCatchesException()
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        String username = "validUername";
        String password = "validPassword";
        List<String> args = List.of(username, password);
        Command validCommanad = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));
        doThrow(new RuntimeException("Simulated exception")).when(cryptoWallet).register(any(), any(), any());
        Response response = commandExecutor.execute(selectionKey, validCommanad);
        assertFalse(response.isOk(), RESPONSE_NOT_OK_MESSAGE);
        assertEquals("Error: Simulated exception", response.getResponse(),RESPONSE_AS_EXPECTED_MESSAGE);
    }

    @Test
    void testExecuteRegisterCommandSuccessfully()
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        List<String> args = List.of("username", "password");
        Command registerCommand = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));
        Response result = commandExecutor.execute(selectionKey, registerCommand);
        assertTrue(result.isOk(), RESPONSE_OK_MESSAGE);
        assertEquals("You successfully signed up!", result.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);
        verify(cryptoWallet, times(1)).register(selectionKey, "username", "password");
    }

    @Test
    void testExecuteRegisterCommandCatchesUsernameWrongFormatException()
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        List<String> args = List.of("!invalidUsername", "password");
        Command registerCommand = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));
        doThrow(new UsernameWrongFormatException("Invalid username format")).when(cryptoWallet)
            .register(any(), eq("!invalidUsername"), anyString());
        Response result = commandExecutor.execute(selectionKey, registerCommand);
        assertFalse(result.isOk(), RESPONSE_NOT_OK_MESSAGE);
        assertEquals("The username that you provided is in wrong format!", result.getResponse(),
            RESPONSE_AS_EXPECTED_MESSAGE);
        verify(cryptoWallet, times(1)).register(selectionKey, "!invalidUsername", "password");
    }

    @Test
    void testExecuteRegisterCommandCatchesUserAlreadyExistsException()
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        List<String> args = List.of("username", "password");
        Command registerCommand = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));
        doThrow(new UserAlreadyExistsException("There is already a user with this username")).when(cryptoWallet)
            .register(any(), eq("username"), anyString());
        Response result = commandExecutor.execute(selectionKey, registerCommand);
        assertFalse(result.isOk(), RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You cannot register with this username because there is an user with the same username!",
            result.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);
        verify(cryptoWallet, times(1)).register(selectionKey, "username", "password");
    }

    @Test
    void testExecuteRegisterCommandCatchesPasswordWrongFormatException()
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        List<String> args = List.of("username", "pas");
        Command registerCommand = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));
        doThrow(new PasswordWrongFormatException("Invalid password format!")).when(cryptoWallet)
            .register(any(), eq("username"), anyString());
        Response result = commandExecutor.execute(selectionKey, registerCommand);
        assertFalse(result.isOk(), RESPONSE_NOT_OK_MESSAGE);
        assertEquals("The password that you provided is in wrong format!", result.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);
        verify(cryptoWallet, times(1)).register(selectionKey, "username", "pas");
    }

    @Test
    void testLoginSuccessful() throws UserNotFoundException, UsernameWrongFormatException, LoginAuthenticationException,
        PasswordWrongFormatException {
        List<String> args = List.of("validUsername", "validPassword");
        Command loginCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));
        Response response = commandExecutor.execute(selectionKey, loginCommand);
        assertTrue(response.isOk(),RESPONSE_OK_MESSAGE);
        assertEquals("You successfully logged in!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).login(selectionKey, "validUsername", "validPassword");
    }

    @Test
    void testLoginCatchesUserNotFoundException()
        throws UserNotFoundException, UsernameWrongFormatException, LoginAuthenticationException,
        PasswordWrongFormatException {
        List<String> args = List.of("invalidUsername", "validPassword");
        Command loginCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));
        doThrow(new UserNotFoundException("User with this username cannot be found!")).when(cryptoWallet)
            .login(any(), eq("invalidUsername"), anyString());
        Response response = commandExecutor.execute(selectionKey, loginCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("There is no user with this username!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).login(selectionKey, "invalidUsername", "validPassword");
    }

    @Test
    void testLoginCatchesUsernameWrongFormatException()
        throws UserNotFoundException, UsernameWrongFormatException, LoginAuthenticationException,
        PasswordWrongFormatException {
        List<String> args = List.of("!validUsername", "validPassword");
        Command loginCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));
        doThrow(new UsernameWrongFormatException("Username is in wrong format!")).when(cryptoWallet)
            .login(any(), eq("!validUsername"), anyString());
        Response response = commandExecutor.execute(selectionKey, loginCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("The username that you provided is in wrong format!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).login(selectionKey, "!validUsername", "validPassword");
    }

    @Test
    void testLoginCatchesLoginAuthenticationException()
        throws UserNotFoundException, UsernameWrongFormatException, LoginAuthenticationException,
        PasswordWrongFormatException {
        List<String> args = List.of("validUsername", "wrongPassword");
        Command loginCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));
        doThrow(new LoginAuthenticationException("Wrong username or password!")).when(cryptoWallet)
            .login(any(), eq("validUsername"), anyString());
        Response response = commandExecutor.execute(selectionKey, loginCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("Your username or password is incorrect!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).login(selectionKey, "validUsername", "wrongPassword");
    }

    @Test
    void testLoginCatchesPasswordWrongFormatException()
        throws UserNotFoundException, UsernameWrongFormatException, LoginAuthenticationException,
        PasswordWrongFormatException {
        List<String> args = List.of("validUsername", "pas");
        Command loginCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));
        doThrow(new PasswordWrongFormatException("Password is in wrong format!")).when(cryptoWallet)
            .login(any(), eq("validUsername"), anyString());
        Response response = commandExecutor.execute(selectionKey, loginCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("The password that you provided is in wrong format!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).login(selectionKey, "validUsername", "pas");
    }

    @Test
    void testDepositMoneySuccessful()
        throws UnauthorizedUserException {
        String money = "100.0";
        Command depositMoneyCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{money});
        Response response = commandExecutor.execute(selectionKey, depositMoneyCommand);
        assertTrue(response.isOk(),RESPONSE_OK_MESSAGE);
        assertEquals("Successfully deposited " + String.format("%.4f", 100.0) + "$", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).depositMoney(selectionKey, 100.0);
    }

    @Test
    void testDepositMoneyCatchesUnauthorizedUserException() throws UnauthorizedUserException {
        String money = "100.0";
        Command depositMoneyCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{money});
        doThrow(new UnauthorizedUserException("User with this username is not authorized")).when(cryptoWallet)
            .depositMoney(any(), eq(100.0));
        Response response = commandExecutor.execute(selectionKey, depositMoneyCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You are not logged in! Please log in to your account!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).depositMoney(selectionKey, 100.0);
    }

    @Test
    void testListOfferingsSuccessfully() throws UnauthorizedUserException {
        String page = "1";
        Command listOfferingsCommand = new Command(CommandLabel.LIST_OFFERINGS, new String[]{page});
        when(cryptoWallet.listOfferings(selectionKey,1)).thenReturn("First page!");
        Response response = commandExecutor.execute(selectionKey, listOfferingsCommand);
        assertTrue(response.isOk(),RESPONSE_OK_MESSAGE);
        assertEquals("First page!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).listOfferings(selectionKey, 1);
    }

    @Test
    void testListOfferingsCatchesUnAuthorizedUserException() throws UnauthorizedUserException {
        String page = "1";
        Command listOfferingsCommand = new Command(CommandLabel.LIST_OFFERINGS, new String[]{page});
        doThrow(new UnauthorizedUserException("User with this username is not authorized")).when(cryptoWallet)
            .listOfferings(any(), eq(1));
        Response response = commandExecutor.execute(selectionKey, listOfferingsCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You are not logged in! Please log in to your account!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).listOfferings(selectionKey, 1);
    }

    @Test
    void testBuyAssetSuccessfully()
        throws UnauthorizedUserException, InsufficientBalanceException, InvalidAssetIdException {
        String assetID = "BTC";
        String amount = "100.0";
        List<String> args = List.of(assetID, amount);
        Command buyAssetCommand = new Command(CommandLabel.BUY_ASSET, args.toArray(new String[0]));
        Response response = commandExecutor.execute(selectionKey, buyAssetCommand);
        assertTrue(response.isOk(),RESPONSE_OK_MESSAGE);
        assertEquals("You successfully bought " +  assetID +  " for " + String.format("%.4f", Double.parseDouble(amount)) + "$", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).buyAsset(selectionKey, "BTC", 100.0);
    }

    @Test
    void testBuyAssetCatchesUnauthorizedException()
        throws UnauthorizedUserException, InsufficientBalanceException, InvalidAssetIdException {
        String assetID = "BTC";
        String amount = "100.0";
        List<String> args = List.of(assetID, amount);
        Command buyAssetCommand = new Command(CommandLabel.BUY_ASSET, args.toArray(new String[0]));
        doThrow(new UnauthorizedUserException("User with this username is not authorized")).when(cryptoWallet)
            .buyAsset(any(), eq("BTC"), eq(100.0));
        Response response = commandExecutor.execute(selectionKey, buyAssetCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You are not logged in! Please log in to your account!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).buyAsset(selectionKey, "BTC", 100.0);
    }

    @Test
    void testBuyAssetCatchesInsufficientBalanceException()
        throws UnauthorizedUserException, InsufficientBalanceException, InvalidAssetIdException {
        String assetID = "BTC";
        String amount = "100.0";
        List<String> args = List.of(assetID, amount);
        Command buyAssetCommand = new Command(CommandLabel.BUY_ASSET, args.toArray(new String[0]));
        doThrow(new InsufficientBalanceException("Insufficient balance!")).when(cryptoWallet)
            .buyAsset(any(), eq("BTC"), eq(100.0));
        Response response = commandExecutor.execute(selectionKey, buyAssetCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You do not have enough money!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).buyAsset(selectionKey, "BTC", 100.0);
    }

    @Test
    void testBuyAssetCatchesInvalidAssetIdException()
        throws UnauthorizedUserException, InsufficientBalanceException, InvalidAssetIdException {
        String assetID = "BTC";
        String amount = "100.0";
        List<String> args = List.of(assetID, amount);
        Command buyAssetCommand = new Command(CommandLabel.BUY_ASSET, args.toArray(new String[0]));
        doThrow(new InvalidAssetIdException("Invalid AssetID")).when(cryptoWallet)
            .buyAsset(any(), eq("BTC"), eq(100.0));
        Response response = commandExecutor.execute(selectionKey, buyAssetCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("There is no asset with this id!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).buyAsset(selectionKey, "BTC", 100.0);
    }

    @Test
    void testSellAssetSuccessfully() throws UnauthorizedUserException, InvalidAssetIdException {
        String assetID = "BTC";
        Command sellAssetCommand = new Command(CommandLabel.SELL_ASSET, new String[]{assetID});
        when(cryptoWallet.sellAsset(selectionKey, assetID)).thenReturn(1000.0);
        Response response = commandExecutor.execute(selectionKey, sellAssetCommand);
        assertTrue(response.isOk(),RESPONSE_OK_MESSAGE);
        assertEquals("You successfully sold your actives from " + assetID + " and earned " + String.format("%.4f", 1000.0) + "$", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).sellAsset(selectionKey, "BTC");
    }

    @Test
    void testSellAssetCatchesUnauthorizedUserException() throws UnauthorizedUserException, InvalidAssetIdException {
        String assetID = "BTC";
        Command sellAssetCommand = new Command(CommandLabel.SELL_ASSET, new String[]{assetID});
        doThrow(new UnauthorizedUserException("User with this username is not authorized")).when(cryptoWallet)
            .sellAsset(any(), eq("BTC"));
        Response response = commandExecutor.execute(selectionKey, sellAssetCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You are not logged in! Please log in to your account!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).sellAsset(selectionKey, "BTC");
    }

    @Test
    void testSellAssetCatchesInvalidAssetIdException() throws UnauthorizedUserException, InvalidAssetIdException {
        String assetID = "BTC";
        Command sellAssetCommand = new Command(CommandLabel.SELL_ASSET, new String[]{assetID});
        doThrow(new InvalidAssetIdException("User with this username is not authorized")).when(cryptoWallet)
            .sellAsset(any(), eq("BTC"));
        Response response = commandExecutor.execute(selectionKey, sellAssetCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("There is no asset with this id!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).sellAsset(selectionKey, "BTC");
    }

    @Test
    void testGetWalletSummarySuccessfully() throws UnauthorizedUserException {
        Command getWalletSummaryCommand = new Command(CommandLabel.WALLET_SUMMARY, new String[]{});
        when(cryptoWallet.getWalletSummary(selectionKey)).thenReturn("Wallet summary!");
        Response response = commandExecutor.execute(selectionKey, getWalletSummaryCommand);
        assertTrue(response.isOk(),RESPONSE_OK_MESSAGE);
        assertEquals("Wallet summary!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).getWalletSummary(selectionKey);
    }

    @Test
    void testGetWalletSummaryCatchesUnauthorizedUserException() throws UnauthorizedUserException {
        Command sellAssetCommand = new Command(CommandLabel.WALLET_SUMMARY, new String[]{});
        doThrow(new UnauthorizedUserException("User with this username is not authorized")).when(cryptoWallet)
            .getWalletSummary(any());
        Response response = commandExecutor.execute(selectionKey, sellAssetCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You are not logged in! Please log in to your account!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).getWalletSummary(selectionKey);
    }

    @Test
    void testGetWalletOverallSummarySuccessfully() throws UnauthorizedUserException {
        Command getWalletOverallSummaryCommand = new Command(CommandLabel.WALLET_OVERALL_SUMMARY, new String[]{});
        when(cryptoWallet.getWalletOverallSummary(selectionKey)).thenReturn("Wallet overall summary!");
        Response response = commandExecutor.execute(selectionKey, getWalletOverallSummaryCommand);
        assertTrue(response.isOk(),RESPONSE_OK_MESSAGE);
        assertEquals("Wallet overall summary!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).getWalletOverallSummary(selectionKey);
    }

    @Test
    void testGetWalletOverallSummaryCatchesUnauthorizedException() throws UnauthorizedUserException {
        Command getWalletOverallSummaryCommand = new Command(CommandLabel.WALLET_OVERALL_SUMMARY, new String[]{});
        doThrow(new UnauthorizedUserException("User with this username is not authorized")).when(cryptoWallet)
            .getWalletOverallSummary(any());
        Response response = commandExecutor.execute(selectionKey, getWalletOverallSummaryCommand);
        assertFalse(response.isOk(),RESPONSE_NOT_OK_MESSAGE);
        assertEquals("You are not logged in! Please log in to your account!", response.getResponse(), RESPONSE_AS_EXPECTED_MESSAGE);

        verify(cryptoWallet, times(1)).getWalletOverallSummary(selectionKey);
    }
}

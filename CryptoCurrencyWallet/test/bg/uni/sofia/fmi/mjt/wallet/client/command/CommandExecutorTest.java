package bg.uni.sofia.fmi.mjt.wallet.client.command;

import bg.uni.sofia.fmi.mjt.wallet.client.CryptoWalletClient;
import bg.uni.sofia.fmi.mjt.wallet.client.SessionInfo;
import bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {
    @Mock
    private UI ui;

    @Mock
    private CryptoWalletClient httpClient;

    @Mock
    private CommandValidator validator;

    @Mock
    private SessionInfo sessionInfo;
    @InjectMocks
    private CommandExecutor commandExecutor;

    @Test
    void testExecuteHelpCommandPrintsHelpMenuForLoggedOut() {
        when(sessionInfo.isLoggedIn()).thenReturn(false);
        Command helpCommand = new Command(CommandLabel.HELP, new String[]{});
        commandExecutor.execute(helpCommand);
        verify(ui).write(CommandLabel.REGISTER.userCommand + " <username> <password>");
        verify(ui).write(CommandLabel.LOGIN.userCommand + " <username> <password>");
        verify(ui).write(CommandLabel.QUIT.userCommand);
    }

    @Test
    void testExecuteHelpCommandPrintsHelpMenuForLoggedIn(){
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command helpCommand = new Command(CommandLabel.HELP, new String[]{});
        commandExecutor.execute(helpCommand);
        verify(ui).write(CommandLabel.DEPOSIT_MONEY.userCommand + " <money>");
        verify(ui).write(CommandLabel.LIST_OFFERINGS.userCommand + " <page number>");
        verify(ui).write(CommandLabel.BUY_ASSET.userCommand + " <assetID> <money>");
        verify(ui).write(CommandLabel.SELL_ASSET.userCommand + " <assetID>");
        verify(ui).write(CommandLabel.WALLET_SUMMARY.userCommand);
        verify(ui).write(CommandLabel.WALLET_OVERALL_SUMMARY.userCommand);
        verify(ui).write(CommandLabel.LOG_OUT.userCommand);
        verify(ui).write(CommandLabel.HELP.userCommand);
    }

    @Test
    void testExecuteRegisterCommandWithValidArgumentsSuccessfulRegistration() {
        when(sessionInfo.isLoggedIn()).thenReturn(false);
        List<String> args = List.of("username", "password");
        Command registerCommand = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));

        when(validator.validateRegisterAndSignUp(registerCommand)).thenReturn(true);
        when(httpClient.sendRequest(registerCommand)).thenReturn("{\"ok\":true,\"response\":\"Registration successful!\"}");

        commandExecutor.execute(registerCommand);

        verify(ui).write("Registration successful!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteRegisterCommandWithInvalidArgumentsErrorDisplayed() {
        when(sessionInfo.isLoggedIn()).thenReturn(false);
        List<String> args = List.of("!username", "password");
        Command registerCommand = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));
        when(validator.validateRegisterAndSignUp(registerCommand)).thenReturn(false);
        commandExecutor.execute(registerCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteRegisterCommandWithValidArgumentsButResponseNotOK() {
        when(sessionInfo.isLoggedIn()).thenReturn(false);
        List<String> args = List.of("username", "password");
        Command registerCommand = new Command(CommandLabel.REGISTER, args.toArray(new String[0]));
        when(validator.validateRegisterAndSignUp(registerCommand)).thenReturn(true);
        when(httpClient.sendRequest(registerCommand)).thenReturn("{\"ok\":false,\"response\":\"Registration failed\"}");

        commandExecutor.execute(registerCommand);

        verify(ui, never()).write(anyString());
        verify(ui).writeError("Registration failed");
        verify(httpClient).sendRequest(registerCommand);
    }

    @Test
    void testExecuteLoginCommandWithValidArgumentsSuccessfulRegistration() {
        when(sessionInfo.isLoggedIn()).thenReturn(false);
        List<String> args = List.of("username", "password");
        Command registerCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));
        when(validator.validateRegisterAndSignUp(registerCommand)).thenReturn(true);
        when(httpClient.sendRequest(registerCommand)).thenReturn("{\"ok\":true,\"response\":\"Login successful!\"}");

        commandExecutor.execute(registerCommand);

        verify(ui).write("Login successful!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteLoginCommandWithInvalidArgumentsErrorDisplayed() {
        when(sessionInfo.isLoggedIn()).thenReturn(false);
        List<String> args = List.of("!username", "password");
        Command registerCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));
        when(validator.validateRegisterAndSignUp(registerCommand)).thenReturn(false);
        commandExecutor.execute(registerCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteLoginCommandWithValidArgumentsButResponseNotOK() {
        when(sessionInfo.isLoggedIn()).thenReturn(false);
        List<String> args = List.of("username", "password");
        Command registerCommand = new Command(CommandLabel.LOGIN, args.toArray(new String[0]));

        when(validator.validateRegisterAndSignUp(registerCommand)).thenReturn(true);
        when(httpClient.sendRequest(registerCommand)).thenReturn("{\"ok\":false,\"response\":\"Login failed\"}");

        commandExecutor.execute(registerCommand);

        verify(ui, never()).write(anyString());
        verify(ui).writeError("Login failed");
        verify(httpClient).sendRequest(registerCommand);
    }

    @Test
    void testExecuteDepositMoneySuccessfully() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command depositCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{"100.00"});
        when(validator.validateDepositMoney(depositCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":true,\"response\":\"Deposit successful!\"}";
        when(httpClient.sendRequest(depositCommand)).thenReturn(serverResponse);
        commandExecutor.execute(depositCommand);

        verify(validator).validateDepositMoney(depositCommand);
        verify(ui).write("Deposit successful!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteDepositMoneyWithInvalidValidator() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command depositCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{"100.00"});
        when(validator.validateDepositMoney(depositCommand)).thenReturn(false);
        commandExecutor.execute(depositCommand);
        verify(validator).validateDepositMoney(depositCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteDepositMoneyWithNotOkResponse() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command depositCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{"100.00"});
        when(validator.validateDepositMoney(depositCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":false,\"response\":\"Deposit failed\"}";
        when(httpClient.sendRequest(depositCommand)).thenReturn(serverResponse);
        commandExecutor.execute(depositCommand);
        verify(validator).validateDepositMoney(depositCommand);
        verify(ui).writeError("Deposit failed");
        verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteBuyAssetSuccessfully() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String assetId = "BTC";
        String amount = "100.0";
        List<String> args = List.of(assetId, amount);
        Command buyAssetCommand = new Command(CommandLabel.BUY_ASSET, args.toArray(new String[0]));
        when(validator.validateBuyAsset(buyAssetCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":true,\"response\":\"Buy asset successful!\"}";
        when(httpClient.sendRequest(buyAssetCommand)).thenReturn(serverResponse);
        commandExecutor.execute(buyAssetCommand);

        verify(validator).validateBuyAsset(buyAssetCommand);
        verify(ui).write("Buy asset successful!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteBuyAssetWithInvalidValidator() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String assetId = "BTC";
        String amount = "100.0";
        List<String> args = List.of(assetId, amount);
        Command buyAssetCommand = new Command(CommandLabel.BUY_ASSET, args.toArray(new String[0]));
        when(validator.validateBuyAsset(buyAssetCommand)).thenReturn(false);
        commandExecutor.execute(buyAssetCommand);
        verify(validator).validateBuyAsset(buyAssetCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteBuyAssetWithNotOkResponse() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String assetId = "BTC";
        String amount = "100.0";
        List<String> args = List.of(assetId, amount);
        Command buyAssetCommand = new Command(CommandLabel.BUY_ASSET, args.toArray(new String[0]));
        when(validator.validateBuyAsset(buyAssetCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":false,\"response\":\"Buy asset failed\"}";
        when(httpClient.sendRequest(buyAssetCommand)).thenReturn(serverResponse);
        commandExecutor.execute(buyAssetCommand);
        verify(validator).validateBuyAsset(buyAssetCommand);
        verify(ui).writeError("Buy asset failed");
        verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteSellAssetSuccessfully() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String assetId = "BTC";
        List<String> args = List.of(assetId);
        Command sellAssetCommand = new Command(CommandLabel.SELL_ASSET, args.toArray(new String[0]));
        when(validator.validateSellAsset(sellAssetCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":true,\"response\":\"Sell asset successful!\"}";
        when(httpClient.sendRequest(sellAssetCommand)).thenReturn(serverResponse);
        commandExecutor.execute(sellAssetCommand);

        verify(validator).validateSellAsset(sellAssetCommand);
        verify(ui).write("Sell asset successful!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteSellAssetWithInvalidValidator() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String assetId = "BTC";
        List<String> args = List.of(assetId);
        Command sellAssetCommand = new Command(CommandLabel.SELL_ASSET, args.toArray(new String[0]));
        when(validator.validateSellAsset(sellAssetCommand)).thenReturn(false);
        commandExecutor.execute(sellAssetCommand);
        verify(validator).validateSellAsset(sellAssetCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteSellAssetWithNotOkResponse() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String assetId = "BTC";
        List<String> args = List.of(assetId);
        Command sellAssetCommand = new Command(CommandLabel.SELL_ASSET, args.toArray(new String[0]));
        when(validator.validateSellAsset(sellAssetCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":false,\"response\":\"Sell asset failed\"}";
        when(httpClient.sendRequest(sellAssetCommand)).thenReturn(serverResponse);
        commandExecutor.execute(sellAssetCommand);
        verify(validator).validateSellAsset(sellAssetCommand);
        verify(ui).writeError("Sell asset failed");
        verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteListOfferingsSuccessfully() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String page = "1";
        List<String> args = List.of(page);
        Command listOfferingsCommand = new Command(CommandLabel.LIST_OFFERINGS, args.toArray(new String[0]));
        when(validator.validateListOfferings(listOfferingsCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":true,\"response\":\"List of assets!\"}";
        when(httpClient.sendRequest(listOfferingsCommand)).thenReturn(serverResponse);
        commandExecutor.execute(listOfferingsCommand);

        verify(validator).validateListOfferings(listOfferingsCommand);
        verify(ui).write("List of assets!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteListOfferingsWithInvalidValidator() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String page = "1";
        List<String> args = List.of(page);
        Command listOfferingsCommand = new Command(CommandLabel.LIST_OFFERINGS, args.toArray(new String[0]));
        when(validator.validateListOfferings(listOfferingsCommand)).thenReturn(false);
        commandExecutor.execute(listOfferingsCommand);
        verify(validator).validateListOfferings(listOfferingsCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteListOfferingsWithNotOkResponse() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        String page = "1";
        List<String> args = List.of(page);
        Command listOfferingsCommand = new Command(CommandLabel.LIST_OFFERINGS, args.toArray(new String[0]));
        when(validator.validateListOfferings(listOfferingsCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":false,\"response\":\"Unsuccessful list of assets!\"}";
        when(httpClient.sendRequest(listOfferingsCommand)).thenReturn(serverResponse);
        commandExecutor.execute(listOfferingsCommand);
        verify(validator).validateListOfferings(listOfferingsCommand);
        verify(ui).writeError("Unsuccessful list of assets!");
        verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteGetWalletSummarySuccessfully() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command walletSummaryCommand = new Command(CommandLabel.WALLET_SUMMARY, new String[]{});
        when(validator.validateCommandsWithNoArguments(walletSummaryCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":true,\"response\":\"Wallet summary!\"}";
        when(httpClient.sendRequest(walletSummaryCommand)).thenReturn(serverResponse);
        commandExecutor.execute(walletSummaryCommand);

        verify(validator).validateCommandsWithNoArguments(walletSummaryCommand);
        verify(ui).write("Wallet summary!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteGetWalletSummaryWithInvalidValidator() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command walletSummaryCommand = new Command(CommandLabel.WALLET_SUMMARY, new String[]{});
        when(validator.validateCommandsWithNoArguments(walletSummaryCommand)).thenReturn(false);
        commandExecutor.execute(walletSummaryCommand);
        verify(validator).validateCommandsWithNoArguments(walletSummaryCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteGetWalletSummaryWithNotOkResponse() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command walletSummaryCommand = new Command(CommandLabel.WALLET_SUMMARY, new String[]{});
        when(validator.validateCommandsWithNoArguments(walletSummaryCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":false,\"response\":\"Unsuccessful wallet summary!\"}";
        when(httpClient.sendRequest(walletSummaryCommand)).thenReturn(serverResponse);
        commandExecutor.execute(walletSummaryCommand);
        verify(validator).validateCommandsWithNoArguments(walletSummaryCommand);
        verify(ui).writeError("Unsuccessful wallet summary!");
        verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteGetWalletOverallSummarySuccessfully() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command walletOverallSummaryCommand = new Command(CommandLabel.WALLET_OVERALL_SUMMARY, new String[]{});
        when(validator.validateCommandsWithNoArguments(walletOverallSummaryCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":true,\"response\":\"Wallet overall summary!\"}";
        when(httpClient.sendRequest(walletOverallSummaryCommand)).thenReturn(serverResponse);
        commandExecutor.execute(walletOverallSummaryCommand);

        verify(validator).validateCommandsWithNoArguments(walletOverallSummaryCommand);
        verify(ui).write("Wallet overall summary!");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testExecuteGetWalletOverallSummaryWithInvalidValidator() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command walletOverallSummaryCommand = new Command(CommandLabel.WALLET_OVERALL_SUMMARY, new String[]{});
        when(validator.validateCommandsWithNoArguments(walletOverallSummaryCommand)).thenReturn(false);
        commandExecutor.execute(walletOverallSummaryCommand);
        verify(validator).validateCommandsWithNoArguments(walletOverallSummaryCommand);
        verify(ui, never()).write(anyString());
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }

    @Test
    void testExecuteGetWalletOverallSummaryWithNotOkResponse() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command walletOverallSummaryCommand = new Command(CommandLabel.WALLET_OVERALL_SUMMARY, new String[]{});
        when(validator.validateCommandsWithNoArguments(walletOverallSummaryCommand)).thenReturn(true);
        String serverResponse = "{\"ok\":false,\"response\":\"Unsuccessful wallet overall summary!\"}";
        when(httpClient.sendRequest(walletOverallSummaryCommand)).thenReturn(serverResponse);
        commandExecutor.execute(walletOverallSummaryCommand);
        verify(validator).validateCommandsWithNoArguments(walletOverallSummaryCommand);
        verify(ui).writeError("Unsuccessful wallet overall summary!");
        verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteLogOut() {
        when(sessionInfo.isLoggedIn()).thenReturn(true);
        Command logOutCommand = new Command(CommandLabel.LOG_OUT, new String[]{});
        commandExecutor.execute(logOutCommand);
        verify(sessionInfo).logOut();
        verify(ui).write("Logged out");
        verify(ui, never()).writeError(anyString());
        verify(httpClient, never()).sendRequest(any(Command.class));
    }
}

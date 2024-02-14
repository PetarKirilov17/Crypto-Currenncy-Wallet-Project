package bg.uni.sofia.fmi.mjt.wallet.client.command;

import bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandValidatorTest {
    @Mock
    private UI ui;

    @InjectMocks
    private CommandValidator commandValidator;

    @Test
    void testValidateRegisterAndSignUpWithValidArgs() {
        Command validRegisterCommand = new Command(CommandLabel.REGISTER, new String[]{"validUsername", "validPassword"});
        boolean result = commandValidator.validateRegisterAndSignUp(validRegisterCommand);
        assertTrue(result, "Validation should pass with valid arguments");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateRegisterAndSignUpWithInvalidUsername() {
        Command invalidUsernameCommand = new Command(CommandLabel.REGISTER, new String[]{"!invalidUsername", "validPassword"});
        boolean result = commandValidator.validateRegisterAndSignUp(invalidUsernameCommand);
        assertFalse(result, "Validation should fail with invalid username");
        verify(ui).writeError("Username has to be between 4 and 20 characters long! It must contain only alphanumeric characters!");
    }

    @Test
    void testValidateRegisterAndSignUpWithInvalidPassword() {
        Command invalidPasswordCommand = new Command(CommandLabel.REGISTER, new String[]{"validUsername", "a".repeat(2)});
        boolean result = commandValidator.validateRegisterAndSignUp(invalidPasswordCommand);
        assertFalse(result, "Validation should fail with invalid password");

        invalidPasswordCommand = new Command(CommandLabel.REGISTER, new String[] {"validUsername", "a".repeat(32)});
        result = commandValidator.validateRegisterAndSignUp(invalidPasswordCommand);
        assertFalse(result, "Validation should fail with invalid password");

        verify(ui, times(2)).writeError("Password has to be between 6 and 30");
    }

    @Test
    void testValidateDepositMoneyWithValidArgs() {
        Command validDepositCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{"100.00"});
        boolean result = commandValidator.validateDepositMoney(validDepositCommand);
        assertTrue(result, "Validation should pass with valid arguments");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateDepositMoneyWithInvalidDouble() {
        Command invalidDoubleCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{"12invalid"});
        boolean result = commandValidator.validateDepositMoney(invalidDoubleCommand);
        assertFalse(result, "Validation should fail with invalid double");
        verify(ui).writeError("Double number is not in correct format! Please insert a valid number!");
    }

    @Test
    void testValidateDepositMoneyWithInvalidArgumentCount() {
        Command invalidArgumentCountCommand = new Command(CommandLabel.DEPOSIT_MONEY, new String[]{"100.00", "extraArg"});
        boolean result = commandValidator.validateDepositMoney(invalidArgumentCountCommand);
        assertFalse(result, "Validation should fail with invalid argument count");
        verify(ui).writeError("You passed an invalid count of arguments! Desired count: 1 | Count of passed arguments: 2");
    }

    @Test
    void testValidateListOfferingsWithValidArgs() {
        Command validListOfferingsCommand = new Command(CommandLabel.LIST_OFFERINGS, new String[]{"1"});
        boolean result = commandValidator.validateListOfferings(validListOfferingsCommand);
        assertTrue(result, "Validation should pass with valid arguments");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateListOfferingsWithInvalidPositiveInteger() {
        Command invalidPositiveIntegerCommand = new Command(CommandLabel.LIST_OFFERINGS, new String[]{"1notAnInteger"});
        boolean result = commandValidator.validateListOfferings(invalidPositiveIntegerCommand);
        assertFalse(result, "Validation should fail with invalid positive integer");
        verify(ui).writeError("Page number is not in correct format! Please insert a valid number!");
    }

    @Test
    void testValidateListOfferingsWithInvalidArgumentCount() {
        Command invalidArgumentCountCommand = new Command(CommandLabel.LIST_OFFERINGS, new String[]{"1", "extraArg"});
        boolean result = commandValidator.validateListOfferings(invalidArgumentCountCommand);
        assertFalse(result, "Validation should fail with invalid argument count");
        verify(ui).writeError("You passed an invalid count of arguments! Desired count: 1 | Count of passed arguments: 2");
    }

    @Test
    void testValidateBuyAssetWithValidArgs() {
        Set<String> mockedAssetIds = new HashSet<>();
        mockedAssetIds.add("BTC");
        mockedAssetIds.add("ETH");
        CommandValidator commandValidatorWithMockedAssets = new CommandValidator(ui, mockedAssetIds);
        Command validBuyAssetCommand = new Command(CommandLabel.BUY_ASSET, new String[]{"BTC", "100.00"});
        boolean result = commandValidatorWithMockedAssets.validateBuyAsset(validBuyAssetCommand);
        assertTrue(result, "Validation should pass with valid arguments");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateBuyAssetWithInvalidAssetId() {
        Set<String> mockedAssetIds = new HashSet<>();
        mockedAssetIds.add("BTC");
        mockedAssetIds.add("ETH");
        CommandValidator commandValidatorWithMockedAssets = new CommandValidator(ui, mockedAssetIds);
        Command invalidAssetIdCommand = new Command(CommandLabel.BUY_ASSET, new String[]{"USD", "100.00"});
        boolean result = commandValidatorWithMockedAssets.validateBuyAsset(invalidAssetIdCommand);
        assertFalse(result, "Validation should fail with invalid asset ID");
        verify(ui).writeError("There is no asset with this asset ID!");
    }

    @Test
    void testValidateBuyAssetWithInvalidArgumentCount() {
        Set<String> mockedAssetIds = new HashSet<>();
        mockedAssetIds.add("BTC");
        mockedAssetIds.add("ETH");
        CommandValidator commandValidatorWithMockedAssets = new CommandValidator(ui, mockedAssetIds);
        Command invalidArgumentCountCommand = new Command(CommandLabel.BUY_ASSET, new String[]{"BTC", "100.00", "extraArg"});
        boolean result = commandValidatorWithMockedAssets.validateBuyAsset(invalidArgumentCountCommand);
        assertFalse(result, "Validation should fail with invalid argument count");
        verify(ui).writeError("You passed an invalid count of arguments! Desired count: 2 | Count of passed arguments: 3");
    }

    @Test
    void testValidateBuyAssetWithInvalidDouble() {
        Set<String> mockedAssetIds = new HashSet<>();
        mockedAssetIds.add("BTC");
        mockedAssetIds.add("ETH");
        CommandValidator commandValidatorWithMockedAssets = new CommandValidator(ui, mockedAssetIds);
        Command invalidDoubleCommand = new Command(CommandLabel.BUY_ASSET, new String[]{"BTC", "12notADouble"});
        boolean result = commandValidatorWithMockedAssets.validateBuyAsset(invalidDoubleCommand);
        assertFalse(result, "Validation should fail with invalid double");
        verify(ui).writeError("Double number is not in correct format! Please insert a valid number!");
    }

    @Test
    void testValidateSellAssetWithValidArgs() {
        Set<String> mockedAssetIds = new HashSet<>();
        mockedAssetIds.add("BTC");
        mockedAssetIds.add("ETH");
        CommandValidator commandValidatorWithMockedAssets = new CommandValidator(ui, mockedAssetIds);
        Command validSellAssetCommand = new Command(CommandLabel.SELL_ASSET, new String[]{"BTC"});
        boolean result = commandValidatorWithMockedAssets.validateSellAsset(validSellAssetCommand);
        assertTrue(result, "Validation should pass with valid arguments");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateSellAssetWithInvalidArgumentCount() {
        Set<String> mockedAssetIds = new HashSet<>();
        mockedAssetIds.add("BTC");
        mockedAssetIds.add("ETH");
        CommandValidator commandValidatorWithMockedAssets = new CommandValidator(ui, mockedAssetIds);
        Command invalidArgumentCountCommand = new Command(CommandLabel.SELL_ASSET, new String[]{"BTC", "extraArg"});
        boolean result = commandValidatorWithMockedAssets.validateSellAsset(invalidArgumentCountCommand);
        assertFalse(result, "Validation should fail with invalid argument count");
        verify(ui).writeError("You passed an invalid count of arguments! Desired count: 1 | Count of passed arguments: 2");
    }

    @Test
    void testValidateSellAssetWithInvalidAssetId() {
        // Arrange
        Set<String> mockedAssetIds = new HashSet<>();
        mockedAssetIds.add("BTC");
        mockedAssetIds.add("ETH");
        CommandValidator commandValidatorWithMockedAssets = new CommandValidator(ui, mockedAssetIds);
        Command invalidAssetIdCommand = new Command(CommandLabel.SELL_ASSET, new String[]{"USD"});
        boolean result = commandValidatorWithMockedAssets.validateSellAsset(invalidAssetIdCommand);
        assertFalse(result, "Validation should fail with invalid asset ID");
        verify(ui).writeError("There is no asset with this asset ID!");
    }
    @Test
    void testValidateCommandsWithNoArgumentsWithValidArgs() {
        Command validCommand = new Command(CommandLabel.WALLET_SUMMARY, new String[]{});
        boolean result = commandValidator.validateCommandsWithNoArguments(validCommand);
        assertTrue(result, "Validation should pass with valid arguments");
        verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateCommandsWithNoArgumentsWithInvalidArgs() {
        Command invalidCommand = new Command(CommandLabel.WALLET_SUMMARY, new String[]{"extraArg"});
        boolean result = commandValidator.validateCommandsWithNoArguments(invalidCommand);
        assertFalse(result, "Validation should fail with invalid arguments");
        verify(ui).writeError("You passed an invalid count of arguments! Desired count: 0 | Count of passed arguments: 1");
    }

}

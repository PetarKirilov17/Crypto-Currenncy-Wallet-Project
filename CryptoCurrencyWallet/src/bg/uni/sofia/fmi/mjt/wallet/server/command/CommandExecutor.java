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
import bg.uni.sofia.fmi.mjt.wallet.server.logger.ErrorLogger;
import java.nio.channels.SelectionKey;
import java.util.Arrays;

public class CommandExecutor {
    private static final String ERROR = "Error: ";
    private static final String UNKNOWN_COMMAND = "Unknown command";

    private CryptoWalletAPI cryptoWallet;

    public CommandExecutor(CryptoWalletAPI cryptoWallet) {
        this.cryptoWallet = cryptoWallet;
    }

    public Response execute(SelectionKey key, Command command) {
        try {
            if (command == null || command.commandLabel() == null) {
                return new Response(false, ERROR + "Command cannot be null!");
            }
            return switch (command.commandLabel()) {
                case REGISTER -> register(key, command.arguments()[0], command.arguments()[1]);
                case LOGIN -> login(key, command.arguments()[0], command.arguments()[1]);
                case DEPOSIT_MONEY -> depositMoney(key, Double.parseDouble(command.arguments()[0]));
                case LIST_OFFERINGS -> listOfferings(key, Integer.parseInt(command.arguments()[0]));
                case BUY_ASSET -> buyAsset(key, command.arguments()[0], Double.parseDouble(command.arguments()[1]));
                case SELL_ASSET -> sellAsset(key, command.arguments()[0]);
                case WALLET_SUMMARY -> getWalletSummary(key);
                case WALLET_OVERALL_SUMMARY -> getWalletOverallSummary(key);
                default -> new Response(false, UNKNOWN_COMMAND);
            };
        }catch (Exception e){
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            return new Response(false, ERROR + e.getMessage());
        }
    }

    private Response register(SelectionKey key, String username, String password) {
        try {
            cryptoWallet.register(key, username, password);
            String responseStr = "You successfully signed up!";
            return new Response(true, responseStr);
        } catch (UsernameWrongFormatException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "The username that you provided is in wrong format!";
            return new Response(false, responseStr);
        } catch (UserAlreadyExistsException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You cannot register with this username because there is an user with the same username!";
            return new Response(false, responseStr);
        } catch (PasswordWrongFormatException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "The password that you provided is in wrong format!";
            return new Response(false, responseStr);
        }
    }

    private Response login(SelectionKey key, String username, String password) {
        try {
            cryptoWallet.login(key, username, password);
            String responseStr = "You successfully logged in!";
            return new Response(true, responseStr);
        } catch (UserNotFoundException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "There is no user with this username!";
            return new Response(false, responseStr);
        } catch (UsernameWrongFormatException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "The username that you provided is in wrong format!";
            return new Response(false, responseStr);
        } catch (LoginAuthenticationException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "Your username or password is incorrect!";
            return new Response(false, responseStr);
        } catch (PasswordWrongFormatException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "The password that you provided is in wrong format!";
            return new Response(false, responseStr);
        }
    }

    private Response depositMoney(SelectionKey key, double amount) {
        try {
            cryptoWallet.depositMoney(key, amount);
            String responseStr = "Successfully deposited " + String.format("%.4f", amount) + "$";
            return new Response(true, responseStr);
        } catch (UnauthorizedUserException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You are not logged in! Please log in to your account!";
            return new Response(false, responseStr);
        }
    }

    private Response listOfferings(SelectionKey key, int pageNumber) {
        try {
            String responseStr = cryptoWallet.listOfferings(key, pageNumber);
            return new Response(true, responseStr);
        } catch (UnauthorizedUserException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You are not logged in! Please log in to your account!";
            return new Response(false, responseStr);
        }
    }

    private Response buyAsset(SelectionKey key, String assetId, double amount) {
        try {
            cryptoWallet.buyAsset(key, assetId, amount);
            String responseStr = "You successfully bought " +  assetId +  " for " + String.format("%.4f", amount) + "$";
            return new Response(true, responseStr);
        } catch (UnauthorizedUserException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You are not logged in! Please log in to your account!";
            return new Response(false, responseStr);
        } catch (InsufficientBalanceException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You do not have enough money!";
            return new Response(false, responseStr);
        } catch (InvalidAssetIdException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "There is no asset with this id!";
            return new Response(false, responseStr);
        }
    }

    private Response sellAsset(SelectionKey key, String assetId) {
        try {
            double earnedMoney = cryptoWallet.sellAsset(key, assetId);
            String responseStr = "You successfully sold your actives from " + assetId + " and earned " + String.format("%.4f", earnedMoney) + "$";
            return new Response(true, responseStr);
        } catch (UnauthorizedUserException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You are not logged in! Please log in to your account!";
            return new Response(false, responseStr);
        } catch (InvalidAssetIdException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "There is no asset with this id!";
            return new Response(false, responseStr);
        }
    }

    private Response getWalletSummary(SelectionKey key) {
        try {
            String responseStr = cryptoWallet.getWalletSummary(key);
            return new Response(true, responseStr);
        } catch (UnauthorizedUserException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You are not logged in! Please log in to your account!";
            return new Response(false, responseStr);
        }
    }

    private Response getWalletOverallSummary(SelectionKey key) {
        try {
            String responseStr = cryptoWallet.getWalletOverallSummary(key);
            return new Response(true, responseStr);
        } catch (UnauthorizedUserException e) {
            ErrorLogger.log("Message: " + e.getMessage() + " | Stack Trace: " +  Arrays.toString(e.getStackTrace()));
            String responseStr = "You are not logged in! Please log in to your account!";
            return new Response(false, responseStr);
        }
    }
}

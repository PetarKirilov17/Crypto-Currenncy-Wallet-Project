package src.bg.uni.sofia.fmi.mjt.wallet.command;

import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.CryptoWalletAPI;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.InsufficientBalanceException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.InvalidAssetIdException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.LoginAuthenticationException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.PasswordWrongFormatException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UnauthorizedUserException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UserAlreadyExistsException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UserNotFoundException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UsernameWrongFormatException;

import java.nio.channels.SelectionKey;

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
                throw new RuntimeException("Command cannot be null!");
            }
            return switch (command.commandLabel()) {
                case REGISTER -> register(key, command.arguments()[0], command.arguments()[1]);
                case LOGIN -> login(key, command.arguments()[0], command.arguments()[1]);
                case DEPOSIT_MONEY -> depositMoney(key, Double.parseDouble(command.arguments()[0]));
                case LIST_OFFERINGS -> listOfferings(key);
                case BUY_ASSET -> buyAsset(key, command.arguments()[0], Double.parseDouble(command.arguments()[1]));
                case SELL_ASSET -> sellAsset(key, command.arguments()[0]);
                case WALLET_SUMMARY -> getWalletSummary(key);
                case WALLET_OVERALL_SUMMARY -> getWalletOverallSummary(key);
                default -> new Response(false, UNKNOWN_COMMAND);
            };
        }catch (Exception e){
            return new Response(false, ERROR + e.getMessage());
        }
    }

    //TODO: add loggers
    private Response register(SelectionKey key, String username, String password) {
        boolean isOk = false;
        String responseStr;
        try {
            cryptoWallet.register(key, username, password);
            isOk = true;
            responseStr = "You successfully signed up!";
        } catch (UsernameWrongFormatException e) {
            responseStr = "The username that you provided is in wrong format!";
        } catch (UserAlreadyExistsException e) {
            responseStr = "You cannot register with this username because there is an user with the same username!";
        } catch (PasswordWrongFormatException e) {
            responseStr = "The password that you provided is in wrong format!";
        }
        return new Response(isOk, responseStr);
    }

    private Response login(SelectionKey key, String username, String password) {
        boolean isOk = false;
        String responseStr;
        try {
            cryptoWallet.login(key, username, password);
            isOk = true;
            responseStr = "You successfully logged in!";
        } catch (UserNotFoundException e) {
            responseStr = "There is no user with this username!";
        } catch (UsernameWrongFormatException e) {
            responseStr = "The username that you provided is in wrong format!";
        } catch (LoginAuthenticationException e) {
            responseStr = "Your username or password is incorrect!";
        } catch (PasswordWrongFormatException e) {
            responseStr = "The password that you provided is in wrong format!";
        }
        return new Response(isOk, responseStr);
    }

    private Response depositMoney(SelectionKey key, double amount) {
        boolean isOk = false;
        String responseStr;
        try {
            cryptoWallet.depositMoney(key, amount);
            isOk = true;
            responseStr = "Successfully deposited " + String.format("%.4f", amount) + "$";
        } catch (UnauthorizedUserException e) {
            responseStr = "You are not logged in! Please log in to your account!";
        }
        return new Response(isOk, responseStr);
    }

    private Response listOfferings(SelectionKey key) {
        boolean isOk = false;
        String responseStr;
        try {
            responseStr = cryptoWallet.listOfferings(key);
            isOk = true;
        } catch (UnauthorizedUserException e) {
            responseStr = "You are not logged in! Please log in to your account!";
        }
        return new Response(isOk, responseStr);
    }

    private Response buyAsset(SelectionKey key, String assetId, double amount) {
        boolean isOk = false;
        String responseStr;
        try {
            cryptoWallet.buyAsset(key, assetId, amount);
            isOk = true;
            responseStr = "You successfully bought " +  assetId +  " for " + String.format("%.4f", amount) + "$";
        } catch (UnauthorizedUserException e) {
            responseStr = "You are not logged in! Please log in to your account!";
        } catch (InsufficientBalanceException e) {
            responseStr = "You do not have enough money!";
        } catch (InvalidAssetIdException e) {
            responseStr = "There is no asset with this id!";
        }
        return new Response(isOk, responseStr);
    }

    private Response sellAsset(SelectionKey key, String assetId) {
        boolean isOk = false;
        String responseStr;
        try {
            double earnedMoney = cryptoWallet.sellAsset(key, assetId);
            isOk = true;
            responseStr = "You successfully sold your actives from " + assetId + " and earned " + String.format("%.4f", earnedMoney) + "$";
        } catch (UnauthorizedUserException e) {
            responseStr = "You are not logged in! Please log in to your account!";
        } catch (InvalidAssetIdException e) {
            responseStr = "There is no asset with this id!";
        }
        return new Response(isOk, responseStr);
    }

    private Response getWalletSummary(SelectionKey key) {
        boolean isOk = false;
        String responseStr;
        try {
            responseStr = cryptoWallet.getWalletSummary(key);
            isOk = true;
        } catch (UnauthorizedUserException e) {
            responseStr = "You are not logged in! Please log in to your account!";
        }
        return new Response(isOk, responseStr);
    }

    private Response getWalletOverallSummary(SelectionKey key) {
        boolean isOk = false;
        String responseStr;
        try {
            responseStr = cryptoWallet.getWalletOverallSummary(key);
            isOk = true;
        } catch (UnauthorizedUserException e) {
            responseStr = "You are not logged in! Please log in to your account!";
        }
        return new Response(isOk, responseStr);
    }
}

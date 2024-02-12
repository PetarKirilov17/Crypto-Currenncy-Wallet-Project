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
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String DEPOSIT_MONEY = "deposit-money";
    private static final String LIST_OFFERINGS = "list-offerings";
    private static final String BUY_ASSET = "buy";
    private static final String SELL_ASSET = "sell";
    private static final String WALLET_SUMMARY = "get-wallet-summary";
    private static final String WALLET_OVERALL_SUMMARY = "get-wallet-overall-summary";
    private static final String STOP = "stop";
    private static final String ERROR = "Error: ";
    private static final String UNKNOWN_COMMAND = "Unknown command";

    private CryptoWalletAPI cryptoWallet;

    public CommandExecutor(CryptoWalletAPI cryptoWallet) {
        this.cryptoWallet = cryptoWallet;
    }

    public String execute(SelectionKey key, Command command) {
        return switch (command.command()){
            case REGISTER -> register(key, command.arguments()[0], command.arguments()[1]);
            case LOGIN -> login(key, command.arguments()[0], command.arguments()[1]);
            case DEPOSIT_MONEY -> depositMoney(key, Double.parseDouble(command.arguments()[0]));
            case LIST_OFFERINGS -> listOfferings(key);
            case BUY_ASSET -> buyAsset(key, command.arguments()[0], Double.parseDouble(command.arguments()[1]));
            case SELL_ASSET -> sellAsset(key, command.arguments()[0]);
            case WALLET_SUMMARY -> getWalletSummary(key);
            case WALLET_OVERALL_SUMMARY -> getWalletOverallSummary(key);
            default -> UNKNOWN_COMMAND;
        };
    }

    //TODO: add loggers
    private String register(SelectionKey key, String username, String password){
        String response;
        try {
            cryptoWallet.register(key, username, password);
            response = "You successfully signed up!";
        } catch (UsernameWrongFormatException e) {
            response = "The username that you provided is in wrong format!";
        } catch (UserAlreadyExistsException e) {
            response = "You cannot register with this username because there is an user with the same username!";
        } catch (PasswordWrongFormatException e) {
            response = "The password that you provided is in wrong format!";
        }
        return response;
    }

    private String login(SelectionKey key, String username, String password){
        String response;
        try{
            cryptoWallet.login(key, username, password);
            response = "You successfully logged in!";
        } catch (UserNotFoundException e) {
            response = "There is no user with this username!";
        } catch (UsernameWrongFormatException e) {
            response = "The username that you provided is in wrong format!";
        } catch (LoginAuthenticationException e) {
            response = "Your username or password is incorrect!";
        } catch (PasswordWrongFormatException e) {
            response = "The password that you provided is in wrong format!";
        }
        return response;
    }

    private String depositMoney(SelectionKey key, double amount){
        String response;
        try {
            cryptoWallet.depositMoney(key, amount);
            response = "Successfully deposited " + amount + "$";
        } catch (UnauthorizedUserException e) {
            response = "You are not logged in! Please log in to your account!";
        }
        return response;
    }

    private String listOfferings(SelectionKey key){
        String response;
        try {
            response = cryptoWallet.listOfferings(key);
        } catch (UnauthorizedUserException e) {
            response = "You are not logged in! Please log in to your account!";
        }
        return response;
    }

    private String buyAsset(SelectionKey key, String assetId, double amount) {
        String response;
        try {
            cryptoWallet.buyAsset(key, assetId, amount);
            response = "You successfully bought " + amount + "amount of " + assetId;
        } catch (UnauthorizedUserException e) {
            response = "You are not logged in! Please log in to your account!";
        } catch (InsufficientBalanceException e) {
            response = "You do not have enough money!";
        } catch (InvalidAssetIdException e) {
            response = "There is no asset with this id!";
        }
        return response;
    }
    private String sellAsset(SelectionKey key, String assetId){
        String response;
        try {
            cryptoWallet.sellAsset(key, assetId);
            response = "You successfully sold your actives from " + assetId;
        } catch (UnauthorizedUserException e) {
            response = "You are not logged in! Please log in to your account!";
        } catch (InvalidAssetIdException e) {
            response = "There is no asset with this id!";
        }
        return response;
    }
    private String getWalletSummary(SelectionKey key){
        String response;
        try {
            response = cryptoWallet.getWalletSummary(key);
        } catch (UnauthorizedUserException e) {
            response = "You are not logged in! Please log in to your account!";
        }
        return response;
    }
    private String getWalletOverallSummary(SelectionKey key){
        String response;
        try {
            response = cryptoWallet.getWalletOverallSummary(key);
        } catch (UnauthorizedUserException e) {
            response = "You are not logged in! Please log in to your account!";
        }
        return response;
    }
}

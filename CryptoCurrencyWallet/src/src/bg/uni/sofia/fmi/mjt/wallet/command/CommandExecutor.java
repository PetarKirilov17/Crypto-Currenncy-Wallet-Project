package src.bg.uni.sofia.fmi.mjt.wallet.command;

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

    public CommandExecutor() {

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

    private String register(SelectionKey key, String username, String password){
        //TODO: add custom exceptions
        return "";
    }

    private String login(SelectionKey key, String username, String password){
        //TODO: add custom exceptions
        return "";
    }

    private String depositMoney(SelectionKey key, double amount){
        return "";
    }

    private String listOfferings(SelectionKey key){
        return "";
    }

    private String buyAsset(SelectionKey key, String assetId, double amount) {
        return "";
    }
    private String sellAsset(SelectionKey key, String assetId){
        return "";
    }
    private String getWalletSummary(SelectionKey key){
        return "";
    }
    private String getWalletOverallSummary(SelectionKey key){
        return "";
    }
}

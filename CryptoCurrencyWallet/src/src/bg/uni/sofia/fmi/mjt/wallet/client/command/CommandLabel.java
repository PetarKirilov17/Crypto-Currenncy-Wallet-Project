package src.bg.uni.sofia.fmi.mjt.wallet.client.command;

import src.bg.uni.sofia.fmi.mjt.wallet.client.exception.InvalidUserCommandException;

public enum CommandLabel {
    REGISTER("register"),
    LOGIN("login"),
    DEPOSIT_MONEY("deposit-money"),
    LIST_OFFERINGS("list-offerings"),
    BUY_ASSET("buy"),
    SELL_ASSET("sell"),
    WALLET_SUMMARY("get-wallet-summary"),
    WALLET_OVERALL_SUMMARY("get-wallet-overall-summary"),
    HELP("help"),
    LOG_OUT("log-out"),
    QUIT("quit");

    public final String userCommand;

    CommandLabel(String userCommand) {
        this.userCommand = userCommand;
    }

    public static CommandLabel getByUserCommand(String userCommandValue) throws InvalidUserCommandException {
        for (CommandLabel command : values()) {
            if (command.userCommand.equals(userCommandValue)) {
                return command;
            }
        }
        throw new InvalidUserCommandException("Wrong user command: " + userCommandValue);
    }
}

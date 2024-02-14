package bg.uni.sofia.fmi.mjt.wallet.client.command;

import bg.uni.sofia.fmi.mjt.wallet.client.CryptoWalletClient;
import bg.uni.sofia.fmi.mjt.wallet.client.SessionInfo;
import bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;
import com.google.gson.Gson;

public class CommandExecutor {
    private UI ui;
    private CryptoWalletClient httpClient;

    private CommandValidator validator;
    private Gson gson;

    private final SessionInfo sessionInfo;

    public CommandExecutor(UI ui, CryptoWalletClient httpClient, CommandValidator validator, SessionInfo sessionInfo) {
        this.ui = ui;
        this.httpClient = httpClient;
        this.validator = validator;
        this.sessionInfo = sessionInfo;
        gson = new Gson();
    }

    public void execute(Command command) {
        if (command.getCommandLabel().equals(CommandLabel.HELP)) {
            printHelpMenu();
            return;
        }
        try {
            if (!sessionInfo.isLoggedIn()) {
                switch (command.getCommandLabel()) {
                    case REGISTER -> register(command);
                    case LOGIN -> login(command);
                    default -> ui.writeError("Please log in to your account first!");
                }
            } else {
                switch (command.getCommandLabel()) {
                    case DEPOSIT_MONEY -> depositMoney(command);
                    case BUY_ASSET -> buyAsset(command);
                    case SELL_ASSET -> sellAsset(command);
                    case LIST_OFFERINGS -> listOfferings(command);
                    case WALLET_SUMMARY, WALLET_OVERALL_SUMMARY -> noArgsCommand(command);
                    case LOG_OUT -> logOut();
                    default -> ui.writeError("Invalid command!");
                }
            }
        } catch (Exception e) {
            ui.writeError("Something went wrong. Probably the server is down. Try restarting the program");
        }
    }

    private void printHelpMenu() {
        if (!sessionInfo.isLoggedIn()) {
            ui.write(CommandLabel.REGISTER.userCommand + " <username> <password>");
            ui.write(CommandLabel.LOGIN.userCommand + " <username> <password>");
            ui.write(CommandLabel.QUIT.userCommand);
            return;
        }
        ui.write(CommandLabel.DEPOSIT_MONEY.userCommand + " <money>");
        ui.write(CommandLabel.LIST_OFFERINGS.userCommand + " <page number>");
        ui.write(CommandLabel.BUY_ASSET.userCommand + " <assetID> <money>");
        ui.write(CommandLabel.SELL_ASSET.userCommand + " <assetID>");
        ui.write(CommandLabel.WALLET_SUMMARY.userCommand);
        ui.write(CommandLabel.WALLET_OVERALL_SUMMARY.userCommand);
        ui.write(CommandLabel.LOG_OUT.userCommand);
        ui.write(CommandLabel.HELP.userCommand);
    }

    private void printResponse(Command command) {
        var responseStr = httpClient.sendRequest(command);
        Response response = gson.fromJson(responseStr, Response.class);
        if (response.isOk()) {
            ui.write(response.getResponse());
        } else {
            ui.writeError(response.getResponse());
        }
    }

    private void register(Command command) {
        if (!validator.validateRegisterAndSignUp(command)) {
            return;
        }
        Response response = gson.fromJson(httpClient.sendRequest(command), Response.class);

        if (response.isOk()) {
            sessionInfo.logIn(command.getArguments()[0]);
            ui.write(response.getResponse());
        } else {
            ui.writeError(response.getResponse());
        }
    }

    private void login(Command command) {
        if (!validator.validateRegisterAndSignUp(command)) {
            return;
        }
        Response response = gson.fromJson(httpClient.sendRequest(command), Response.class);

        if (response.isOk()) {
            sessionInfo.logIn(command.getArguments()[0]);
            ui.write(response.getResponse());
        } else {
            ui.writeError(response.getResponse());
        }
    }

    private void depositMoney(Command command) {
        if (!validator.validateDepositMoney(command)) {
            return;
        }
        printResponse(command);
    }

    private void buyAsset(Command command) {
        if (!validator.validateBuyAsset(command)) {
            return;
        }
        printResponse(command);
    }

    private void sellAsset(Command command) {
        if (!validator.validateSellAsset(command)) {
            return;
        }
        printResponse(command);
    }

    private void listOfferings(Command command) {
        if (!validator.validateListOfferings(command)) {
            return;
        }
        printResponse(command);
    }

    private void noArgsCommand(Command command) {
        if (!validator.validateCommandsWithNoArguments(command)) {
            return;
        }
        printResponse(command);
    }

    private void logOut() {
        sessionInfo.logOut();
        ui.write("Logged out");
    }

}

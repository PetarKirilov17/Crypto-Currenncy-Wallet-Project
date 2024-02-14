package bg.uni.sofia.fmi.mjt.wallet.client;

import bg.uni.sofia.fmi.mjt.wallet.client.command.Command;
import bg.uni.sofia.fmi.mjt.wallet.client.command.CommandCreator;
import bg.uni.sofia.fmi.mjt.wallet.client.command.CommandExecutor;
import bg.uni.sofia.fmi.mjt.wallet.client.command.CommandLabel;
import bg.uni.sofia.fmi.mjt.wallet.client.command.CommandValidator;
import bg.uni.sofia.fmi.mjt.wallet.client.exception.InvalidUserCommandException;
import bg.uni.sofia.fmi.mjt.wallet.client.exception.ServerNotFoundException;
import bg.uni.sofia.fmi.mjt.wallet.client.ui.ConsoleUI;
import bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;

public class RunClient {
    public static void main(String[] args) {
        UI ui = new ConsoleUI();
        CryptoWalletClient cryptoWalletClient = new CryptoWalletClient();
        try {
            cryptoWalletClient.run();
        } catch (ServerNotFoundException e) {
            ui.writeError(e.getMessage());
            ui.writeError("You need to start the server!");
            return;
        }
        SessionInfo sessionInfo = new SessionInfo();
        CommandValidator validator = new CommandValidator(ui);
        CommandExecutor commandExecutor = new CommandExecutor(ui, cryptoWalletClient, validator, sessionInfo);
        CommandCreator commandCreator = new CommandCreator(ui);
        ui.write("Welcome to Crypto Wallet! Type help to see the available commands: ");
        while (true) {
            Command command;
            try {
                command = commandCreator.readCommand(); //reads command from the user input and validate it
            } catch (InvalidUserCommandException e) {
                ui.writeError(e.getMessage());
                continue;
            }
            if (command.getCommandLabel().equals(CommandLabel.QUIT)) {
                cryptoWalletClient.stop();
                return;
            }
            commandExecutor.execute(command);
        }
    }
}

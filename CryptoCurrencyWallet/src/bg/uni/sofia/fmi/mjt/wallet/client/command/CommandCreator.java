package bg.uni.sofia.fmi.mjt.wallet.client.command;

import bg.uni.sofia.fmi.mjt.wallet.client.exception.InvalidUserCommandException;
import bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;

import java.util.Arrays;
import java.util.List;

public class CommandCreator {
    private static final String INPUT_SEPARATOR = " ";
    private UI ui;

    public CommandCreator(UI ui){
        this.ui = ui;
    }

    public Command readCommand() throws InvalidUserCommandException {
        String input = ui.read();
        List<String> tokens = Arrays.stream(input.split(INPUT_SEPARATOR)).toList();
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);
        CommandLabel label = CommandLabel.getByUserCommand(tokens.get(0));
        return new Command(label, args);
    }
}

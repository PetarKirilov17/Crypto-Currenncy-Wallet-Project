package bg.uni.sofia.fmi.mjt.wallet.client.command;

public class Command {
    private CommandLabel commandLabel;
    private String[] arguments;

    public Command(CommandLabel commandLabel, String[] arguments) {
        this.commandLabel = commandLabel;
        this.arguments = arguments;
    }

    public CommandLabel getCommandLabel() {
        return commandLabel;
    }

    public String[] getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(commandLabel.userCommand);
        for (var arg : getArguments()) {
            res.append(" ").append(arg);
        }
        return res.toString();
    }
}

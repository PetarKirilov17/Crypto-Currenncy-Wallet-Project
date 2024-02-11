package src.bg.uni.sofia.fmi.mjt.wallet.client.command;

import com.google.gson.Gson;
import src.bg.uni.sofia.fmi.mjt.wallet.client.CryptoWalletClient;
import src.bg.uni.sofia.fmi.mjt.wallet.client.SessionInfo;
import src.bg.uni.sofia.fmi.mjt.wallet.client.ui.UI;

public class CommandExecutor {
    private UI ui;
    private CryptoWalletClient httpClient;

    private Gson gson;


    public CommandExecutor(UI ui, CryptoWalletClient httpClient) {
        this.ui = ui;
        this.httpClient = httpClient;
        gson = new Gson();
    }

    public void execute(Command command){
        if(command.getCommandLabel().equals(CommandLabel.HELP)){
            printHelpMenu();
            return;
        }
        printResponse(command);
    }

    private void printHelpMenu(){
        if(!SessionInfo.isLoggedIn()){
            ui.write(CommandLabel.REGISTER.userCommand + " <username> <password>");
            ui.write(CommandLabel.LOGIN.userCommand + " <username> <password>");
            ui.write(CommandLabel.QUIT.userCommand);
            return;
        }
        ui.write(CommandLabel.DEPOSIT_MONEY.userCommand + " <money>");
        ui.write(CommandLabel.LIST_OFFERINGS.userCommand);
        ui.write(CommandLabel.BUY_ASSET.userCommand + " <assetID> <money amount>");
        ui.write(CommandLabel.SELL_ASSET.userCommand + " <assetID>");
        ui.write(CommandLabel.WALLET_SUMMARY.userCommand);
        ui.write(CommandLabel.WALLET_OVERALL_SUMMARY.userCommand);
    }

    private void printResponse(Command command){
        String response = httpClient.sendRequest(command);
        ui.write(response);
    }

}

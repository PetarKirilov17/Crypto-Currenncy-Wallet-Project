package src.bg.uni.sofia.fmi.mjt.wallet.server;

import src.bg.uni.sofia.fmi.mjt.wallet.command.CommandExecutor;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.CryptoWalletAPI;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.CryptoWalletController;
import src.bg.uni.sofia.fmi.mjt.wallet.database.Database;
import src.bg.uni.sofia.fmi.mjt.wallet.database.FileDatabase;

public class RunServer {
    public static void main(String[] args){
        //TODO: start the server
        Database database = new FileDatabase();
        CryptoWalletAPI wallet = new CryptoWalletController(database);
        CommandExecutor commandExecutor = new CommandExecutor(wallet);
        CryptoWalletServer server = new CryptoWalletServer(commandExecutor);
        server.start();
    }
}

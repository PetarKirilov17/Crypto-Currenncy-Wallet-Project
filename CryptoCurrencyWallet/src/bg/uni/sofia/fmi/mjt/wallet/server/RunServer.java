package bg.uni.sofia.fmi.mjt.wallet.server;

import bg.uni.sofia.fmi.mjt.wallet.server.command.CommandExecutor;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoWalletAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoWalletController;
import bg.uni.sofia.fmi.mjt.wallet.server.database.Database;
import bg.uni.sofia.fmi.mjt.wallet.server.database.FileDatabase;

public class RunServer {
    public static void main(String[] args){
        Database database = new FileDatabase();
        CryptoWalletAPI wallet = new CryptoWalletController(database);
        CommandExecutor commandExecutor = new CommandExecutor(wallet);
        CryptoWalletServer server = new CryptoWalletServer(commandExecutor);
        server.start();
    }
}

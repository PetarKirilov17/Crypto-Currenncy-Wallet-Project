package bg.uni.sofia.fmi.mjt.wallet.server;

import bg.uni.sofia.fmi.mjt.wallet.server.command.CommandExecutor;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoAssetUpdater;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoWalletAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoWalletController;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.CryptoConsumerAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.SyncCryptoConsumer;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service.UserService;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service.UserServiceAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service.WalletService;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service.WalletServiceAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.database.Database;
import bg.uni.sofia.fmi.mjt.wallet.server.database.FileDatabase;
import bg.uni.sofia.fmi.mjt.wallet.server.database.password.MDPasswordHasher;
import bg.uni.sofia.fmi.mjt.wallet.server.database.password.PasswordHasherAPI;

import java.net.http.HttpClient;
import java.nio.file.Path;

public class RunServer {
    private static final String USERS_FILE_PATH =
        "users.txt";
    private static final String RES_DIRECTORY = "res";

    private static final int ASSETS_TO_SHOW = 20;

    public static void main(String[] args) {
        Path usersPath = Path.of(RES_DIRECTORY, USERS_FILE_PATH).toAbsolutePath();
        Database database = new FileDatabase(usersPath);

        HttpClient httpClient = HttpClient.newBuilder().build();
        CryptoConsumerAPI consumerAPI = new SyncCryptoConsumer(httpClient);
        CryptoAssetUpdater updater = new CryptoAssetUpdater(consumerAPI);
        WalletServiceAPI walletServiceAPI = new WalletService(database, updater);
        PasswordHasherAPI passwordHasherAPI = new MDPasswordHasher();
        UserServiceAPI userService = new UserService(database, passwordHasherAPI);

        CryptoWalletAPI wallet = new CryptoWalletController(userService, walletServiceAPI);
        CommandExecutor commandExecutor = new CommandExecutor(wallet);
        CryptoWalletServer server = new CryptoWalletServer(commandExecutor);
        server.start();
    }
}

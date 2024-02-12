package src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet;

import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.CryptoConsumerAPI;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.SyncCryptoConsumer;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets.CryptoAsset;
import src.bg.uni.sofia.fmi.mjt.wallet.database.Database;
import src.bg.uni.sofia.fmi.mjt.wallet.database.password.MDPasswordHasher;
import src.bg.uni.sofia.fmi.mjt.wallet.database.password.PasswordHasherAPI;
import src.bg.uni.sofia.fmi.mjt.wallet.database.user.Purchase;
import src.bg.uni.sofia.fmi.mjt.wallet.database.user.User;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.InsufficientBalanceException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.InvalidAssetIdException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.LoginAuthenticationException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.PasswordWrongFormatException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UnauthorizedUserException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UserAlreadyExistsException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UserNotFoundException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UsernameWrongFormatException;
import src.bg.uni.sofia.fmi.mjt.wallet.validation.StringValidator;

import java.net.http.HttpClient;
import java.nio.channels.SelectionKey;
import java.util.LinkedHashMap;
import java.util.Map;

public class CryptoWalletController implements CryptoWalletAPI {
    private static final int ASSETS_LIST_SIZE = 20;
    private final HttpClient client;
    private final Database database;
    private final Map<String, CryptoAsset> assets;
    private final PasswordHasherAPI passwordHasher;
    private final CryptoConsumerAPI consumerAPI;
    private final CryptoAssetUpdater cryptoAssetUpdater;

    public CryptoWalletController(Database database) {
        this.database = database;
        client = HttpClient.newBuilder().build();
        consumerAPI = new SyncCryptoConsumer(client);
        this.passwordHasher = new MDPasswordHasher();
        this.assets = new LinkedHashMap<>();
        cryptoAssetUpdater = new CryptoAssetUpdater(consumerAPI);
        cryptoAssetUpdater.updateAllAssetsIfNeeded(assets);
    }

    @Override
    public void register(SelectionKey key, String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserAlreadyExistsException {
        validateStrings(username, password);
        if (database.checkIfUserExists(username)) {
            throw new UserAlreadyExistsException("There is already an user with this username!");
        }
        String hashedPassword = passwordHasher.hashPassword(password);
        User user = new User(username, hashedPassword);
        database.addUser(user);
        key.attach(user);
    }

    @Override
    public void login(SelectionKey key, String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserNotFoundException,
        LoginAuthenticationException {
        validateStrings(username, password);
        if (!database.checkIfUserExists(username)) {
            throw new UserNotFoundException("There is no user with username: " + username);
        }
        User user = database.getUserByUsername(username);
        String hashedPassword = passwordHasher.hashPassword(password);
        if (!user.login(username, hashedPassword)) {
            throw new LoginAuthenticationException("Incorrect username or password!");
        }
        key.attach(user);
    }

    @Override
    public void depositMoney(SelectionKey key, double amount) throws UnauthorizedUserException {
        checkAuthorization(key);
        User currentUser = (User) key.attachment();
        currentUser.increaseBalance(amount);
        database.updateUser(currentUser);
    }

    @Override
    public String listOfferings(SelectionKey key) throws UnauthorizedUserException {
        checkAuthorization(key);
        cryptoAssetUpdater.updateAllAssetsIfNeeded(assets);
        StringBuilder sb = new StringBuilder();
        var assetList = assets.values().stream().filter(a -> a.priceUSD()!=null).limit(ASSETS_LIST_SIZE).toList();
        for (var a : assetList) {
            sb.append("Asset ID: ").append(a.assetId()).append(" -> ").append("Price: ")
                .append(String.format("%.4f", a.priceUSD())).append("$ per unit!");
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public void buyAsset(SelectionKey key, String assetId, double money)
        throws UnauthorizedUserException, InvalidAssetIdException, InsufficientBalanceException {
        checkAuthorization(key);

        cryptoAssetUpdater.updateAssetIfNeeded(assets, assetId);

        User currentUser = (User) key.attachment();
        CryptoAsset asset = getAssetByAssetId(assetId);

        if (Double.compare(currentUser.getBalance(), money) < 0) {
            throw new InsufficientBalanceException(
                "There is no enough money in your account! Please first deposit money!");
        }

        double previousAmount = currentUser.getAmountOfAsset(assetId);
        double previousAvg = currentUser.getAvgPrice(assetId);
        double amountToBuy = money / asset.priceUSD();

        double newAvg =
            (previousAmount * previousAvg + amountToBuy * asset.priceUSD()) / (previousAmount + amountToBuy);
        currentUser.removePurchase(assetId);
        Purchase purchase = new Purchase(assetId,  previousAmount + amountToBuy, newAvg);
        currentUser.addPurchase(purchase);
        currentUser.decreaseBalance(money);
        database.updateUser(currentUser);
    }

    @Override
    public double sellAsset(SelectionKey key, String assetId) throws UnauthorizedUserException, InvalidAssetIdException {
        checkAuthorization(key);

        cryptoAssetUpdater.updateAssetIfNeeded(assets, assetId);

        User currentUser = (User) key.attachment();
        if(!currentUser.containsAsset(assetId)){
            throw new InvalidAssetIdException("You do not have purchases from " + assetId);
        }
        CryptoAsset asset = getAssetByAssetId(assetId);

        double amountOfAsset = currentUser.getAmountOfAsset(assetId);
        currentUser.removePurchase(assetId);
        double receivingMoney = asset.priceUSD() * amountOfAsset;
        currentUser.increaseBalance(receivingMoney);
        database.updateUser(currentUser);
        return receivingMoney;
    }

    @Override
    public String getWalletSummary(SelectionKey key) throws UnauthorizedUserException {
        checkAuthorization(key);
        User currentUser = (User) key.attachment();
        StringBuilder sb = new StringBuilder();
        sb.append("Current balance: ").append(String.format("%.4f", currentUser.getBalance())).append("$");
        sb.append(System.lineSeparator());
        for (var it : currentUser.getPurchases()) {
            sb.append("Asset ID: ").append(it.assetId()).append(" Amount: ").append(String.format("%.4f", it.amount()));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public String getWalletOverallSummary(SelectionKey key) throws UnauthorizedUserException {
        checkAuthorization(key);
        cryptoAssetUpdater.updateAllAssetsIfNeeded(assets);
        User currentUser = (User) key.attachment();
        var userPurchases = currentUser.getPurchases();
        StringBuilder sb = new StringBuilder();
        for(var up : userPurchases){
            CryptoAsset currentAsset = this.assets.get(up.assetId());
            double difference = up.amount() * currentAsset.priceUSD() - up.amount() * up.avgPrice();
            if(difference == 0.0){
                sb.append("Asset ID: ").append(up.assetId()).append(" | Amount: ")
                    .append(String.format("%.4f", up.amount())).append(" | You do not have profit or loss!");
            }
            else if(difference > 0.0){
                sb.append("Asset ID: ").append(up.assetId()).append(" | Amount: ")
                    .append(String.format("%.4f", up.amount())).append(" | UP: ").append(String.format("%.4f", difference)).append("$");
            }else{
                difference = -difference;
                sb.append("Asset ID: ").append(up.assetId()).append(" | Amount: ")
                    .append(String.format("%.4f", up.amount())).append(" | DOWN: ").append(String.format("%.4f", difference)).append("$");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private void validateStrings(String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException {
        if (username == null || !StringValidator.isValidUsername(username)) {
            throw new UsernameWrongFormatException("Wrong format of the username");
        }
        if (password == null || !StringValidator.isValidPassword(password)) {
            throw new PasswordWrongFormatException("Wrong format of the password!");
        }
    }

    private void checkAuthorization(SelectionKey key) throws UnauthorizedUserException {
        if (key.attachment() == null) {
            throw new UnauthorizedUserException("You need to log in to your account first!");
        }
    }

    private CryptoAsset getAssetByAssetId(String assetId) throws InvalidAssetIdException {
        if (!assets.containsKey(assetId)) {
            throw new InvalidAssetIdException("Asset ID is invalid!");
        }
        return assets.get(assetId);
    }
}

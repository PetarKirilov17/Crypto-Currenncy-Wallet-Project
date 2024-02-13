package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoAssetUpdater;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets.CryptoAsset;
import bg.uni.sofia.fmi.mjt.wallet.server.database.Database;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.Purchase;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InsufficientBalanceException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidAssetIdException;

import java.util.LinkedHashMap;
import java.util.Map;

public class WalletService implements WalletServiceAPI {
    private static final int ASSETS_LIST_SIZE = 20;
    private final Database database;
    private final CryptoAssetUpdater cryptoAssetUpdater;
    private final Map<String, CryptoAsset> assets;
    public WalletService(Database database, CryptoAssetUpdater updater){
        this.database = database;
        this.cryptoAssetUpdater = updater;
        this.assets = new LinkedHashMap<>();
        cryptoAssetUpdater.updateAllAssetsIfNeeded(assets);
    }

    @Override
    public void depositMoney(User user, double money) {
        user.increaseBalance(money);
        database.updateUser(user);
    }

    @Override
    public String listOfferings(int pageNumber) {
        cryptoAssetUpdater.updateAllAssetsIfNeeded(assets);
        StringBuilder sb = new StringBuilder();
        var assetList = assets.values().stream().filter(a -> a.priceUSD()!=null && a.priceUSD() > 0.0001).skip(
            (long) (pageNumber - 1) *ASSETS_LIST_SIZE ).limit(ASSETS_LIST_SIZE).toList();
        for (var a : assetList) {
            sb.append("Asset ID: ").append(a.assetId()).append(" -> ").append("Price: ")
                .append(String.format("%.4f", a.priceUSD())).append("$ per unit!");
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public void buyAsset(User user, String assetId, double money)
        throws InsufficientBalanceException, InvalidAssetIdException {
        cryptoAssetUpdater.updateAssetIfNeeded(assets, assetId);

        CryptoAsset asset = getAssetByAssetId(assetId);

        if (Double.compare(user.getBalance(), money) < 0) {
            throw new InsufficientBalanceException(
                "There is no enough money in your account! Please first deposit money!");
        }

        double previousAmount = user.getAmountOfAsset(assetId);
        double previousAvg = user.getAvgPrice(assetId);
        double amountToBuy = money / asset.priceUSD();

        double newAvg =
            (previousAmount * previousAvg + amountToBuy * asset.priceUSD()) / (previousAmount + amountToBuy);
        user.removePurchase(assetId);
        Purchase purchase = new Purchase(assetId,  previousAmount + amountToBuy, newAvg);
        user.addPurchase(purchase);
        user.decreaseBalance(money);
        database.updateUser(user);
    }

    @Override
    public double sellAsset(User user, String assetId) throws InvalidAssetIdException {
        cryptoAssetUpdater.updateAssetIfNeeded(assets, assetId);

        if(!user.containsAsset(assetId)){
            throw new InvalidAssetIdException("You do not have purchases from " + assetId);
        }
        CryptoAsset asset = getAssetByAssetId(assetId);

        double amountOfAsset = user.getAmountOfAsset(assetId);
        user.removePurchase(assetId);
        double receivingMoney = asset.priceUSD() * amountOfAsset;
        user.increaseBalance(receivingMoney);
        database.updateUser(user);
        return receivingMoney;
    }

    @Override
    public String getWalletSummary(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Current balance: ").append(String.format("%.4f", user.getBalance())).append("$");
        sb.append(System.lineSeparator());
        for (var it : user.getPurchases()) {
            sb.append("Asset ID: ").append(it.assetId()).append(" Amount: ").append(String.format("%.4f", it.amount()));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public String getWalletOverallSummary(User user) {
        cryptoAssetUpdater.updateAllAssetsIfNeeded(assets);
        var userPurchases = user.getPurchases();
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

    private CryptoAsset getAssetByAssetId(String assetId) throws InvalidAssetIdException {
        if (!assets.containsKey(assetId)) {
            throw new InvalidAssetIdException("Asset ID is invalid!");
        }
        return assets.get(assetId);
    }
}

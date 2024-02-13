package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service;

import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InsufficientBalanceException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidAssetIdException;

public interface WalletServiceAPI {
    void depositMoney(User user, double money);

    String listOfferings(int pageNumber);

    void buyAsset(User user, String assetId, double money) throws InsufficientBalanceException, InvalidAssetIdException;

    double sellAsset(User user, String assetId) throws InvalidAssetIdException;

    String getWalletSummary(User user);

    String getWalletOverallSummary(User user);
}

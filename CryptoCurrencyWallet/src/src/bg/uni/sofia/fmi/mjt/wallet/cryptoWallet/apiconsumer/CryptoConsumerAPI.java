package src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer;

import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets.Asset;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets.CryptoAsset;

import java.util.List;

public interface CryptoConsumerAPI {
    //TODO: change
    List<CryptoAsset> getAllAssets();

    CryptoAsset getAssetById(String id);
}

package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets.CryptoAsset;

import java.util.List;

public interface CryptoConsumerAPI {
    List<CryptoAsset> getAllAssets();

    CryptoAsset getAssetById(String id);
}

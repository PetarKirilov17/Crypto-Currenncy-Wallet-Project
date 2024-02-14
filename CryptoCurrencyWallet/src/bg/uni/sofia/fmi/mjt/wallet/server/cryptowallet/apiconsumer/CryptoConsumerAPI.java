package bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.apiconsumer;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.apiconsumer.assets.CryptoAsset;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidCredentialsForAPIException;

import java.util.List;

public interface CryptoConsumerAPI {
    List<CryptoAsset> getAllAssets() throws InvalidCredentialsForAPIException;

    CryptoAsset getAssetById(String id) throws InvalidCredentialsForAPIException;
}

package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets;

import java.time.LocalDateTime;

public record CryptoAsset(
    String assetId,
    String name,
    Double priceUSD,
    LocalDateTime lastUpdated
) {
}

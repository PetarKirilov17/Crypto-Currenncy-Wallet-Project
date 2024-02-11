package src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CryptoAsset(
    String assetId,
    String name,
    Double priceUSD,
    LocalDateTime lastUpdated
) {
}

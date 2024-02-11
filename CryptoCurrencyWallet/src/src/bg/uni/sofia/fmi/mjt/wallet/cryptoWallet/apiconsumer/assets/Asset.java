package src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets;

import com.google.gson.annotations.SerializedName;

public record Asset(
    @SerializedName("asset_id") String assetId,
    String name,
    @SerializedName("type_is_crypto") int typeIsCrypto,
    @SerializedName("data_quote_start") String dataQuoteStart,
    @SerializedName("data_quote_end") String dataQuoteEnd,
    @SerializedName("data_orderbook_start") String dataOrderbookStart,
    @SerializedName("data_orderbook_end") String dataOrderbookEnd,
    @SerializedName("data_trade_start") String dataTradeStart,
    @SerializedName("data_trade_end") String dataTradeEnd,
    @SerializedName("data_symbols_count") Long dataSymbolsCount,
    @SerializedName("volume_1hrs_usd") Double volume1hrsUSD,
    @SerializedName("volume_1day_usd") Double volume1dayUSD,
    @SerializedName("volume_1mth_usd") Double volume1mthUSD,
    @SerializedName("price_usd") Double priceUSD,
    @SerializedName("id_icon") String idIcon,
    @SerializedName("supply_current") Double supplyCurrent,
    @SerializedName("supply_total") Double supplyTotal,
    @SerializedName("supply_max") Double supplyMax,
    @SerializedName("data_start") String dataStart,
    @SerializedName("data_end") String dataEnd
) {
}
package src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets.Asset;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets.CryptoAsset;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SyncCryptoConsumer implements CryptoConsumerAPI{
    private HttpClient client;
    private static final String COIN_API_URL = "https://rest.coinapi.io/v1/assets";

    public SyncCryptoConsumer(HttpClient client){
        this.client = client;
    }
    @Override
    public List<CryptoAsset> getAllAssets() {
        HttpResponse<String> responseStr;
        try {
            URI uri = new URI(COIN_API_URL);
            HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .header("X-CoinAPI-Key", System.getenv("CryptoAPI_KEY"))
                .build();

            responseStr = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Asset[] response = gson.fromJson(responseStr.body(), Asset[].class);
        List<CryptoAsset> result = new ArrayList<>();
        for(var it : response){
            if(it.typeIsCrypto() == 0){
                continue;
            }
            result.add(new CryptoAsset(it.assetId(), it.name(), it.priceUSD(), LocalDateTime.now()));
        }
        return result;
    }

    @Override
    public CryptoAsset getAssetById(String id) {
        HttpResponse<String> responseStr;
        try {
            URI uri = new URI(COIN_API_URL+"/"+id);
            HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .header("X-CoinAPI-Key", System.getenv("CryptoAPI_KEY"))
                .build();

            responseStr = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Asset response = gson.fromJson(responseStr.body(), Asset.class);
        return new CryptoAsset(response.assetId(), response.name(), response.priceUSD(), LocalDateTime.now());
    }
}

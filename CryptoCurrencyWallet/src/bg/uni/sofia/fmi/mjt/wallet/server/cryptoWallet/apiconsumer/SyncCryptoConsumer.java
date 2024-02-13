package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets.Asset;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets.CryptoAsset;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidCredentialsForAPIException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private static final int CREDENTIALS_BAD_REQUEST_STATUS_CODE = 401;

    public SyncCryptoConsumer(HttpClient client){
        this.client = client;
    }
    @Override
    public List<CryptoAsset> getAllAssets() throws InvalidCredentialsForAPIException {
        HttpResponse<String> responseStr;
        URI uri;
        try {
            uri = new URI(COIN_API_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        HttpRequest request = HttpRequest.newBuilder().uri(uri)
            .header("X-CoinAPI-Key", System.getenv("CryptoAPI_KEY"))
            .build();
        try {
            responseStr = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        validateResponse(responseStr.statusCode());
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
    public CryptoAsset getAssetById(String id) throws InvalidCredentialsForAPIException {
        HttpResponse<String> responseStr;
        URI uri;
        try {
            uri = new URI(COIN_API_URL+"/"+id);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        HttpRequest request = HttpRequest.newBuilder().uri(uri)
            .header("X-CoinAPI-Key", System.getenv("CryptoAPI_KEY"))
            .build();
        try{
            responseStr = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        validateResponse(responseStr.statusCode());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Asset[] response = gson.fromJson(responseStr.body(), Asset[].class);
        return new CryptoAsset(response[0].assetId(), response[0].name(), response[0].priceUSD(), LocalDateTime.now());
    }

    private void validateResponse(int responseStatusCode) throws InvalidCredentialsForAPIException {
        if(responseStatusCode == CREDENTIALS_BAD_REQUEST_STATUS_CODE){
            throw new InvalidCredentialsForAPIException("API Key is not specified or it is not correctly formatted!");
        }
    }
}

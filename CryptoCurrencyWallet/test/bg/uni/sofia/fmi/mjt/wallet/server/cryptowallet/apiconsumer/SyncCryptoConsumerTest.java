package bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.apiconsumer;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.apiconsumer.assets.CryptoAsset;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidCredentialsForAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SyncCryptoConsumerTest {
    @Mock
    private HttpClient client;

    @InjectMocks
    private SyncCryptoConsumer consumer;

    @BeforeEach
    void setConsumer(){
        consumer = new SyncCryptoConsumer(client, "testAPIKEY");
    }

    @Test
    void testGetAllAssetsInvalidCredentials() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(401);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        assertThrows(InvalidCredentialsForAPIException.class, () -> consumer.getAllAssets(),
            "Get All Assets should throw InvalidCredentialsForAPIException when status code of the response is 401!");
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetAllAssetsSuccessfully() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("" +
            "[{\"asset_id\":\"BTC\",\"name\":\"Bitcoin\",\"price_usd\":50000, \"type_is_crypto\":1}," +
            "{\"asset_id\":\"ETH\",\"name\":\"Ethereum\",\"price_usd\":3000, \"type_is_crypto\":1}," +
            "{\"asset_id\":\"EUR\",\"name\":\"Euro\",\"price_usd\":1.5, \"type_is_crypto\":0}]");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        List<CryptoAsset> result = consumer.getAllAssets();

        assertNotNull(result, "Result should not be null!");
        assertEquals(2, result.size(), "Size of the result list should as expected!");
        CryptoAsset cryptoAsset = result.get(0);
        assertEquals("BTC", cryptoAsset.assetId(), "Asset ID should be the same as expected!");
        assertEquals("Bitcoin", cryptoAsset.name(), "Asset Name should be the same as expected!");
        assertEquals(50000, cryptoAsset.priceUSD(), "Asset priceUSD should be the same as expected!");
        assertNotNull(cryptoAsset.lastUpdated(), "Asset last modified field should not be null!");
        CryptoAsset cryptoAsset2 = result.get(1);

        assertEquals("ETH", cryptoAsset2.assetId(), "Asset ID should be the same as expected!");
        assertEquals("Ethereum", cryptoAsset2.name(), "Asset Name should be the same as expected!");
        assertEquals(3000, cryptoAsset2.priceUSD(), "Asset priceUSD should be the same as expected!");
        assertNotNull(cryptoAsset2.lastUpdated(), "Asset last modified field should not be null!");
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetAllAssetsThrowsRuntimeExceptionWhenClientThrowsIOException() throws Exception {
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(
            new IOException("IO Exception"));
        assertThrows(RuntimeException.class, () -> consumer.getAllAssets(),
            "Get All Assets  should throw RuntimeException when httpClient throws IOException");
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetAllAssetsThrowsRuntimeExceptionWhenClientThrowsInterruptedException() throws Exception {
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(
            new InterruptedException("Interrupted Exception"));
        assertThrows(RuntimeException.class, () -> consumer.getAllAssets(),
            "Get All Assets  should throw RuntimeException when httpClient throws InterruptedException");
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetAssetByAssetIdInvalidCredentials() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(401);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        assertThrows(InvalidCredentialsForAPIException.class, () -> consumer.getAssetById("BTC"),
            "Get All Assets by AssetID should throw InvalidCredentialsForAPIException when status code of the response is 401!");
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetAssetByIdSuccessfully() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[{\"asset_id\":\"BTC\",\"name\":\"Bitcoin\",\"price_usd\":50000, \"type_is_crypto\":1}]");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        CryptoAsset result = consumer.getAssetById("BTC");
        assertNotNull(result, "Result should not be null!");
        assertEquals("BTC", result.assetId(), "Result should contain the expected Asset ID");
        assertEquals("Bitcoin", result.name(), "Result should contain the expected asset name!");
        assertEquals(50000, result.priceUSD(), "Result should contain the asset priceUSD!");
        assertNotNull(result.lastUpdated(), "Result last modified field cannot be null!");
    }
    @Test
    void testGetAssetByIdThrowsRuntimeExceptionWhenAssetIdIsNotValid() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        assertThrows(RuntimeException.class, () -> consumer.getAssetById("BTC"),
            "Get Asset by AssetID should throw RuntimeException when response is empty!");
    }

    @Test
    void testGetAssetByAssetIdThrowsRuntimeExceptionWhenClientThrowsIOException() throws Exception {
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(
            new IOException("IO Exception"));
        assertThrows(RuntimeException.class, () -> consumer.getAssetById("BTC"),
            "Get Asset by AssetID should throw RuntimeException when httpClient throws IOException");
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetAssetByAssetIDThrowsRuntimeExceptionWhenClientThrowsInterruptedException() throws Exception {
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(
            new InterruptedException("Interrupted Exception"));
        assertThrows(RuntimeException.class, () -> consumer.getAssetById("BTC"),
            "Get Asset By AssetID should throw RuntimeException when httpClient throws InterruptedException");
        verify(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}


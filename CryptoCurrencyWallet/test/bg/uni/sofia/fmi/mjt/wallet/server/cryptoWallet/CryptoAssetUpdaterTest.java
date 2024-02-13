package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.CryptoConsumerAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets.CryptoAsset;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidCredentialsForAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CryptoAssetUpdaterTest {
    @Mock
    private CryptoConsumerAPI consumerAPI;

    @InjectMocks
    private CryptoAssetUpdater updater;

    @Test
    void testUpdateAllAssetsIfNeededWhenMapIsEmpty() throws InvalidCredentialsForAPIException {
        Map<String, CryptoAsset> assetMap = new HashMap<>();
        List<CryptoAsset> assetList = Arrays.asList(
            new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now()),
            new CryptoAsset("ETH",  "Ethereum", 3000.0, LocalDateTime.now())
        );
        when(consumerAPI.getAllAssets()).thenReturn(assetList);
        updater.setInterval(30);
        Map<String, CryptoAsset> result = updater.updateAllAssetsIfNeeded(assetMap);

        assertEquals(assetList.size(), result.size(), "Result map and expected map should have same sizes!");
        for (CryptoAsset asset : assetList) {
            assertTrue(result.containsKey(asset.assetId()), "Result should contain " + asset.assetId() + " as a key!");
            assertEquals(asset, result.get(asset.assetId()), "Result should contain " + asset.assetId() + "as an asset!");
        }
        verify(consumerAPI, times(1)).getAllAssets();
    }

    @Test
    void testUpdateAllAssetsIfNeededWhenMapIsNotEmptyAndNeedsUpdate() throws InvalidCredentialsForAPIException {
        Map<String, CryptoAsset> assetMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        CryptoAsset earliestAsset = new CryptoAsset("BTC","Bitcoin",50000.0,  now.minus(Duration.ofMinutes(40)));
        assetMap.put(earliestAsset.assetId(), earliestAsset);

        List<CryptoAsset> newAssetList = Arrays.asList(
            new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now()),
            new CryptoAsset("ETH",  "Ethereum", 3000.0, LocalDateTime.now())
        );

        when(consumerAPI.getAllAssets()).thenReturn(newAssetList);
        updater.setInterval(30);
        Map<String, CryptoAsset> result = updater.updateAllAssetsIfNeeded(assetMap);

        assertEquals(newAssetList.size(), result.size(), "Result map and expected map should have same sizes!");
        for (CryptoAsset asset : newAssetList) {
            assertTrue(result.containsKey(asset.assetId()),"Result should contain " + asset.assetId() + " as a key!");
            assertEquals(asset, result.get(asset.assetId()), "Result should contain " + asset.assetId() + "as an asset!");
        }

        verify(consumerAPI, times(1)).getAllAssets();
    }

    @Test
    void testUpdateAllAssetsIfNeededWhenMapIsNotEmptyAndDoesNotNeedUpdate() throws InvalidCredentialsForAPIException {
        Map<String, CryptoAsset> assetMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        CryptoAsset earliestAsset = new CryptoAsset("BTC", "Bitcoin", 50000.0, now.minus(Duration.ofMinutes(20)));
        assetMap.put(earliestAsset.assetId(), earliestAsset);
        updater.setInterval(30);
        Map<String, CryptoAsset> result = updater.updateAllAssetsIfNeeded(assetMap);

        assertEquals(assetMap.size(), result.size(), "Result map and expected map should have same sizes!");
        assertEquals(earliestAsset, result.get(earliestAsset.assetId()), "Result should contain " + earliestAsset.assetId() + "as an asset!");

        verify(consumerAPI, times(0)).getAllAssets();
    }

    @Test
    void testUpdateAssetIfNotNeeded() throws InvalidCredentialsForAPIException {
        String assetId = "BTC";
        CryptoAsset existingAsset = new CryptoAsset(assetId, "Bitcoin", 50000.0, LocalDateTime.now().minusMinutes(15));
        Map<String, CryptoAsset> assetMap = new HashMap<>();
        assetMap.put(assetId, existingAsset);

        Map<String, CryptoAsset> updatedAssets = updater.updateAssetIfNeeded(assetMap, assetId);

        assertEquals(assetMap, updatedAssets, "Maps should be the same!");
        verify(consumerAPI, times(0)).getAssetById(assetId);
    }

    @Test
    void testUpdateAssetIfNeededNeeded() throws InvalidCredentialsForAPIException {
        String assetId = "BTC";
        CryptoAsset existingAsset = new CryptoAsset(assetId, "Bitcoin", 50000.0, LocalDateTime.now().minusMinutes(45));
        Map<String, CryptoAsset> assetMap = new HashMap<>();
        assetMap.put(assetId, existingAsset);

        CryptoAsset newAsset = new CryptoAsset(assetId, "Bitcoin", 50000.0, LocalDateTime.now());
        when(consumerAPI.getAssetById(assetId)).thenReturn(newAsset);

        Map<String, CryptoAsset> updatedAssets = updater.updateAssetIfNeeded(assetMap, assetId);

        assertEquals(assetMap, updatedAssets, "Maps should be the same!");
        assertEquals(newAsset, updatedAssets.get(assetId), newAsset.assetId() + " should be in the map!");
        verify(consumerAPI, times(1)).getAssetById(assetId);
    }

    @Test
    void testUpdateAssetIfNeededException() throws InvalidCredentialsForAPIException {
        String assetId = "BTC";
        CryptoAsset existingAsset = new CryptoAsset(assetId, "Bitcoin", 50000.0, LocalDateTime.now().minusMinutes(45));
        Map<String, CryptoAsset> assetMap = new HashMap<>();
        assetMap.put(assetId, existingAsset);

        when(consumerAPI.getAssetById(assetId)).thenThrow(new InvalidCredentialsForAPIException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> updater.updateAssetIfNeeded(assetMap, assetId),
            "UpdateAssetIfNeeded should throw Runtime Exception when the consumerAPI throws InvalidCredentialsForAPIException!");
        verify(consumerAPI, times(1)).getAssetById(assetId);
    }

    @Test
    void testSetIntervalThrowsRuntimeExceptionWhenTryingToSetNegativeInterval(){
        assertThrows(RuntimeException.class, () -> updater.setInterval(-2),
            "Set Interval should throw RuntimeException when trying to set negative interval of minutes!");
    }

}

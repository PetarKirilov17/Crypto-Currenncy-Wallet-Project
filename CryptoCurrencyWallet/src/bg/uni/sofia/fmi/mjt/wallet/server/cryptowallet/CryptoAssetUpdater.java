package bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.apiconsumer.CryptoConsumerAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.apiconsumer.assets.CryptoAsset;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidCredentialsForAPIException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CryptoAssetUpdater {
    private static final int UPDATE_INTERVAL_IN_MINS = 30;
    private static final String RES_DIRECTORY = "res";
    private static final String ASSET_IDS_FILE_PATH = "assetIds.txt";
    private Path assetIdsPath = Path.of(RES_DIRECTORY, ASSET_IDS_FILE_PATH).toAbsolutePath();
    private CryptoConsumerAPI cryptoConsumer;
    private int intervalInMins;

    public CryptoAssetUpdater(CryptoConsumerAPI cryptoConsumer) {
        this.cryptoConsumer = cryptoConsumer;
        this.intervalInMins = UPDATE_INTERVAL_IN_MINS;
    }

    public Map<String, CryptoAsset> updateAllAssetsIfNeeded(Map<String, CryptoAsset> assetMap) {
        if (assetMap.isEmpty()) {
            List<CryptoAsset> assetList = null;
            try {
                assetList = this.cryptoConsumer.getAllAssets();
            } catch (InvalidCredentialsForAPIException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            for (var a : assetList) {
                assetMap.put(a.assetId(), a);
            }
            writeCryptoAssetsIdsToFile(assetList.stream().map(CryptoAsset::assetId).toList());
            return assetMap;
        }
        var assetList = assetMap.values().stream().toList();
        var earliest = Collections.min(assetList, Comparator.comparing(CryptoAsset::lastUpdated));
        var currTime = LocalDateTime.now();
        Duration duration = Duration.between(earliest.lastUpdated(), currTime);
        if (duration.toMinutes() > this.intervalInMins) {
            update(assetList, assetMap);
        }
        return assetMap;
    }

    private void update(List<CryptoAsset> assetList, Map<String, CryptoAsset> assetMap) {
        try {
            assetList = cryptoConsumer.getAllAssets();
        } catch (InvalidCredentialsForAPIException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        assetMap.clear();
        for (var a : assetList) {
            assetMap.put(a.assetId(), a);
        }
    }

    public Map<String, CryptoAsset> updateAssetIfNeeded(Map<String, CryptoAsset> assetMap, String assetId) {
        var asset = assetMap.get(assetId);
        Duration duration = Duration.between(asset.lastUpdated(), LocalDateTime.now());
        if (duration.toMinutes() > this.intervalInMins) {
            try {
                asset = cryptoConsumer.getAssetById(asset.assetId());
            } catch (InvalidCredentialsForAPIException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            assetMap.put(assetId, asset);
        }
        return assetMap;
    }

    public void setInterval(int intervalInMins) {
        if (intervalInMins < 0) {
            throw new RuntimeException("Interval cannot be a negative number!");
        }
        this.intervalInMins = intervalInMins;
    }

    private void writeCryptoAssetsIdsToFile(List<String> assetIds) {
        if (Files.exists(assetIdsPath)) {
            return;
        }
        try {
            Files.createFile(assetIdsPath);

        } catch (IOException e) {
            throw new UncheckedIOException("Something went wrong while creating a file", e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(assetIdsPath, StandardOpenOption.CREATE)) {
            for (String assetId : assetIds) {
                writer.write(assetId);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Something went wrong while writing in the file", e);
        }
    }
}

package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.CryptoConsumerAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets.CryptoAsset;
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
    private static final String RES_DIRECTORY = "res";
    private static final String ASSET_IDS_FILE_PATH = "assetIds.txt";
    private Path assetIdsPath = Path.of(RES_DIRECTORY, ASSET_IDS_FILE_PATH).toAbsolutePath();
    private final static int UPDATE_INTERVAL_IN_MINS = 30;
    private CryptoConsumerAPI cryptoConsumer;

    public CryptoAssetUpdater(CryptoConsumerAPI cryptoConsumer){
        this.cryptoConsumer = cryptoConsumer;
    }

    public void updateAllAssetsIfNeeded(Map<String, CryptoAsset> assetMap){
        if(assetMap.isEmpty()){
            List<CryptoAsset> assetList = null;
            try {
                assetList = this.cryptoConsumer.getAllAssets();
            } catch (InvalidCredentialsForAPIException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            for (var a : assetList){
                assetMap.put(a.assetId(), a);
            }
            writeCryptoAssetsIdsToFile(assetList.stream().map(CryptoAsset::assetId).toList());
            return;
        }
        var assetList = assetMap.values().stream().toList();
        var earliest = Collections.min(assetList, Comparator.comparing(CryptoAsset::lastUpdated));
        var currTime = LocalDateTime.now();
        Duration duration = Duration.between(earliest.lastUpdated(), currTime);
        if(duration.toMinutes() > UPDATE_INTERVAL_IN_MINS){
            try {
                assetList = cryptoConsumer.getAllAssets();
            } catch (InvalidCredentialsForAPIException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            assetMap.clear();
            for (var a : assetList){
                assetMap.put(a.assetId(), a);
            }
        }
    }

    public void updateAssetIfNeeded(Map<String, CryptoAsset> assetMap, String assetId){
        var asset = assetMap.get(assetId);
        Duration duration = Duration.between(asset.lastUpdated(),LocalDateTime.now());
        if(duration.toMinutes() > UPDATE_INTERVAL_IN_MINS){
            try {
                asset = cryptoConsumer.getAssetById(asset.assetId());
            } catch (InvalidCredentialsForAPIException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            assetMap.put(assetId, asset);
        }
    }

    private void writeCryptoAssetsIdsToFile(List<String> assetIds){
        if(Files.exists(assetIdsPath)){
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

package src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet;

import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.CryptoConsumerAPI;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets.CryptoAsset;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CryptoAssetUpdater {
    private final static int UPDATE_INTERVAL_IN_MINS = 30;
    private CryptoConsumerAPI cryptoConsumer;

    public CryptoAssetUpdater(CryptoConsumerAPI cryptoConsumer){
        this.cryptoConsumer = cryptoConsumer;
    }

    public void updateAllAssetsIfNeeded(Map<String, CryptoAsset> assetMap){
        if(assetMap.isEmpty()){
            var assetList = this.cryptoConsumer.getAllAssets();
            for (var a : assetList){
                assetMap.put(a.assetId(), a);
            }
            return;
        }
        var assetList = assetMap.values().stream().toList();
        var earliest = Collections.min(assetList, Comparator.comparing(CryptoAsset::lastUpdated));
        var currTime = LocalDateTime.now();
        Duration duration = Duration.between(earliest.lastUpdated(), currTime);
        if(duration.toMinutes() > UPDATE_INTERVAL_IN_MINS){
            assetList = cryptoConsumer.getAllAssets();
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
            asset = cryptoConsumer.getAssetById(asset.assetId());
            assetMap.put(assetId, asset);
        }
    }
}

package src.bg.uni.sofia.fmi.mjt.wallet.client;

import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.CryptoConsumerAPI;
import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.SyncCryptoConsumer;
import src.bg.uni.sofia.fmi.mjt.wallet.database.Database;
import src.bg.uni.sofia.fmi.mjt.wallet.database.FileDatabase;
import src.bg.uni.sofia.fmi.mjt.wallet.database.user.Purchase;
import src.bg.uni.sofia.fmi.mjt.wallet.database.user.User;

import javax.xml.crypto.Data;
import java.net.http.HttpClient;

public class CryptoWalletClient {
    public static void main(String[] args){
//        HttpClient client = HttpClient.newBuilder().build();
//        CryptoConsumerAPI consumerAPI = new SyncCryptoConsumer(client);
//        var result = consumerAPI.getAllAssets();
//        System.out.println(result.stream().limit(150).toList());

        Database database = new FileDatabase();
        User user1 = new User("stoyan", "stoyan123");
        user1.increaseBalance(10);
        Purchase purchase1 = new Purchase("BTC", 3);
        Purchase purchase2 = new Purchase("ETH", 2);

        user1.addPurchase(purchase1);
        user1.addPurchase(purchase2);
        database.addUser(user1);

        User user2 = new User("petar", "petar123");
        user2.increaseBalance(20);
        user2.decreaseBalance(6);
        Purchase purchase3 = new Purchase("BTC", 4);
        Purchase purchase4 = new Purchase("DOGE", 5);
        user2.addPurchase(purchase3);
        user2.addPurchase(purchase4);
        database.addUser(user2);
        var res = database.getUsers();
        for (var it : res.values()){
            System.out.println(it.getUsername() + " " + it.getBalance());
            System.out.println(it.getPurchases());
        }
    }
}

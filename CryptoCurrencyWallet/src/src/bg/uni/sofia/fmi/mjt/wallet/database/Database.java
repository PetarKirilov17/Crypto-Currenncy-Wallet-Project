package src.bg.uni.sofia.fmi.mjt.wallet.database;

import src.bg.uni.sofia.fmi.mjt.wallet.database.user.Purchase;
import src.bg.uni.sofia.fmi.mjt.wallet.database.user.User;

import java.util.Map;

public interface Database {
    Map<String, User> getUsers();

    void addUser(User user);

    void updateUser(User user);

    void addPurchaseToUser(User user, Purchase purchase);

    void removePurchaseFromUser(User user, Purchase purchase);

    boolean checkIfUserExists(String username);

    User getUserByUsername(String username);
}

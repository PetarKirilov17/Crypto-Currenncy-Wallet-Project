package bg.uni.sofia.fmi.mjt.wallet.server.database;

import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.Purchase;

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

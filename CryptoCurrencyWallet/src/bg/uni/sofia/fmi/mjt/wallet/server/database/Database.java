package bg.uni.sofia.fmi.mjt.wallet.server.database;

import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;

import java.util.Map;

public interface Database {
    Map<String, User> getUsers();

    void addUser(User user);

    void updateUser(User user);

    boolean checkIfUserExists(String username);

    User getUserByUsername(String username);
}

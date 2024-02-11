package src.bg.uni.sofia.fmi.mjt.wallet.database;

import src.bg.uni.sofia.fmi.mjt.wallet.database.user.Purchase;
import src.bg.uni.sofia.fmi.mjt.wallet.database.user.User;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FileDatabase implements Database {
    private static final String USERS_FILE_PATH =
        "D:\\University\\Sem3\\MJT-Course\\CryptoCurrencyProject\\CryptoCurrencyWallet\\src\\src\\bg\\uni\\sofia\\fmi\\mjt\\wallet\\database\\users.txt";
    private Path usersPath = Path.of(USERS_FILE_PATH);
    private Map<String, User> users;
    public FileDatabase() {
        users = loadUsersFromFile();
    }

    @Override
    public void addUser(User user) {
        users.put(user.getUsername(), user);
        writeUsersToFile();
    }

    @Override
    public void updateUser(User user) {
        users.put(user.getUsername(), user);
        writeUsersToFile();
    }

    @Override
    public void addPurchaseToUser(User user, Purchase purchase) {

    }

    @Override
    public void removePurchaseFromUser(User user, Purchase purchase) {
    }

    @Override
    public boolean checkIfUserExists(String username) {
        return users.get(username) != null;
    }

    @Override
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    @Override
    public Map<String,User> getUsers(){
        return Collections.unmodifiableMap(this.users);
    }

    private Map<String, User> loadUsersFromFile() {
        if (!Files.exists(usersPath)) {
            return new HashMap<>();
        }
        Map<String, User> users = new HashMap<>();
        try (ObjectInputStream userInputStream = new ObjectInputStream(new FileInputStream(USERS_FILE_PATH))) {
            while (true) {
                try {
                    User user = (User) userInputStream.readObject();
                    users.put(user.getUsername(), user);
                } catch (EOFException e) {
                    // End of file reached
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error while loading data from files", e);
        }
        return users;
    }

    private void writeUsersToFile() {
        if (!Files.exists(usersPath)) {
            try {
                Files.createFile(usersPath);
            } catch (IOException e) {
                throw new UncheckedIOException("Something went wrong while creating a file", e);
            }
        }
        try (ObjectOutputStream userOutputStream = new ObjectOutputStream(new FileOutputStream(USERS_FILE_PATH))) {
            for (User user : users.values().stream().toList()) {
                userOutputStream.writeObject(user);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while saving the users into file!", e);
        }
    }
}

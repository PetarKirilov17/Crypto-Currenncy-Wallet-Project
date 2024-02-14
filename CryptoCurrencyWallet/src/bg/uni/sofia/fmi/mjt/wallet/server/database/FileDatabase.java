package bg.uni.sofia.fmi.mjt.wallet.server.database;

import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;

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

    private final Path usersPath;
    private Map<String, User> users;

    public FileDatabase(Path usersPath) {
        this.usersPath = usersPath;
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
    public boolean checkIfUserExists(String username) {
        return users.get(username) != null;
    }

    @Override
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    @Override
    public Map<String, User> getUsers() {
        return Collections.unmodifiableMap(this.users);
    }

    private Map<String, User> loadUsersFromFile() {
        if (!Files.exists(usersPath)) {
            return new HashMap<>();
        }
        Map<String, User> users = new HashMap<>();
        try (ObjectInputStream userInputStream = new ObjectInputStream(new FileInputStream(usersPath.toFile()))) {
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
        try (ObjectOutputStream userOutputStream = new ObjectOutputStream(new FileOutputStream(usersPath.toFile()))) {
            for (User user : users.values().stream().toList()) {
                userOutputStream.writeObject(user);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while saving the users into file!", e);
        }
    }
}

package bg.uni.sofia.fmi.mjt.wallet.server.database;

import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileDatabaseTest {
    private static final String TEST_FILE_PATH = "test_users.txt";
    private static final String RES_DIRECTORY = "res";
    private static final Path testFilePath = Path.of(RES_DIRECTORY, TEST_FILE_PATH).toAbsolutePath();
    static Database fileDatabase;

    @BeforeAll
    static void setUpBefore() {
        fileDatabase = new FileDatabase(testFilePath);
    }

    @AfterAll
    static void tearDown() {
        try {
            Files.deleteIfExists(testFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddUser() {
        User user = new User("testUser", "password");
        fileDatabase.addUser(user);
        assertEquals(user, fileDatabase.getUserByUsername("testUser"), "File Database should contain the user!");
    }

    @Test
    void testUpdateUser() {
        User initialUser = new User("testUser", "password");
        fileDatabase.addUser(initialUser);

        User updatedUser = new User("testUser", "newPassword");
        fileDatabase.updateUser(updatedUser);

        assertEquals(updatedUser, fileDatabase.getUserByUsername("testUser"),
            "File Database should contain the updated user!");
    }

    @Test
    void testGetUsers() {
        User user1 = new User("user1", "password1");
        User user2 = new User("user2", "password2");

        fileDatabase.addUser(user1);
        fileDatabase.addUser(user2);

        Map<String, User> users = fileDatabase.getUsers();

        // Check if the map contains the expected users
        assertEquals(user1, users.get("user1"), "Users map should contain the first user!");
        assertEquals(user2, users.get("user2"), "Users map should contain the second user!");

        // Check immutability by attempting to modify the returned map
        assertThrows(UnsupportedOperationException.class, () -> users.put("user3", new User("user3", "password3")),
            "Add User should throw UnsupportedOperationException when trying to modify the map!");
    }

    @Test
    void testLoadUsersFromFileWhenFileExists() throws IOException {
        // Create a temporary file
        Path tempFilePath = Files.createTempFile("temp_users", ".txt");

        try {
            saveUsersToFile(tempFilePath, new User("user1", "password1"), new User("user2", "password2"));

            FileDatabase testDatabase = new FileDatabase(tempFilePath);

            assertEquals(2, testDatabase.getUsers().size(), "File Database should contain 2 users!");
            assertTrue(testDatabase.checkIfUserExists("user1"), "File Database should contain the first user!");
            assertTrue(testDatabase.checkIfUserExists("user2"), "File Database should contain the second user!");
        } finally {
            Files.deleteIfExists(tempFilePath);
        }
    }

    private void saveUsersToFile(Path filePath, User... users) throws IOException {
        try (ObjectOutputStream userOutputStream = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            for (User user : users) {
                userOutputStream.writeObject(user);
            }
        }
    }
}

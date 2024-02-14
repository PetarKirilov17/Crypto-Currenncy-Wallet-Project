package bg.uni.sofia.fmi.mjt.wallet.client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionInfoTest {
    private SessionInfo sessionInfo;

    @BeforeEach
    void setUp() {
        sessionInfo = new SessionInfo();
    }

    @Test
    void testIsLoggedInWhenNotLoggedIn() {
        boolean result = sessionInfo.isLoggedIn();
        assertFalse(result, "Should not be logged in initially");
    }

    @Test
    void testLogInWhenNotLoggedIn() {
        String username = "testUsername";
        sessionInfo.logIn(username);
        assertTrue(sessionInfo.isLoggedIn(), "Should be logged in after logging in");
        assertEquals(username, sessionInfo.getLoggedInUser(), "Logged-in user should match the logged-in username");
    }

    @Test
    void testLogInWhenAlreadyLoggedIn() {
        String initialUser = "user1";
        String newUser = "user2";
        sessionInfo.logIn(initialUser);
        assertThrows(IllegalStateException.class, () -> sessionInfo.logIn(newUser),
            "Log in should throw IllegalStateException when attempting to log in when already logged in");
    }

    @Test
    void testLogOutWhenLoggedIn() {
        String username = "testUsername";
        sessionInfo.logIn(username);
        sessionInfo.logOut();
        assertFalse(sessionInfo.isLoggedIn(), "Should not be logged in after logging out");
        assertNull(sessionInfo.getLoggedInUser(), "Logged-in user should be null after logging out");
    }

    @Test
    void testLogOutWhenNotLoggedIn() {
        assertThrows(IllegalStateException.class, () -> sessionInfo.logOut(),
            "Should throw IllegalStateException when attempting to log out when not logged in");
    }
}

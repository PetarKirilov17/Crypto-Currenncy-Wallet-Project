package src.bg.uni.sofia.fmi.mjt.wallet.client;

public class SessionInfo {
    private static String loggedInUser;

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static String getLoggedInUser() {
        return loggedInUser;
    }

    public static void logIn(String username) {
        if (isLoggedIn()) {
            throw new IllegalStateException("There is already logged in user. Log out first!");
        }

        loggedInUser = username;
    }

    public static void logOut() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Nobody is logged in");
        }

        loggedInUser = null;
    }
}

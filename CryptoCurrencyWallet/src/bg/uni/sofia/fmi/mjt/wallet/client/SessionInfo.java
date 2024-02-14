package bg.uni.sofia.fmi.mjt.wallet.client;

public class SessionInfo {
    private String loggedInUser;

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public void logIn(String username) {
        if (isLoggedIn()) {
            throw new IllegalStateException("There is already a logged-in user. Log out first!");
        }

        loggedInUser = username;
    }

    public void logOut() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Nobody is logged in");
        }

        loggedInUser = null;
    }
}
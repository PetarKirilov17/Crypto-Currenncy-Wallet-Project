package bg.uni.sofia.fmi.mjt.wallet.client.exception;

public class ServerNotFoundException extends Exception {
    public ServerNotFoundException(String message) {
        super(message);
    }

    public ServerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

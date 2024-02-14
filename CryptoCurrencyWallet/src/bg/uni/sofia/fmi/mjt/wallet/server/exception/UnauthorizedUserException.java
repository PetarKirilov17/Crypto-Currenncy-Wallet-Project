package bg.uni.sofia.fmi.mjt.wallet.server.exception;

public class UnauthorizedUserException extends Exception {

    public UnauthorizedUserException(String message) {
        super(message);
    }

    public UnauthorizedUserException(String message, Throwable cause) {
        super(message, cause);
    }
}

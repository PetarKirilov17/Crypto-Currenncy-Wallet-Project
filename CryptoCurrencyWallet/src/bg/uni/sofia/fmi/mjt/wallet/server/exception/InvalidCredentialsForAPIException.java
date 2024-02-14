package bg.uni.sofia.fmi.mjt.wallet.server.exception;

public class InvalidCredentialsForAPIException extends Exception {

    public InvalidCredentialsForAPIException(String message) {
        super(message);
    }

    public InvalidCredentialsForAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}

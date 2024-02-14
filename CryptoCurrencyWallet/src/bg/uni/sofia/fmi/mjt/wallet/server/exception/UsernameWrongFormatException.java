package bg.uni.sofia.fmi.mjt.wallet.server.exception;

public class UsernameWrongFormatException extends Exception {
    public UsernameWrongFormatException(String message) {
        super(message);
    }

    public UsernameWrongFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

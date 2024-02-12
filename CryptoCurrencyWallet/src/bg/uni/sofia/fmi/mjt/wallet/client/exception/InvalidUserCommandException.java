package bg.uni.sofia.fmi.mjt.wallet.client.exception;

public class InvalidUserCommandException extends Exception{

    public InvalidUserCommandException(String message) {
        super(message);
    }

    public InvalidUserCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}

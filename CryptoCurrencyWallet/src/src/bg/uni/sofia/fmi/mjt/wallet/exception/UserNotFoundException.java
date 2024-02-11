package src.bg.uni.sofia.fmi.mjt.wallet.exception;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

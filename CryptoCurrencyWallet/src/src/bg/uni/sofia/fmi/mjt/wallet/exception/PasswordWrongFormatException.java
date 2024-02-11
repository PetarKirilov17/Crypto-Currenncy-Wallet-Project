package src.bg.uni.sofia.fmi.mjt.wallet.exception;

public class PasswordWrongFormatException extends Exception{
    public PasswordWrongFormatException(String message) {
        super(message);
    }

    public PasswordWrongFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

package src.bg.uni.sofia.fmi.mjt.wallet.exception;

public class InvalidAssetIdException extends Exception{
    public InvalidAssetIdException(String message) {
        super(message);
    }

    public InvalidAssetIdException(String message, Throwable cause) {
        super(message, cause);
    }
}

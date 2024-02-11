package src.bg.uni.sofia.fmi.mjt.wallet.exception;

public class LoginAuthenticationException extends Exception{

    public LoginAuthenticationException(String message) {
        super(message);
    }

    public LoginAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package src.bg.uni.sofia.fmi.mjt.wallet.validation;

import java.util.regex.Pattern;

public class StringValidator {
    private static final String REGEX_ONLY_LETTERS = "[a-zA-Z]+";
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 30;

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 30;

    private static final String REGEX_USERNAME = "^[A-Za-z][A-Za-z0-9_]{" + (MIN_USERNAME_LENGTH - 1) + "," + (
        MAX_USERNAME_LENGTH - 1) + "}$";


    public static boolean isValidUsername(String username) {
        return Pattern.matches(REGEX_USERNAME, username);
    }

    public static boolean isValidPassword(String password) {
        return (password.length() <= MAX_PASSWORD_LENGTH && password.length() >= MIN_PASSWORD_LENGTH);
    }
}
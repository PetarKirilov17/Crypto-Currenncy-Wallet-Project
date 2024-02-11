package src.bg.uni.sofia.fmi.mjt.wallet.database.password;

public interface PasswordHasherAPI {
    String hashPassword(String password);
}

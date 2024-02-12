package bg.uni.sofia.fmi.mjt.wallet.server.database.password;

public interface PasswordHasherAPI {
    String hashPassword(String password);
}

package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service;

import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.LoginAuthenticationException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.PasswordWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserNotFoundException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UsernameWrongFormatException;

public interface UserServiceAPI {
    User register(String username, String password)
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException;
    User login(String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserNotFoundException,
        LoginAuthenticationException;
}

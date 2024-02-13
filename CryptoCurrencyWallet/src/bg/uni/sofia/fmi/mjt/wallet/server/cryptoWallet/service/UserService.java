package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service;

import bg.uni.sofia.fmi.mjt.wallet.server.database.Database;
import bg.uni.sofia.fmi.mjt.wallet.server.database.password.PasswordHasherAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.LoginAuthenticationException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.PasswordWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserNotFoundException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UsernameWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.validation.StringValidator;

public class UserService implements UserServiceAPI{
    private final Database database;
    private final PasswordHasherAPI passwordHasher;
    public UserService(Database database, PasswordHasherAPI passwordHasher){
        this.database = database;
        this.passwordHasher = passwordHasher;
    }
    @Override
    public User register(String username, String password)
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        User user = buildUser(username, password);
        database.addUser(user);
        return user;
    }

    @Override
    public User login(String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserNotFoundException,
        LoginAuthenticationException {
        validateStrings(username, password);
        if (!database.checkIfUserExists(username)) {
            throw new UserNotFoundException("There is no user with username: " + username);
        }
        User user = database.getUserByUsername(username);
        String hashedPassword = passwordHasher.hashPassword(password);
        if (!user.login(username, hashedPassword)) {
            throw new LoginAuthenticationException("Incorrect username or password!");
        }
        return user;
    }


    private User buildUser(String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserAlreadyExistsException {
        validateStrings(username, password);
        if (database.checkIfUserExists(username)) {
            throw new UserAlreadyExistsException("There is already an user with this username!");
        }
        String hashedPassword = passwordHasher.hashPassword(password);
        return new User(username, hashedPassword);
    }
    private void validateStrings(String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException {
        if (username == null || !StringValidator.isValidUsername(username)) {
            throw new UsernameWrongFormatException("Wrong format of the username");
        }
        if (password == null || !StringValidator.isValidPassword(password)) {
            throw new PasswordWrongFormatException("Wrong format of the password!");
        }
    }
}

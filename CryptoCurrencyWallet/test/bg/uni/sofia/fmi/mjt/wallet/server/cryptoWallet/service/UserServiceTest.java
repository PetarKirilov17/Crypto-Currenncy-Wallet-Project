package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service;

import bg.uni.sofia.fmi.mjt.wallet.server.database.Database;
import bg.uni.sofia.fmi.mjt.wallet.server.database.password.PasswordHasherAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.LoginAuthenticationException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.PasswordWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserNotFoundException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UsernameWrongFormatException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private Database database;

    @Mock
    private PasswordHasherAPI passwordHasher;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterThrowsUsernameWrongFormatExceptionWhenPassingNullForUsername() {
        assertThrows(UsernameWrongFormatException.class, () -> userService.register(null, "testPass"),
            "Register should throw UsernameWrongFormatException when passing null for username!");
    }

    @Test
    void testRegisterThrowsUsernameWrongFormatExceptionWhenPassingIncorrectStringForUsername() {
        assertThrows(UsernameWrongFormatException.class, () -> userService.register("!InvalidUsername", "testPassword"),
            "Register should throw UsernameWrongFormatException when passing invalid string for username!");
    }

    @Test
    void testRegisterThrowsPasswordWrongFormatExceptionWhenPassingNullForPassword() {
        assertThrows(PasswordWrongFormatException.class, () -> userService.register("testUsername", null),
            "Register should throw PasswordWrongFormatException when passing null for password!");
    }

    @Test
    void testRegisterThrowsPasswordWrongFormatExceptionWhenPassingInvalidStringForPassword() {
        assertThrows(PasswordWrongFormatException.class, () -> userService.register("testUsername", "a".repeat(2)),
            "Register should throw PasswordWrongFormatException when passing too short password!");

        assertThrows(PasswordWrongFormatException.class, () -> userService.register("testUsername", "a".repeat(32)),
            "Register should throw PasswordWrongFormatException when passing too long password!");
    }

    @Test
    void testRegisterThrowsUserAlreadyExistsExceptionWhenPassingUsernameThatAlreadyExists() {
        when(database.checkIfUserExists("testUsername")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.register("testUsername", "testPassword"),
            "Register should throw UserAlreadyExistsException when trying to register a user with username that already exists in the database!");
    }

    @Test
    void testRegisterSuccessfully()
        throws UsernameWrongFormatException, UserAlreadyExistsException, PasswordWrongFormatException {
        when(database.checkIfUserExists("testUsername")).thenReturn(false);
        when(passwordHasher.hashPassword("testPassword")).thenReturn("hashedPassword");
        User returnedUser = userService.register("testUsername", "testPassword");
        User expectedUser = new User("testUsername", "hashedPassword");
        assertEquals(expectedUser, returnedUser, "Register should return the new user that was created!");
        verify(database, times(1)).addUser(any(User.class));
    }

    @Test
    void testLoginThrowsUsernameWrongFormatExceptionWhenPassingNullForUsername(){
        assertThrows(UsernameWrongFormatException.class, () -> userService.login(null, "testPass"),
            "Login should throw UsernameWrongFormatException when passing null for username!");
    }

    @Test
    void testLoginThrowsUsernameWrongFormatExceptionWhenPassingIncorrectStringForUsername() {
        assertThrows(UsernameWrongFormatException.class, () -> userService.login("!InvalidUsername", "testPassword"),
            "Login should throw UsernameWrongFormatException when passing invalid string for username!");
    }

    @Test
    void testLoginThrowsPasswordWrongFormatExceptionWhenPassingNullForPassword() {
        assertThrows(PasswordWrongFormatException.class, () -> userService.login("testUsername", null),
            "Login should throw PasswordWrongFormatException when passing null for password!");
    }

    @Test
    void testLoginThrowsPasswordWrongFormatExceptionWhenPassingInvalidStringForPassword() {
        assertThrows(PasswordWrongFormatException.class, () -> userService.login("testUsername", "a".repeat(2)),
            "Login should throw PasswordWrongFormatException when passing too short password!");

        assertThrows(PasswordWrongFormatException.class, () -> userService.login("testUsername", "a".repeat(32)),
            "Login should throw PasswordWrongFormatException when passing too long password!");
    }

    @Test
    void testLoginThrowsUserNotFoundExceptionThereIsNoUserWithThePassedUsername(){
        when(database.checkIfUserExists("testUsername")).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> userService.login("testUsername", "testPassword"),
            "Login should throw UserNotFoundException when trying to log in with username that is not in the database");
    }

    @Test
    void testLoginThrowsLoginAuthenticationExceptionWhenUsernameOrPasswordIsIncorrect(){
        User expectedUser = new User("username", "hashPassword1");
        when(database.checkIfUserExists("username")).thenReturn(true);
        when(database.getUserByUsername("username")).thenReturn(expectedUser);
        when(passwordHasher.hashPassword("testPassword")).thenReturn("hashPassword2");
        assertThrows(LoginAuthenticationException.class, () -> userService.login("username", "testPassword"),
            "Login should throw LoginAuthenticationException when username or password is incorrect!");
    }

    @Test
    void testLoginSuccessfully()
        throws UserNotFoundException, UsernameWrongFormatException, LoginAuthenticationException,
        PasswordWrongFormatException {
        User expectedUser = new User("username", "hashPassword1");
        when(database.checkIfUserExists("username")).thenReturn(true);
        when(database.getUserByUsername("username")).thenReturn(expectedUser);
        when(passwordHasher.hashPassword("testPassword")).thenReturn("hashPassword1");
        assertEquals(expectedUser, userService.login("username", "testPassword"),
            "Login should return the user with the passed username and password!");
    }
}

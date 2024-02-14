package bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.service.UserServiceAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptowallet.service.WalletServiceAPI;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InsufficientBalanceException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidAssetIdException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.LoginAuthenticationException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.PasswordWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UnauthorizedUserException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserNotFoundException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UsernameWrongFormatException;

import java.nio.channels.SelectionKey;

public class CryptoWalletController implements CryptoWalletAPI {

    private final UserServiceAPI userService;
    private final WalletServiceAPI walletService;

    public CryptoWalletController(UserServiceAPI userService, WalletServiceAPI walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @Override
    public void register(SelectionKey key, String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserAlreadyExistsException {
        User registeredUser = userService.register(username, password);
        key.attach(registeredUser);
    }

    @Override
    public void login(SelectionKey key, String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserNotFoundException,
        LoginAuthenticationException {
        var loggedInUser = userService.login(username, password);
        key.attach(loggedInUser);
    }

    @Override
    public void depositMoney(SelectionKey key, double amount) throws UnauthorizedUserException {
        checkAuthorization(key);
        User currentUser = (User) key.attachment();
        walletService.depositMoney(currentUser, amount);
    }

    @Override
    public String listOfferings(SelectionKey key, int pageNumber) throws UnauthorizedUserException {
        checkAuthorization(key);
        return walletService.listOfferings(pageNumber);
    }

    @Override
    public void buyAsset(SelectionKey key, String assetId, double money)
        throws UnauthorizedUserException, InvalidAssetIdException, InsufficientBalanceException {
        checkAuthorization(key);
        User currentUser = (User) key.attachment();
        walletService.buyAsset(currentUser, assetId, money);
    }

    @Override
    public double sellAsset(SelectionKey key, String assetId)
        throws UnauthorizedUserException, InvalidAssetIdException {
        checkAuthorization(key);
        User currentUser = (User) key.attachment();
        return walletService.sellAsset(currentUser, assetId);
    }

    @Override
    public String getWalletSummary(SelectionKey key) throws UnauthorizedUserException {
        checkAuthorization(key);
        User currentUser = (User) key.attachment();
        return walletService.getWalletSummary(currentUser);
    }

    @Override
    public String getWalletOverallSummary(SelectionKey key) throws UnauthorizedUserException {
        checkAuthorization(key);
        User currentUser = (User) key.attachment();
        return walletService.getWalletOverallSummary(currentUser);
    }

    private void checkAuthorization(SelectionKey key) throws UnauthorizedUserException {
        if (key.attachment() == null) {
            throw new UnauthorizedUserException("You need to log in to your account first!");
        }
    }
}

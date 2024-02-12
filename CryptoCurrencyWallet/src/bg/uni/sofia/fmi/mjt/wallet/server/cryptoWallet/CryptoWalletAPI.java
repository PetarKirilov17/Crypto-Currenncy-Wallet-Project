package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet;

import bg.uni.sofia.fmi.mjt.wallet.server.exception.InsufficientBalanceException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidAssetIdException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.LoginAuthenticationException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.PasswordWrongFormatException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UnauthorizedUserException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserAlreadyExistsException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UserNotFoundException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.UsernameWrongFormatException;

import java.nio.channels.SelectionKey;

public interface CryptoWalletAPI {
    void register(SelectionKey key, String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserAlreadyExistsException;

    void login(SelectionKey key, String username, String password)
        throws UsernameWrongFormatException, PasswordWrongFormatException, UserNotFoundException,
        LoginAuthenticationException;

    void depositMoney(SelectionKey key, double amount) throws UnauthorizedUserException;

    String listOfferings(SelectionKey key) throws UnauthorizedUserException;

    void buyAsset(SelectionKey key, String assetId, double money)
        throws UnauthorizedUserException, InvalidAssetIdException, InsufficientBalanceException;

    double sellAsset(SelectionKey key, String assetId) throws UnauthorizedUserException, InvalidAssetIdException;

    String getWalletSummary(SelectionKey key) throws UnauthorizedUserException;

    String getWalletOverallSummary(SelectionKey key) throws UnauthorizedUserException;
}

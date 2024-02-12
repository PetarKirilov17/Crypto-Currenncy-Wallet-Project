package src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet;

import src.bg.uni.sofia.fmi.mjt.wallet.cryptoWallet.apiconsumer.assets.CryptoAsset;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.InsufficientBalanceException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.InvalidAssetIdException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.LoginAuthenticationException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.PasswordWrongFormatException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UnauthorizedUserException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UserAlreadyExistsException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UserNotFoundException;
import src.bg.uni.sofia.fmi.mjt.wallet.exception.UsernameWrongFormatException;

import java.nio.channels.SelectionKey;
import java.util.List;

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

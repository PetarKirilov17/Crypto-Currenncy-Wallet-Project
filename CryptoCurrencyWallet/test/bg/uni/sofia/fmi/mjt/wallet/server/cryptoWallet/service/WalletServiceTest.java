package bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.service;

import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.CryptoAssetUpdater;
import bg.uni.sofia.fmi.mjt.wallet.server.cryptoWallet.apiconsumer.assets.CryptoAsset;
import bg.uni.sofia.fmi.mjt.wallet.server.database.Database;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.Purchase;
import bg.uni.sofia.fmi.mjt.wallet.server.database.user.User;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InsufficientBalanceException;
import bg.uni.sofia.fmi.mjt.wallet.server.exception.InvalidAssetIdException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @Mock
    private Database database;

    @Mock
    private CryptoAssetUpdater cryptoAssetUpdater;
    @InjectMocks
    private WalletService walletService;

    private static Map<String, CryptoAsset> assets;

    @BeforeAll
    static void setUp() {
        assets = new LinkedHashMap<>();
        assets.put("BTC", new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now()));
        assets.put("ETH", new CryptoAsset("ETH", "Ethereum", 3000.0, LocalDateTime.now()));
        assets.put("XRP", new CryptoAsset("XRP", "Ripple", 1.0, LocalDateTime.now()));
        assets.put("DOGE", new CryptoAsset("DOGE", "Doge coin", 1.5, LocalDateTime.now()));
        assets.put("FTC", new CryptoAsset("FTC", "Feathercoin", 0.003, LocalDateTime.now()));
    }

    @Test
    void testDepositMoneyThrowsRuntimeExceptionWhenDepositingNegativeAmountOfMoney() {
        User user = new User("testUsername", "testPass");
        double depositedMoney = -50.0;
        assertThrows(RuntimeException.class, () -> walletService.depositMoney(user, depositedMoney),
            "Deposit money should throw RuntimeException when trying to deposit negative amount of money!");
    }

    @Test
    void testDepositMoneyIncreasesBalanceAndUpdatesUser() {
        User user = new User("testUsername", "testPass");
        double depositedMoney = 50.0;

        walletService.depositMoney(user, depositedMoney);

        assertEquals(depositedMoney, user.getBalance(),
            "User should have the deposited amount of money in their account!");
        verify(database).updateUser(user);
    }

    @Test
    void listOfferingsReturnsExpectedOutput() {
        int pageNumber = 1;
        int assetsToShow = 3;
        walletService.setAssetsToShow(assetsToShow);
        when(cryptoAssetUpdater.updateAllAssetsIfNeeded(anyMap())).thenReturn(assets);
        StringBuilder expectedOutput1 = new StringBuilder();
        expectedOutput1.append("Asset ID: BTC -> Price: ").append(String.format("%.4f", 50000.0)).append("$ per unit!")
            .append(System.lineSeparator());
        expectedOutput1.append("Asset ID: ETH -> Price: ").append(String.format("%.4f", 3000.0)).append("$ per unit!")
            .append(System.lineSeparator());
        expectedOutput1.append("Asset ID: XRP -> Price: ").append(String.format("%.4f", 1.0)).append("$ per unit!")
            .append(System.lineSeparator());

        StringBuilder expectedOutput2 = new StringBuilder();
        expectedOutput2.append("Asset ID: DOGE -> Price: ").append(String.format("%.4f", 1.5)).append("$ per unit!")
            .append(System.lineSeparator());
        expectedOutput2.append("Asset ID: FTC -> Price: ").append(String.format("%.4f", 0.003)).append("$ per unit!")
            .append(System.lineSeparator());

        String result1 = walletService.listOfferings(pageNumber);
        pageNumber = 2;
        String result2 = walletService.listOfferings(pageNumber);

        assertEquals(expectedOutput1.toString(), result1,
            "List-offerings should retrieve the first page of the assets!");
        assertEquals(expectedOutput2.toString(), result2,
            "List-offerings should retrieve the second page of the asssets!");
    }

    @Test
    void testAssetsToShowThrowsRuntimeExceptionWhenTryingToSetNegativeNumber() {
        assertThrows(RuntimeException.class, () -> walletService.setAssetsToShow(-2),
            "SetAssetsToShow should throw RuntimeException when trying to insert negative number!");
    }

    @Test
    void testBuyAssetThrowsInsufficientBalanceExceptionWhenTryingToBuyAssetForMoneyThatAreNotInTheUserBalance() {
        User user = new User("testUsername", "testPassword");
        user.increaseBalance(10.0);
        CryptoAsset mockAsset = new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now());
        when(cryptoAssetUpdater.updateAssetIfNeeded(any(), any())).thenReturn(Map.of("BTC", mockAsset));
        double purchaseAmount = 15.0;
        String assetId = "BTC";
        assertThrows(InsufficientBalanceException.class, () -> walletService.buyAsset(user, assetId, purchaseAmount),
            "Buy asset should throw InsufficientBalanceException when trying to buy asset for sum that is not available in the user account!");

        verify(database, never()).updateUser(any());
    }

    @Test
    void testBuyAssetThrowsInvalidAssetIdExceptionWhenPassingInvalidId() {
        User user = new User("testUsername", "testPassword");
        CryptoAsset mockAsset = new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now());
        when(cryptoAssetUpdater.updateAssetIfNeeded(any(), any())).thenReturn(Map.of("BTC", mockAsset));
        double purchaseAmount = 15.0;
        String assetId = "USD";
        assertThrows(InvalidAssetIdException.class, () -> walletService.buyAsset(user, assetId, purchaseAmount),
            "Buy asset should throw InvalidAssetIdException when trying to buy asset that is not available in the assets map!");

        verify(database, never()).updateUser(any());
    }

    @Test
    void testBuyAssetSuccess() throws InsufficientBalanceException, InvalidAssetIdException {
        User user = new User("testUsername", "testPassword");
        user.increaseBalance(100.0);
        CryptoAsset mockAsset = new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now());
        when(cryptoAssetUpdater.updateAssetIfNeeded(any(), any())).thenReturn(Map.of("BTC", mockAsset));

        double purchaseAmount = 60.0;
        String assetId = "BTC";
        walletService.buyAsset(user, assetId, purchaseAmount);
        assertEquals(40.0, user.getBalance(), "User balance should be decreased");
        assertEquals(0.0012, user.getAmountOfAsset("BTC"), "User account should have this amount of asset!");

        verify(database).updateUser(user);
    }

    @Test
    void testSellAssetThrowsInvalidAssetIdExceptionWhenUserDoesNotHaveActivesOfGivenAsset() {
        User user = new User("testUsername", "testPassword");
        String assetId = "BTC";
        assertThrows(InvalidAssetIdException.class, () -> walletService.sellAsset(user, assetId),
            "Sell Asset should throw InvalidAssetIdException when the user does not actives from the passed asset!");

        verify(database, never()).updateUser(any());
    }

    @Test
    void testSellAssetSuccess() throws InvalidAssetIdException {
        User mockUser = mock(User.class);
        when(mockUser.containsAsset(any())).thenReturn(true);
        when(mockUser.getAmountOfAsset(any())).thenReturn(2.0);
        CryptoAsset mockAsset = new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now());
        when(cryptoAssetUpdater.updateAssetIfNeeded(any(), any())).thenReturn(Map.of("BTC", mockAsset));

        String assetId = "BTC";
        double sellingMoney = walletService.sellAsset(mockUser, assetId);
        assertEquals(100000.0, sellingMoney,
            "Sell asset should return the amount of money that is earned from the sale of the asset!");
        verify(mockUser).increaseBalance(sellingMoney);
        verify(mockUser).removePurchase(assetId);

        verify(database).updateUser(mockUser);
    }

    @Test
    void testGetWalletSummary() {
        User mockUser = mock(User.class);
        when(mockUser.getBalance()).thenReturn(100.0);

        Purchase purchase1 = new Purchase("BTC", 2.0, 50000.0);
        Purchase purchase2 = new Purchase("ETH", 3.5, 3000.0);
        when(mockUser.getPurchases()).thenReturn(Set.of(purchase1, purchase2));

        String walletSummary = walletService.getWalletSummary(mockUser);

        String expectedSummary = "Current balance: " + String.format("%.4f", 100.0) + "$" + System.lineSeparator() +
            "Asset ID: BTC Amount: " + String.format("%.4f", 2.0) + System.lineSeparator() +
            "Asset ID: ETH Amount: " + String.format("%.4f", 3.5) + System.lineSeparator();
        assertEquals(expectedSummary, walletSummary,
            "Get-wallet-summary should return the all the purchases of the user!");
    }

    @Test
    void testGetWalletOverallSummary() {
        User mockUser = mock(User.class);
        Purchase purchase1 = new Purchase("BTC", 2.0, 50000.0);
        Purchase purchase2 = new Purchase("ETH", 2.0, 3000.0);
        Purchase purchase3 = new Purchase("DOGE", 2.0, 1000.0);
        when(mockUser.getPurchases()).thenReturn(Set.of(purchase1, purchase2, purchase3));

        CryptoAsset asset1 = new CryptoAsset("BTC", "Bitcoin", 50000.0, LocalDateTime.now());
        CryptoAsset asset2 = new CryptoAsset("ETH", "Ethereum", 3500.0, LocalDateTime.now());
        CryptoAsset asset3 = new CryptoAsset("DOGE", "Dogecoin", 500.0, LocalDateTime.now());
        when(cryptoAssetUpdater.updateAllAssetsIfNeeded(anyMap())).thenReturn(
            Map.of("BTC", asset1, "ETH", asset2, "DOGE", asset3));

        String walletSummary = walletService.getWalletOverallSummary(mockUser);

        String expectedSummary =
            "Asset ID: BTC | Amount: " + String.format("%.4f", 2.0) + " | You do not have profit or loss!" +
                System.lineSeparator() +
                "Asset ID: DOGE | Amount: " + String.format("%.4f", 2.0) + " | DOWN: " + String.format("%.4f", 1000.0) +
                "$" + System.lineSeparator() +
                "Asset ID: ETH | Amount: " + String.format("%.4f", 2.0) + " | UP: " + String.format("%.4f", 1000.0) +
                "$" + System.lineSeparator();

        assertEquals(expectedSummary, walletSummary,
            "Get-wallet-summary should return the all the purchases of the user!");
    }
}

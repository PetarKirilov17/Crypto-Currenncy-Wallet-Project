package src.bg.uni.sofia.fmi.mjt.wallet.database.user;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -1822662178653243146L;
    private String username;
    private String passwordHash;
    private double balance;
    private Set<Purchase> purchases;

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = password;
        balance = 0.0;
        purchases = new HashSet<>();
    }

    public boolean login(String username, String passwordHash) {
        return this.username.equals(username) && this.passwordHash.equals(passwordHash);
    }

    public String getUsername() {
        return username;
    }

    public double getBalance() {
        return this.balance;
    }

    public void increaseBalance(double amount) {
        if (amount < 0) {
            throw new RuntimeException("Amount cannot be negative!");
        }
        this.balance += amount;
    }

    public void decreaseBalance(double amount) {
        if (amount < 0) {
            throw new RuntimeException("Amount cannot be negative!");
        }
        this.balance -= amount;
    }

    public void addPurchase(Purchase purchase){
        purchases.add(purchase);
    }

    public void removePurchase(String assetId){
        for (var it : purchases){
            if(it.assetId().equals(assetId)){
                purchases.remove(it);
                break;
            }
        }
    }

    public boolean containsAsset(String assetId){
        for (var it : purchases){
            if(it.assetId().equals(assetId)){
                return true;
            }
        }
        return false;
    }

    public double getAmountOfAsset(String assetId){
        for(var it : purchases){
            if(it.assetId().equals(assetId)){
                return it.amount();
            }
        }
        return 0.0;
    }

    public double getAvgPrice(String assetId){
        for(var it : purchases){
            if(it.assetId().equals(assetId)){
                return it.avgPrice();
            }
        }
        return 0.0;
    }

    public Set<Purchase> getPurchases(){
        if(purchases == null){
            return new HashSet<>();
        }
        return Collections.unmodifiableSet(purchases);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User user = (User) obj;
        return username.equals(user.username) && passwordHash.equals(user.passwordHash);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (passwordHash != null ? passwordHash.hashCode() : 0);
        return result;
    }
}
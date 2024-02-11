package src.bg.uni.sofia.fmi.mjt.wallet.database.user;

import java.io.Serializable;

public record Purchase(String assetId, double amount) implements Serializable{}

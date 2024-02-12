package bg.uni.sofia.fmi.mjt.wallet.server.database.user;

import java.io.Serializable;

public record Purchase(String assetId, double amount, double avgPrice) implements Serializable{}

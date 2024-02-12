package bg.uni.sofia.fmi.mjt.wallet.client.ui;

public interface UI {
    String read();

    void write(String str);

    void writeError(String error);
}

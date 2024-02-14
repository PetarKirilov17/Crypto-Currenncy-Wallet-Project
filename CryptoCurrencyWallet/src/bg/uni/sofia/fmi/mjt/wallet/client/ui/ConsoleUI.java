package bg.uni.sofia.fmi.mjt.wallet.client.ui;

import java.util.Scanner;

public class ConsoleUI implements UI {
    @Override
    public String read() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    @Override
    public void write(String str) {
        System.out.println(str);
    }

    @Override
    public void writeError(String error) {
        System.err.println(error);
    }
}

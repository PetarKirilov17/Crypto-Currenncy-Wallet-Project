package bg.uni.sofia.fmi.mjt.wallet.client;

import com.google.gson.Gson;
import bg.uni.sofia.fmi.mjt.wallet.client.command.Command;
import bg.uni.sofia.fmi.mjt.wallet.client.exception.ServerNotFoundException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class CryptoWalletClient {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 4096;
    private Gson gson;

    private ByteBuffer buffer;

    private SocketChannel socketChannel;

    public CryptoWalletClient() {
        gson = new Gson();
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public void run() throws ServerNotFoundException {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        } catch (IOException e) {
            throw new ServerNotFoundException("The server is not working at the moment!", e);
        }
    }

    public void stop() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException("The channel failed to close!", e);
        }
    }

    public String sendRequest(Command command) {
        try {
            buffer.clear();
            buffer.put(gson.toJson(command).getBytes());
            buffer.flip(); // switch to reading mode
            socketChannel.write(buffer); // buffer drain

            buffer.clear(); // switch to writing mode
            socketChannel.read(buffer); // buffer fill
            buffer.flip(); // switch to reading mode

            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);

            return new String(byteArray, StandardCharsets.UTF_8); // buffer drain
        } catch (Exception e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}

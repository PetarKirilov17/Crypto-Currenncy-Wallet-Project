package src.bg.uni.sofia.fmi.mjt.wallet.server;

import src.bg.uni.sofia.fmi.mjt.wallet.command.CommandCreator;
import src.bg.uni.sofia.fmi.mjt.wallet.command.CommandExecutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class CryptoWalletServer {
    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private boolean isServerOn;
    private Selector selector;
    private ByteBuffer buffer;
    private final CommandExecutor commandExecutor;

    public CryptoWalletServer(CommandExecutor executor){
        this.commandExecutor = executor;
    }

    public void start(){
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()){
            selector = Selector.open();
            configServerSocketChannel(serverSocketChannel, selector);

            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerOn = true;
            System.out.println("Server started!");
            while(isServerOn){
                int readyChannels = selector.select();
                if(readyChannels == 0){
                    continue;
                }
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeySet.iterator();

                while(keyIterator.hasNext()){
                    SelectionKey key = keyIterator.next();
                    if(key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        String clientInput = getClientInput(sc);
                        System.out.println(clientInput);
                        if(clientInput == null){
                            continue;
                        }
                        String response = commandExecutor.execute(key, CommandCreator.createCommand(clientInput));
                        System.out.println("Response: " + response);
                        sendResponse(sc, response);
                        //TODO: add functionality for disconnecting
                    }else if(key.isAcceptable()){
                        accept(key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with starting the server", e);
        }
    }

    public void stop(){
        this.isServerOn = false;
        System.out.println("The server is stopped");
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private String getClientInput(SocketChannel sc) throws IOException {
        buffer.clear();
        int r = sc.read(buffer);
        if (r < 0) {
            System.out.println("Client has closed the connection!");
            sc.close();
            return null;
        }
        buffer.flip();
        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void sendResponse(SocketChannel sc, String response) throws IOException {
        buffer.clear();
        buffer.put(response.getBytes());
        buffer.flip();
        sc.write(buffer);
    }
}

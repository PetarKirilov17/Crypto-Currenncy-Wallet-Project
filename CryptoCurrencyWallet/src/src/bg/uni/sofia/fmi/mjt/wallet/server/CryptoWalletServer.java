package src.bg.uni.sofia.fmi.mjt.wallet.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class CryptoWalletServer {
    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private boolean isServerOn;
    private Selector selector;

    public void start(){
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()){
            selector = Selector.open();
            configServerSocketChannel(serverSocketChannel, selector);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerOn = true;
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
                        buffer.clear();
                        int r = sc.read(buffer);
                        if (r < 0) {
                            System.out.println("Client has closed the connection!");
                            sc.close();
                            continue;
                        }
                        buffer.flip();
                        //TODO: handle the commands from the client
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
}

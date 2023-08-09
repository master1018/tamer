public class CloseRegisteredChannel {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel server = ServerSocketChannel.open();
        ServerSocket s = server.socket ();
        s.bind (new InetSocketAddress (0));
        int port = s.getLocalPort ();
        SocketChannel client = SocketChannel.open ();
        client.connect (new InetSocketAddress ("127.0.0.1", port));
        SocketChannel slave = server.accept ();
        slave.configureBlocking (true);
        Selector selector = Selector.open ();
        client.configureBlocking (false);
        SelectionKey key = client.register (
            selector, SelectionKey.OP_READ, null
        );
        client.close();
        System.out.println ("Will hang here...");
        int nb = slave.read (ByteBuffer.allocate (1024));
        selector.close();
        server.close();
    }
}

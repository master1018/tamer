public class SelectAfterRead {
    final static int TIMEOUT = 1000;
    public static void main(String[] argv) throws Exception {
        InetAddress lh = InetAddress.getByName(ByteServer.LOCALHOST);
        ByteServer server = new ByteServer(1);
        server.start();
        Selector sel = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(lh, server.port()));
        sc.read(ByteBuffer.allocate(1));
        sc.configureBlocking(false);
        sc.register(sel, SelectionKey.OP_READ);
        if (sel.selectNow() != 0)
            throw new Exception("Select returned nonzero value");
        sc.close();
        sel.close();
        server.exit();
        server = new ByteServer(2);
        server.start();
        sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(lh, server.port()));
        sc.configureBlocking(false);
        sel = Selector.open();
        sc.register(sel, SelectionKey.OP_READ);
        if (sel.select(TIMEOUT) != 1)
            throw new Exception("One selected key expected");
        sel.selectedKeys().clear();
        if (sel.selectNow() != 1)
            throw new Exception("One selected key expected");
        if (sc.read(ByteBuffer.allocate(1)) != 1)
            throw new Exception("One byte expected");
        if (sc.read(ByteBuffer.allocate(1)) != 1)
            throw new Exception("One byte expected");
        if (sel.selectNow() != 0)
            throw new Exception("Select returned nonzero value");
        sc.close();
        sel.close();
        server.exit();
    }
}

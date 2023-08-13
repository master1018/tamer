public class ReadAfterConnect {
    public static void main(String[] argv) throws Exception {
        ByteServer server = new ByteServer(0); 
        server.start();
        InetSocketAddress isa = new InetSocketAddress(
                InetAddress.getByName(ByteServer.LOCALHOST), server.port());
        Selector sel = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.connect(isa);
        sc.configureBlocking(false);
        sc.register(sel, SelectionKey.OP_READ);
        if (sel.selectNow() != 0)
            throw new Exception("Select returned nonzero value");
        sc.close();
        server.exit();
    }
}

public class SelectWrite {
    public static void main(String[] argv) throws Exception {
        ByteServer server = new ByteServer(0);
        server.start();
        InetSocketAddress isa = new InetSocketAddress(
                InetAddress.getByName(ByteServer.LOCALHOST), server.port());
        Selector sel = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.connect(isa);
        sc.configureBlocking(false);
        sc.register(sel, SelectionKey.OP_WRITE);
        sel.select();
        sel.selectedKeys().clear();
        if (sel.select() == 0) {
            throw new Exception("Select returned zero");
        }
        sc.close();
        sel.close();
    }
}

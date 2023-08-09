public class NetworkChannelTests {
    static interface ChannelFactory {
        NetworkChannel open() throws IOException;
    }
    static class BogusSocketAddress extends SocketAddress {
    }
    static void bindTests(ChannelFactory factory) throws IOException {
        NetworkChannel ch;
        ch = factory.open().bind(new InetSocketAddress(0));
        try {
            ch.bind(new InetSocketAddress(0));
            throw new RuntimeException("AlreadyBoundException not thrown");
        } catch (AlreadyBoundException x) {
        }
        ch.close();
        ch = factory.open().bind(null);
        if (ch.getLocalAddress() == null)
            throw new RuntimeException("socket not found");
        ch.close();
        ch = factory.open();
        try {
            ch.bind(new BogusSocketAddress());
            throw new RuntimeException("UnsupportedAddressTypeException not thrown");
        } catch (UnsupportedAddressTypeException x) {
        }
        ch.close();
        try {
            ch.bind(new InetSocketAddress(0));
            throw new RuntimeException("ClosedChannelException not thrown");
        } catch (ClosedChannelException x) {
        }
    }
    static void localAddressTests(ChannelFactory factory) throws IOException {
        NetworkChannel ch;
        ch = factory.open();
        if (ch.getLocalAddress() != null) {
            throw new RuntimeException("Local address returned when not bound");
        }
        InetSocketAddress local =
            (InetSocketAddress)(ch.bind(new InetSocketAddress(0)).getLocalAddress());
        if (!local.getAddress().isAnyLocalAddress()) {
            if (NetworkInterface.getByInetAddress(local.getAddress()) == null)
                throw new RuntimeException("not bound to local address");
        }
        if (local.getPort() <= 0)
            throw new RuntimeException("not bound to local port");
        ch.close();
        try {
            ch.getLocalAddress();
            throw new RuntimeException("ClosedChannelException expected");
        } catch (ClosedChannelException e) { }
    }
    static void connectedAddressTests() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open()
            .bind(new InetSocketAddress(0));
        InetSocketAddress local = (InetSocketAddress)(ssc.getLocalAddress());
        int port = local.getPort();
        InetSocketAddress server = new InetSocketAddress(InetAddress.getLocalHost(), port);
        SocketChannel sc = SocketChannel.open();
        if (sc.getRemoteAddress() != null)
            throw new RuntimeException("getRemoteAddress returned address when not connected");
        sc.connect(server);
        SocketAddress remote = sc.getRemoteAddress();
        if (!remote.equals(server))
            throw new RuntimeException("getRemoteAddress returned incorrect address");
        sc.close();
        try {
            sc.getRemoteAddress();
            throw new RuntimeException("ClosedChannelException expected");
        } catch (ClosedChannelException e) { }
        ssc.close();
    }
    public static void main(String[] args) throws IOException {
        ChannelFactory factory;
        factory = new ChannelFactory() {
            public NetworkChannel open() throws IOException {
                return SocketChannel.open();
            }
        };
        bindTests(factory);
        localAddressTests(factory);
        connectedAddressTests();
        factory = new ChannelFactory() {
            public NetworkChannel open() throws IOException {
                return ServerSocketChannel.open();
            }
        };
        bindTests(factory);
        localAddressTests(factory);
        ServerSocketChannel.open()
            .bind(new InetSocketAddress(0), 100).close();
        ServerSocketChannel.open()
            .bind(new InetSocketAddress(0), 0).close();
        ServerSocketChannel.open()
            .bind(new InetSocketAddress(0), -1).close();
        factory = new ChannelFactory() {
            public NetworkChannel open() throws IOException {
                return DatagramChannel.open();
            }
        };
        bindTests(factory);
        localAddressTests(factory);
    }
}

public class LocalAddress {
    public static void main(String[] args) throws Exception {
        test1();
    }
    static void test1() throws Exception {
        InetAddress bogus = InetAddress.getByName("0.0.0.0");
        InetSocketAddress saddr = new InetSocketAddress(
            InetAddress.getByName(TestUtil.HOST), 23);
        SocketChannel sc = SocketChannel.open();
        try {
            sc.connect(saddr);
            InetAddress ia = sc.socket().getLocalAddress();
            if (ia == null || ia.equals(bogus))
                throw new RuntimeException("test failed");
        } finally {
            sc.close();
        }
        sc = SocketChannel.open();
        try {
            sc.socket().bind(new InetSocketAddress(0));
            if (sc.socket().getLocalPort() == 0)
                throw new RuntimeException("test failed");
            sc.socket().connect(saddr);
            InetAddress ia = sc.socket().getLocalAddress();
            if (ia == null || ia.isAnyLocalAddress())
                throw new RuntimeException("test failed");
        } finally {
            sc.close();
        }
    }
}

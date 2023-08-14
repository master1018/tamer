public class Stream {
    static final int DAYTIME_PORT = 13;
    static final String DAYTIME_HOST = TestUtil.HOST;
    static void test() throws Exception {
        InetSocketAddress isa
            = new InetSocketAddress(InetAddress.getByName(DAYTIME_HOST),
                                    DAYTIME_PORT);
        SocketChannel sc = SocketChannel.open();
        sc.connect(isa);
        sc.configureBlocking(false);
        InputStream is = sc.socket().getInputStream();
        byte b[] = new byte[10];
        try {
            int n = is.read(b);
            throw new RuntimeException("Exception expected; none thrown");
        } catch (IllegalBlockingModeException e) {
        }
        sc.close();
    }
    public static void main(String[] args) throws Exception {
        test();
    }
}

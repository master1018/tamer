public class BufferSize {
    static final int DAYTIME_PORT = 13;
    static final String DAYTIME_HOST = TestUtil.HOST;
    public static void main(String[] args) throws Exception {
        InetSocketAddress isa
            = new InetSocketAddress(InetAddress.getByName(DAYTIME_HOST),
                                    DAYTIME_PORT);
        ServerSocketChannel sc = ServerSocketChannel.open();
        try {
            sc.socket().setReceiveBufferSize(-1);
            throw new Exception("Illegal size accepted");
        } catch (IllegalArgumentException iae) {
        }
        try {
            sc.socket().setReceiveBufferSize(0);
            throw new Exception("Illegal size accepted");
        } catch (IllegalArgumentException iae) {
        }
        sc.close();
    }
}

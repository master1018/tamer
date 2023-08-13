public class ConnectState {
    static PrintStream log = System.err;
    static String REMOTE_HOST = TestUtil.HOST;
    static int REMOTE_PORT = 7;                         
    static InetSocketAddress remote;
    final static int ST_UNCONNECTED = 0;
    final static int ST_PENDING = 1;
    final static int ST_CONNECTED = 2;
    final static int ST_CLOSED = 3;
    static abstract class Test {
        abstract String go(SocketChannel sc) throws Exception;
        static void check(boolean test, String desc) throws Exception {
            if (!test)
                throw new Exception("Incorrect state: " + desc);
        }
        static void check(SocketChannel sc, int state) throws Exception {
            switch (state) {
            case ST_UNCONNECTED:
                check(!sc.isConnected(), "!isConnected");
                check(!sc.isConnectionPending(), "!isConnectionPending");
                check(sc.isOpen(), "isOpen");
                break;
            case ST_PENDING:
                check(!sc.isConnected(), "!isConnected");
                check(sc.isConnectionPending(), "isConnectionPending");
                check(sc.isOpen(), "isOpen");
                break;
            case ST_CONNECTED:
                check(sc.isConnected(), "isConnected");
                check(!sc.isConnectionPending(), "!isConnectionPending");
                check(sc.isOpen(), "isOpen");
                break;
            case ST_CLOSED:
                check(sc.isConnected(), "isConnected");
                check(!sc.isConnectionPending(), "!isConnectionPending");
                check(sc.isOpen(), "isOpen");
                break;
            }
        }
        Test(String name, Class exception, int state) throws Exception {
            SocketChannel sc = SocketChannel.open();
            String note = null;
            try {
                try {
                    note = go(sc);
                } catch (Exception x) {
                    if (exception != null) {
                        if (exception.isInstance(x)) {
                            log.println(name + ": As expected: "
                                        + x);
                            check(sc, state);
                            return;
                        } else {
                            throw new Exception(name
                                                + ": Incorrect exception",
                                                x);
                        }
                    } else {
                        throw new Exception(name
                                            + ": Unexpected exception",
                                            x);
                    }
                }
                if (exception != null)
                    throw new Exception(name
                                        + ": Expected exception not thrown: "
                                        + exception);
                check(sc, state);
                log.println(name + ": Returned normally"
                            + ((note != null) ? ": " + note : ""));
            } finally {
                if (sc.isOpen())
                    sc.close();
            }
        }
    }
    static void tests() throws Exception {
        log.println(remote);
        new Test("Read unconnected", NotYetConnectedException.class,
                 ST_UNCONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    ByteBuffer b = ByteBuffer.allocateDirect(1024);
                    sc.read(b);
                    return null;
                }};
        new Test("Write unconnected", NotYetConnectedException.class,
                 ST_UNCONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    ByteBuffer b = ByteBuffer.allocateDirect(1024);
                    sc.write(b);
                    return null;
                }};
        new Test("Simple connect", null, ST_CONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    sc.connect(remote);
                    return null;
                }};
        new Test("Simple connect & finish", null, ST_CONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    sc.connect(remote);
                    if (!sc.finishConnect())
                        throw new Exception("finishConnect returned false");
                    return null;
                }};
        new Test("Double connect",
                 AlreadyConnectedException.class, ST_CONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    sc.connect(remote);
                    sc.connect(remote);
                    return null;
                }};
        new Test("Finish w/o start",
                 NoConnectionPendingException.class, ST_UNCONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    sc.finishConnect();
                    return null;
                }};
        new Test("NB simple connect", null, ST_CONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    sc.configureBlocking(false);
                    sc.connect(remote);
                    int n = 0;
                    while (!sc.finishConnect()) {
                        Thread.sleep(10);
                        n++;
                    }
                    sc.finishConnect();         
                    return ("Tries to finish = " + n);
                }};
        new Test("NB double connect",
                 ConnectionPendingException.class, ST_PENDING) {
                String go(SocketChannel sc) throws Exception {
                    sc.configureBlocking(false);
                    sc.connect(remote);
                    sc.connect(remote);
                    return null;
                }};
        new Test("NB finish w/o start",
                 NoConnectionPendingException.class, ST_UNCONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    sc.configureBlocking(false);
                    sc.finishConnect();
                    return null;
                }};
        new Test("NB connect, B finish", null, ST_CONNECTED) {
                String go(SocketChannel sc) throws Exception {
                    sc.configureBlocking(false);
                    sc.connect(remote);
                    sc.configureBlocking(true);
                    sc.finishConnect();
                    return null;
                }};
    }
    public static void main(String[] args) throws Exception {
        remote = new InetSocketAddress(InetAddress.getByName(REMOTE_HOST),
                                       REMOTE_PORT);
        tests();
    }
}

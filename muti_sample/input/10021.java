public class UnboundSocketTests {
    static int failures = 0;
    static void check(String msg, Object actual, Object expected) {
        System.out.format("%s expected: %s, actual: %s", msg, expected, actual);
        if (actual == expected) {
            System.out.println(" [PASS]");
        } else {
            System.out.println(" [FAIL]");
            failures++;
        }
    }
    static void checkIsAnyLocalAddress(String msg, InetAddress actual) {
        System.out.format("%s actual: %s", msg, actual);
        if (actual.isAnyLocalAddress()) {
            System.out.println(" [PASS]");
        } else {
            System.out.println(" [FAIL]");
            failures++;
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("\n-- SocketChannel --");
        SocketChannel sc = SocketChannel.open();
        try {
            check("getLocalPort()", sc.socket().getLocalPort(), -1);
            checkIsAnyLocalAddress("getLocalAddress()",
                sc.socket().getLocalAddress());
            check("getLocalSocketAddress()", sc.socket().getLocalSocketAddress(), null);
            check("getPort()", sc.socket().getPort(), 0);
            check("getInetAddress()", sc.socket().getInetAddress(), null);
            check("getRemoteSocketAddress()", sc.socket().getRemoteSocketAddress(), null);
        } finally {
            sc.close();
        }
        System.out.println("\n-- ServerSocketChannel --");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            check("getLocalPort()", ssc.socket().getLocalPort(), -1);
            check("getInetAddress()", ssc.socket().getInetAddress(), null);
            check("getLocalSocketAddress()", ssc.socket().getLocalSocketAddress(), null);
        } finally {
            ssc.close();
        }
        System.out.println("\n-- DatagramChannel --");
        DatagramChannel dc = DatagramChannel.open();
        try {
            check("getLocalPort()", dc.socket().getLocalPort(), 0);
            checkIsAnyLocalAddress("getLocalAddress()",
                dc.socket().getLocalAddress());
            check("getLocalSocketAddress()", dc.socket().getLocalSocketAddress(), null);
            check("getPort()", dc.socket().getPort(), -1);
            check("getInetAddress()", dc.socket().getInetAddress(), null);
            check("getRemoteSocketAddress()", dc.socket().getRemoteSocketAddress(), null);
        } finally {
            dc.close();
        }
        if (failures > 0) {
            throw new RuntimeException(failures + " sub-tests(s) failed.");
        }
    }
}

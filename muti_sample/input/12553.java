public class StateTest {
    private static int failures = 0;
    private static String TEST_SERVICE = "StateTestService";
    private static void waitForTestResult(ServerSocketChannel ssc, boolean expectFail) throws IOException {
        Selector sel = ssc.provider().openSelector();
        SelectionKey sk;
        SocketChannel sc;
        ssc.configureBlocking(false);
        sk = ssc.register(sel, SelectionKey.OP_ACCEPT);
        long to = 15*1000;
        sc = null;
        for (;;) {
            long st = System.currentTimeMillis();
            sel.select(to);
            if (sk.isAcceptable() && ((sc = ssc.accept()) != null)) {
                break;
            }
            sel.selectedKeys().remove(sk);
            to -= System.currentTimeMillis() - st;
            if (to <= 0) {
                throw new IOException("Timed out waiting for service to report test result");
            }
        }
        sk.cancel();
        ssc.configureBlocking(false);
        sc.configureBlocking(false);
        sk = sc.register(sel, SelectionKey.OP_READ);
        to = 5000;
        ByteBuffer bb = ByteBuffer.allocateDirect(20);
        for (;;) {
            long st = System.currentTimeMillis();
            sel.select(to);
            if (sk.isReadable()) {
                int n = sc.read(bb);
                if (n > 0) {
                    break;
                }
                if (n < 0) {
                    throw new IOException("Premature EOF - no test result from service");
                }
            }
            sel.selectedKeys().remove(sk);
            to -= System.currentTimeMillis() - st;
            if (to <= 0) {
                throw new IOException("Timed out waiting for service to report test result");
            }
        }
        sk.cancel();
        sc.close();
        sel.close();
        bb.flip();
        byte b = bb.get();
        if (expectFail && b == 'P') {
            System.err.println("Test passed - test is expected to fail!!!");
            failures++;
        }
        if (!expectFail && b != 'P') {
            System.err.println("Test failed!");
            failures++;
        }
    }
    public static void main(String args[]) throws IOException {
        boolean expectFail = false;
        String options[] = args;
        if (args.length > 0 && args[0].equals("-expectFail")) {
            expectFail = true;
            options = new String[args.length-1];
            if (args.length > 1) {
                System.arraycopy(args, 1, options, 0, args.length-1);
            }
        }
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(0));
        int port = ssc.socket().getLocalPort();
        String arg[] = new String[1];
        arg[0] = String.valueOf(port);
        SocketChannel sc = Launcher.launchWithSocketChannel(TEST_SERVICE, options, arg);
        waitForTestResult(ssc, expectFail);
        sc.close();
        sc = Launcher.launchWithServerSocketChannel(TEST_SERVICE, options, arg);
        waitForTestResult(ssc, expectFail);
        sc.close();
        DatagramChannel dc = Launcher.launchWithDatagramChannel(TEST_SERVICE, options, arg);
        waitForTestResult(ssc, expectFail);
        dc.close();
        if (failures > 0) {
            throw new RuntimeException("Test failed - see log for details");
        } else {
            System.out.println("All tests passed.");
        }
    }
}

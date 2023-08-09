public class SocketTest extends TestCase {
    private static final String NON_EXISTING_ADDRESS = "123.123.123.123";
    private static final String KNOW_GOOD_ADDRESS = "209.85.129.147";
    private static final String PACKAGE_DROPPING_ADDRESS = "191.167.0.1";
    @SmallTest
    public void testSocketSimple() throws Exception {
        ServerSocket ss;
        Socket s, s1;
        int port;
        IOException lastEx = null;
        ss = new ServerSocket();
        for (port = 9900; port < 9999; port++) {
            try {
                ss.bind(new InetSocketAddress("127.0.0.1", port));
                lastEx = null;
                break;
            } catch (IOException ex) {
                lastEx = ex;
            }
        }
        if (lastEx != null) {
            throw lastEx;
        }
        s = new Socket("127.0.0.1", port);
        s1 = ss.accept();
        s.getOutputStream().write(0xa5);
        assertEquals(0xa5, s1.getInputStream().read());
        s1.getOutputStream().write(0x5a);
        assertEquals(0x5a, s.getInputStream().read());
    }
    @SmallTest
    public void testWildcardAddress() throws Exception {
        Socket s2 = new Socket();
        s2.bind(new InetSocketAddress((InetAddress) null, 12345));
        byte[] addr = s2.getLocalAddress().getAddress();
        for (int i = 0; i < 4; i++) {
            assertEquals("Not the wildcard address", 0, addr[i]);
        }
    }
    @SmallTest
    public void testServerSocketClose() throws Exception {
        ServerSocket s3 = new ServerSocket(23456);
        s3.close();
        ServerSocket s4 = new ServerSocket(23456);
        s4.close();
    }
    private Exception serverError = null;
    @LargeTest
    public void testSetReuseAddress() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(8383);
        final ServerSocket serverSock = new ServerSocket();
        serverSock.setReuseAddress(true);
        serverSock.bind(addr);
        final Semaphore semThreadEnd = new Semaphore(0);
        new Thread() {
            @Override
            public void run() {
                try {
                    Socket sock = serverSock.accept();
                    sock.getInputStream().read();
                    sock.close();
                } catch (IOException e) {
                    serverError = e;
                }
                semThreadEnd.release();
            }
        }.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        Socket client = new Socket("localhost", 8383);
        client.getOutputStream().write(1);
        try {
            semThreadEnd.acquire();
        } catch (InterruptedException e) {
        }
        serverSock.close();
        ServerSocket serverSock2 = new ServerSocket();
        serverSock2.setReuseAddress(true);
        serverSock2.bind(addr);
        serverSock2.close();
        if (serverError != null) {
            throw new RuntimeException("Server must complete without error", serverError);
        }
    }
    @LargeTest
    public void testTimeoutException() throws IOException {
        ServerSocket s = new ServerSocket(9800);
        s.setSoTimeout(2000);
        try {
            s.accept();
        } catch (SocketTimeoutException e) {
        }
    }
    @SmallTest
    public void testNativeSocketChannelOpen() throws IOException {
        SocketChannel.open();
    }
    @Suppress
    public void disable_testConnectWithIP4IPAddr() {
        InetSocketAddress scktAddrss = new InetSocketAddress(KNOW_GOOD_ADDRESS,
                80);
        Socket clntSckt = new Socket();
        try {
            clntSckt.connect(scktAddrss, 5000);
        } catch (Throwable e) {
            fail("connection problem:" + e.getClass().getName() + ": "
                    + e.getMessage());
        } finally {
            try {
                clntSckt.close();
            } catch (Exception e) {
            }
        }
    }
    private Socket client;
    private Exception error;
    private boolean connected;
    @Suppress
    public void disable_testSocketConnectClose() {
        try {
            client = new Socket();
            new Thread() {
                @Override
                public void run() {
                    try {
                        client.connect(new InetSocketAddress(PACKAGE_DROPPING_ADDRESS, 1357));
                    } catch (Exception ex) {
                        error = ex;
                    }
                    connected = true;
                }
            }.start();
            Thread.sleep(1000);
            Assert.assertNull("Connect must not fail immediately. Maybe try different address.", error);
            Assert.assertFalse("Connect must not succeed. Maybe try different address.", connected);
            client.close();
            Thread.sleep(1000);
            if (error == null) {
                fail("Socket connect still ongoing");
            } else if (!(error instanceof SocketException)) {
                fail("Socket connect interrupted with wrong error: " + error.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

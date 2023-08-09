@TestTargetClass(SSLSocketFactory.class) 
public class SSLSocketFactoryTest extends TestCase {
    private ServerSocket ss;
    protected int startServer(String name) {
        int portNumber = Support_PortManager.getNextPort();
        try {
            ss = new ServerSocket(portNumber);
        } catch (IOException e) {
            fail(name + ": " + e);
        }
        return ss.getLocalPort();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            SocketFactory sf = SSLSocketFactory.getDefault();
            assertTrue(sf instanceof SSLSocketFactory);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefault",
        args = {}
    )
    public void test_getDefault() {
        assertNotNull("Incorrect default socket factory",
                SSLSocketFactory.getDefault());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {java.net.Socket.class, java.lang.String.class, int.class, boolean.class}
    )
    public void test_createSocket() {
        SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
        int sport = startServer("test_createSocket()");
        int[] invalid = {
                Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE
        };
        try {
            Socket st = new Socket("localhost", sport);
            Socket s = sf.createSocket(st, "localhost", sport, false);
            assertFalse(s.isClosed());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
            Socket st = new Socket("localhost", sport);
            Socket s = sf.createSocket(st, "localhost", sport, true);
            s.close();
            assertTrue(st.isClosed());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
            sf.createSocket(null, "localhost", sport, true);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalid.length; i++) {
            try {
                Socket s = sf.createSocket(new Socket(), "localhost", 1080, false);
                fail("IOException wasn't thrown");
            } catch (IOException ioe) {
            }
        }
        try {
            Socket st = new Socket("bla-bla", sport);
            Socket s = sf.createSocket(st, "bla-bla", sport, false);
            fail("UnknownHostException wasn't thrown: " + "bla-bla");
        } catch (UnknownHostException uhe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of UnknownHostException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultCipherSuites",
        args = {}
    )
    public void test_getDefaultCipherSuites() {
        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            assertTrue("no default cipher suites returned",
                    sf.getDefaultCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedCipherSuites",
        args = {}
    )
    public void test_getSupportedCipherSuites() {
        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            assertTrue("no supported cipher suites returned",
                    sf.getSupportedCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}

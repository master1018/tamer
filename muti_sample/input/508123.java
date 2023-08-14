@TestTargetClass(SSLCertificateSocketFactory.class)
public class SSLCertificateSocketFactoryTest extends AndroidTestCase {
    private SSLCertificateSocketFactory mFactory;
    private int mTimeout;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTimeout = 1000;
        mFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(mTimeout);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getSupportedCipherSuites",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefault",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getDefaultCipherSuites",
            args = {}
        )
    })
    @ToBeFixed(bug="1695243", explanation="Android API javadocs are incomplete")
    public void testAccessProperties() throws Exception {
        mFactory.getSupportedCipherSuites();
        mFactory.getDefaultCipherSuites();
        SocketFactory sf = SSLCertificateSocketFactory.getDefault(mTimeout);
        assertNotNull(sf);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createSocket",
            args = {java.net.InetAddress.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createSocket",
            args = {java.net.Socket.class, java.lang.String.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createSocket",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "createSocket",
            args = {java.lang.String.class, int.class, java.net.InetAddress.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createSocket",
            args = {java.net.InetAddress.class, int.class, java.net.InetAddress.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SSLCertificateSocketFactory",
            args = {int.class}
        )
    })
    @BrokenTest("flaky")
    public void testCreateSocket() throws Exception {
        new SSLCertificateSocketFactory(100);
        int port = 443;
        String host = "www.fortify.net";
        InetAddress inetAddress = null;
        inetAddress = InetAddress.getLocalHost();
        try {
            mFactory.createSocket(inetAddress, port);
            fail("should throw exception!");
        } catch (IOException e) {
        }
        try {
            InetAddress inetAddress1 = InetAddress.getLocalHost();
            InetAddress inetAddress2 = InetAddress.getLocalHost();
            mFactory.createSocket(inetAddress1, port, inetAddress2, port);
            fail("should throw exception!");
        } catch (IOException e) {
        }
        try {
            Socket socket = new Socket();
            mFactory.createSocket(socket, host, port, true);
            fail("should throw exception!");
        } catch (IOException e) {
        }
        Socket socket = null;
        socket = mFactory.createSocket(host, port);
        assertNotNull(socket);
        assertNotNull(socket.getOutputStream());
        assertNotNull(socket.getInputStream());
    }
}

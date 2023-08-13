@TestTargetClass(SSLServerSocketFactory.class)
public class SSLServerSocketFactoryTest extends TestCase {
    private class MockSSLServerSocketFactory extends SSLServerSocketFactory {
        public MockSSLServerSocketFactory() {
            super();
        }
        @Override
        public String[] getDefaultCipherSuites() {
            return null;
        }
        @Override
        public String[] getSupportedCipherSuites() {
            return null;
        }
        @Override
        public ServerSocket createServerSocket(int arg0) throws IOException {
            return null;
        }
        @Override
        public ServerSocket createServerSocket(int arg0, int arg1)
                throws IOException {
            return null;
        }
        @Override
        public ServerSocket createServerSocket(int arg0, int arg1,
                InetAddress arg2) throws IOException {
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLServerSocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            MockSSLServerSocketFactory ssf = new MockSSLServerSocketFactory();
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
                SSLServerSocketFactory.getDefault());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultCipherSuites",
        args = {}
    )
    public void test_getDefaultCipherSuites() {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory
                .getDefault();
        try {
            assertTrue(ssf.getDefaultCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedCipherSuites",
        args = {}
    )
    public void test_getSupportedCipherSuites() {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory
                .getDefault();
        try {
            assertTrue(ssf.getSupportedCipherSuites().length > 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
}

@TestTargetClass(java.net.MulticastSocket.class)
public class JavaNetMulticastSocketTest extends TestCase {
    SecurityManager old;
    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that java.net.MulticastSocket(int) consructor calls checkListen on security permissions.",
            method = "MulticastSocket",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that java.net.MulticastSocket(int) consructor calls checkListen on security permissions.",
            method = "MulticastSocket",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that java.net.MulticastSocket(int) consructor calls checkListen on security permissions.",
            method = "MulticastSocket",
            args = {}
        )
    })
    public void test_ctor() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            int port = 0;
            void reset(){
                called = false;
                port = 0;
            }
            @Override
            public void checkListen(int port) {
                called = true;
                this.port = port;
                super.checkListen(port);
            }
            @Override
            public void checkPermission(Permission permission) {
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        int port = Support_PortManager.getNextPortForUDP();
        s.reset();
        new MulticastSocket(port);
        assertTrue("java.net.MulticastSocket(int) ctor must call checkListen on security permissions", s.called);
        assertEquals("Argument of checkListen is not correct", port, s.port);
        s.reset();
        new MulticastSocket(0);
        assertTrue("java.net.MulticastSocket() ctor must call checkListen on security permissions", s.called);
        assertEquals("Argument of checkListen is not correct", 0, s.port);
        s.reset();
        new MulticastSocket();
        assertTrue("java.net.MulticastSocket() ctor must call checkListen on security permissions", s.called);
        assertEquals("Argument of checkListen is not correct", 0, s.port);
        port = Support_PortManager.getNextPortForUDP();
        s.reset();
        new MulticastSocket(new InetSocketAddress(port));
        assertTrue("java.net.MulticastSocket() ctor must call checkListen on security permissions", s.called);
        assertEquals("Argument of checkListen is not correct", port, s.port);
    }    
}

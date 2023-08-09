@TestTargetClass(DatagramSocket.class)
public class JavaNetDatagramSocketTest extends TestCase {
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
            notes = "Verifies that java.net.DatagramSocket constructor calls checkListen of security manager.",
            method = "DatagramSocket",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that java.net.DatagramSocket constructor calls checkListen of security manager.",
            method = "DatagramSocket",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that java.net.DatagramSocket constructor calls checkListen of security manager.",
            method = "DatagramSocket",
            args = {int.class, java.net.InetAddress.class}
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
            }
            @Override
            public void checkPermission(Permission p) {
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        int port = Support_PortManager.getNextPortForUDP();
        s.reset();
        DatagramSocket s1 = new DatagramSocket(port);
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", port, s.port);
        s.reset();
        DatagramSocket s2 = new DatagramSocket();
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", 0, s.port);
        port = Support_PortManager.getNextPortForUDP();
        s.reset();
        DatagramSocket s3 = new DatagramSocket(new InetSocketAddress(port));
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", port, s.port);
        port = Support_PortManager.getNextPortForUDP();
        s.reset();
        DatagramSocket s4 = new DatagramSocket(port, InetAddress.getLocalHost());
        assertTrue("java.net.DatagramSocket ctor must call checkListen on security manager", s.called);
        assertEquals("Argument of checkListen is not correct", port, s.port);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that java.net.DatagramSocket receive calls checkAccept of security manager.",
        method = "receive",
        args = {java.net.DatagramPacket.class}
    )
    public void test_receive() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkAccept(String host, int port) {
                this.called = true;
                super.checkAccept(host, port);
            }
            @Override
            public void checkPermission(Permission p) {
            }
        }
        final int port = Support_PortManager.getNextPortForUDP();
        DatagramSocket s1 = new DatagramSocket(port);
        Thread sender = new Thread(){
            public void run(){
                try {
                    DatagramPacket sendPacket = new DatagramPacket(new byte[256], 256, InetAddress.getLocalHost(), port);
                    DatagramSocket sender = new DatagramSocket();
                    while(!isInterrupted()){
                        sender.send(sendPacket);
                        Thread.sleep(10);
                    }
                }
                catch(InterruptedException e){
                }
                catch(Exception e){
                    fail("unexpected exception " + e);
                }
            }
        };
        sender.start();
        DatagramPacket p = new DatagramPacket(new byte[256], 0, 256);
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        assertTrue(s1.getInetAddress()==null);
        try {
            s1.receive(p);
        } catch(Exception e) {
            fail("unexpected exception " + e);
        }
        sender.interrupt();
        assertTrue("java.net.DatagramSocket.receive must call checkAccept on security manager", s.called);
    }
}

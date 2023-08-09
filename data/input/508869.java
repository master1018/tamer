@TestTargetClass(ServerSocketFactory.class)
public class ServerSocketFactoryTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ServerSocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            ServerSocketFactory sf = new MyServerSocketFactory();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException checking missed",
        method = "createServerSocket",
        args = {}
    )
    public final void test_createServerSocket_01() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        try {
            ServerSocket ss = sf.createServerSocket();
            assertNotNull(ss);
        } catch (SocketException e) {        
        } catch (Exception e) {
            fail(e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createServerSocket",
        args = {int.class}
    )
    public final void test_createServerSocket_02() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        try {
            ServerSocket ss = sf.createServerSocket(portNumber);
            assertNotNull(ss);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
        try {
            sf.createServerSocket(portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IOException");
        }
        try {
            sf.createServerSocket(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException ioe) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createServerSocket",
        args = {int.class, int.class}
    )
    public final void test_createServerSocket_03() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        try {
            ServerSocket ss = sf.createServerSocket(portNumber, 0);
            assertNotNull(ss);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
        try {
            sf.createServerSocket(portNumber, 0);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IOException");
        }
        try {
            sf.createServerSocket(65536, 0);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException ioe) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createServerSocket",
        args = {int.class, int.class, InetAddress.class}
    )
    public final void test_createServerSocket_04() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        try {
            ServerSocket ss = sf.createServerSocket(portNumber, 0, InetAddress.getLocalHost());
            assertNotNull(ss);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
        try {
            sf.createServerSocket(portNumber, 0, InetAddress.getLocalHost());
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IOException");
        }
        try {
            sf.createServerSocket(Integer.MAX_VALUE, 0, InetAddress.getLocalHost());
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException ioe) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefault",
        args = {}
    )
    public final void test_getDefault() {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        ServerSocket s;
        try {
            s = sf.createServerSocket(0);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createServerSocket(0, 50);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createServerSocket(0, 50, InetAddress.getLocalHost());
            s.close();
        } catch (IOException e) {
        } 
    }
}
class MyServerSocketFactory extends ServerSocketFactory {
    public MyServerSocketFactory() {
        super();
    }
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return null;
    }
    @Override
    public ServerSocket createServerSocket(int port, int backlog)
            throws IOException {
        return null;
    }
    @Override
    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress address) throws IOException {
        return null;
    }
}
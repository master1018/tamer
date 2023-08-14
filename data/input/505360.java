@TestTargetClass(SocketFactory.class) 
public class SocketFactoryTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SocketFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            MySocketFactory sf = new MySocketFactory();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException check missed",
        method = "createSocket",
        args = {}
    )
    public final void test_createSocket_01() {
        SocketFactory sf = SocketFactory.getDefault();
        try {
            Socket s = sf.createSocket();
            assertNotNull(s);
            assertEquals(-1, s.getLocalPort());
            assertEquals(0, s.getPort());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        MySocketFactory msf = new MySocketFactory();
        try {
            msf.createSocket();
            fail("No expected SocketException");
        } catch (SocketException e) {        
        } catch (IOException e) {
            fail(e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {String.class, int.class}
    )
    public final void test_createSocket_02() {
        SocketFactory sf = SocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons String,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport);
            assertNotNull(s);
            assertTrue("Failed to create socket", s.getPort() == sport);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            Socket s = sf.createSocket("bla-bla", sport);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException uhe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of UnknownHostException");
        }
        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
        }
        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket("localhost", 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {InetAddress.class, int.class}
    )
    public final void test_createSocket_03() {
        SocketFactory sf = SocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons InetAddress,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), sport);
            assertNotNull(s);
            assertTrue("Failed to create socket", s.getPort() == sport);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
        }
        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket(InetAddress.getLocalHost(), 8081);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {InetAddress.class, int.class, InetAddress.class, int.class}
    )
    public final void test_createSocket_04() {
        SocketFactory sf = SocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons InetAddress,I,InetAddress,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), sport,
                                       InetAddress.getLocalHost(), portNumber);
            assertNotNull(s);
            assertTrue("1: Failed to create socket", s.getPort() == sport);
            assertTrue("2: Failed to create socket", s.getLocalPort() == portNumber);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost(), invalidPorts[i],
                                           InetAddress.getLocalHost(), portNumber);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost(), sport,
                                           InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost(), sport,
                                       InetAddress.getLocalHost(), portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException ioe) {
        }
        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        }     
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createSocket",
        args = {String.class, int.class, InetAddress.class, int.class}
    )
    public final void test_createSocket_05() {
        SocketFactory sf = SocketFactory.getDefault();
        int portNumber = Support_PortManager.getNextPort();
        int sport = startServer("Cons String,I,InetAddress,I");
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};
        try {
            Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport,
                                       InetAddress.getLocalHost(), portNumber);
            assertNotNull(s);
            assertTrue("1: Failed to create socket", s.getPort() == sport);
            assertTrue("2: Failed to create socket", s.getLocalPort() == portNumber);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        portNumber = Support_PortManager.getNextPort();
        try {
            Socket s = sf.createSocket("bla-bla", sport, InetAddress.getLocalHost(), portNumber);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException uhe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of UnknownHostException");
        }
        for (int i = 0; i < invalidPorts.length; i++) {
            portNumber = Support_PortManager.getNextPort();
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), invalidPorts[i],
                                           InetAddress.getLocalHost(), portNumber);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
            try {
                Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport,
                                           InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException iae) {
            } catch (Exception e) {
                fail(e + " was thrown instead of IllegalArgumentException for " + invalidPorts[i]);
            }
        }
        SocketFactory f = SocketFactory.getDefault();
        try {
            Socket s = f.createSocket("localhost", 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefault",
        args = {}
    )
    public final void test_getDefault() {
        SocketFactory sf = SocketFactory.getDefault();
        Socket s;
        try {
            s = sf.createSocket("localhost", 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket("localhost", 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081);
            s.close();
        } catch (IOException e) {
        } 
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
        }     
    }
    protected int startServer(String name) {
        int portNumber = Support_PortManager.getNextPort();
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(portNumber);
        } catch (IOException e) {
            fail(name + ": " + e);
        }
        return ss.getLocalPort();
    }
}
class MySocketFactory extends SocketFactory {
    public MySocketFactory() {
        super();
    }
    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return null;
    }
    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return null;
    }
    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return null;
     }
    @Override
    public Socket createSocket(InetAddress address, int port, 
                               InetAddress localAddress, int localPort) throws IOException {
        return null;
     }
}

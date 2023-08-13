@TestTargetClass(DatagramPacket.class) 
public class DatagramPacketTest extends junit.framework.TestCase {
    DatagramPacket dp;
    volatile boolean started = false;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DatagramPacket",
        args = {byte[].class, int.class}
    )
    public void test_Constructor$BI() {
        try {
            dp = new DatagramPacket("Hello".getBytes(), 5);
            assertEquals("Created incorrect packet", "Hello", new String(dp.getData(), 0,
                    dp.getData().length));
            assertEquals("Wrong length", 5, dp.getLength());
        } catch (Exception e) {
            fail("Exception during Constructor test: " + e.toString());
        }
        dp = new DatagramPacket(new byte[942],4);
        assertEquals(-1, dp.getPort());
        try{
            dp.getSocketAddress();
            fail("Should throw IllegalArgumentException");            
        }catch(IllegalArgumentException e){
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DatagramPacket",
        args = {byte[].class, int.class, int.class}
    )
    public void test_Constructor$BII() {
        try {
            dp = new DatagramPacket("Hello".getBytes(), 2, 3);
            assertEquals("Created incorrect packet", "Hello", new String(dp.getData(), 0,
                    dp.getData().length));
            assertEquals("Wrong length", 3, dp.getLength());
            assertEquals("Wrong offset", 2, dp.getOffset());
        } catch (Exception e) {
            fail("Exception during Constructor test: " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DatagramPacket",
        args = {byte[].class, int.class, int.class, java.net.InetAddress.class, int.class}
    )
    public void test_Constructor$BIILjava_net_InetAddressI() {
        try {
            dp = new DatagramPacket("Hello".getBytes(), 2, 3, InetAddress
                    .getLocalHost(), 0);
            assertTrue("Created incorrect packet", dp.getAddress().equals(
                    InetAddress.getLocalHost())
                    && dp.getPort() == 0);
            assertEquals("Wrong length", 3, dp.getLength());
            assertEquals("Wrong offset", 2, dp.getOffset());
        } catch (Exception e) {
            fail("Exception during Constructor test: " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DatagramPacket",
        args = {byte[].class, int.class, java.net.InetAddress.class, int.class}
    )
    public void test_Constructor$BILjava_net_InetAddressI() {
        try {
            dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
                    .getLocalHost(), 0);
            assertTrue("Created incorrect packet", dp.getAddress().equals(
                    InetAddress.getLocalHost())
                    && dp.getPort() == 0);
            assertEquals("Wrong length", 5, dp.getLength());
        } catch (Exception e) {
            fail("Exception during Constructor test: " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAddress",
        args = {}
    )
    public void test_getAddress() {
        try {
            dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
                    .getLocalHost(), 0);
            assertTrue("Incorrect address returned", dp.getAddress().equals(
                    InetAddress.getLocalHost()));
        } catch (Exception e) {
            fail("Exception during getAddress test:" + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getData",
        args = {}
    )
    public void test_getData() {
        dp = new DatagramPacket("Hello".getBytes(), 5);
        assertEquals("Incorrect length returned", "Hello", new String(dp.getData(), 0, dp
                .getData().length));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLength",
        args = {}
    )
    public void test_getLength() {
        dp = new DatagramPacket("Hello".getBytes(), 5);
        assertEquals("Incorrect length returned", 5, dp.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOffset",
        args = {}
    )
    public void test_getOffset() {
        dp = new DatagramPacket("Hello".getBytes(), 3, 2);
        assertEquals("Incorrect length returned", 3, dp.getOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPort",
        args = {}
    )
    public void test_getPort() {
        try {
            dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
                    .getLocalHost(), 1000);
            assertEquals("Incorrect port returned", 1000, dp.getPort());
        } catch (Exception e) {
            fail("Exception during getPort test : " + e.getMessage());
        }
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            fail("Unexpected UnknownHostException : " + e.getMessage());
        }
        int[] ports = Support_PortManager.getNextPortsForUDP(2);
        final int port = ports[0];
        final Object lock = new Object();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket(port);
                    synchronized (lock) {
                        started = true;
                        lock.notifyAll();
                    }
                    socket.setSoTimeout(3000);
                    DatagramPacket packet = new DatagramPacket(new byte[256],
                            256);
                    socket.receive(packet);
                    socket.send(packet);
                    socket.close();
                } catch (IOException e) {
                    System.out.println("thread exception: " + e);
                    if (socket != null)
                        socket.close();
                }
            }
        });
        thread.start();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(ports[1]);
            socket.setSoTimeout(3000);
            DatagramPacket packet = new DatagramPacket(new byte[] { 1, 2, 3, 4,
                    5, 6 }, 6, localhost, port);
            synchronized (lock) {
                try {
                    if (!started)
                        lock.wait();
                } catch (InterruptedException e) {
                    fail(e.toString());
                }
            }
            socket.send(packet);
            socket.receive(packet);
            socket.close();
            assertTrue("datagram received wrong port: " + packet.getPort(),
                    packet.getPort() == port);
        } catch (IOException e) {
            if (socket != null)
                socket.close();
            System.err.println("port: " + port + " datagram server error: ");
            e.printStackTrace();
            fail("port : " + port + " datagram server error : "
                    + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setAddress",
        args = {java.net.InetAddress.class}
    )
    public void test_setAddressLjava_net_InetAddress() {
        try {
            InetAddress ia = InetAddress
                    .getByName(Support_Configuration.InetTestIP);
            dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
                    .getLocalHost(), 0);
            dp.setAddress(ia);
            assertTrue("Incorrect address returned", dp.getAddress().equals(ia));
        } catch (Exception e) {
            fail("Exception during getAddress test:" + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setData",
        args = {byte[].class, int.class, int.class}
    )
    public void test_setData$BII() {
        dp = new DatagramPacket("Hello".getBytes(), 5);
        dp.setData("Wagga Wagga".getBytes(), 2, 3);
        assertEquals("Incorrect data set", "Wagga Wagga", new String(dp.getData())
                );
        try {
            dp.setData(null, 2, 3);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setData",
        args = {byte[].class}
    )
    public void test_setData$B() {
        dp = new DatagramPacket("Hello".getBytes(), 5);
        dp.setData("Ralph".getBytes());
        assertEquals("Incorrect data set", "Ralph", new String(dp.getData(), 0, dp
                .getData().length));
        try {
            dp.setData(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLength",
        args = {int.class}
    )
    public void test_setLengthI() {
        dp = new DatagramPacket("Hello".getBytes(), 5);
        dp.setLength(1);
        assertEquals("Failed to set packet length", 1, dp.getLength());
        try {
            new DatagramPacket("Hello".getBytes(), 6);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            new DatagramPacket("Hello".getBytes(), -1);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPort",
        args = {int.class}
    )
    public void test_setPortI() {
        try {
            dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
                    .getLocalHost(), 1000);
            dp.setPort(2000);
            assertEquals("Port not set", 2000, dp.getPort());
        } catch (Exception e) {
            fail("Exception during setPort test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SocketException checking missed.",
        method = "DatagramPacket",
        args = {byte[].class, int.class, java.net.SocketAddress.class}
    )
    public void test_Constructor$BILjava_net_SocketAddress() {
        class mySocketAddress extends SocketAddress {
            public mySocketAddress() {
            }
        }
        try {
            byte buf[] = new byte[1];
            try {
                DatagramPacket thePacket = new DatagramPacket(buf, 1,
                        new mySocketAddress());
                fail("No exception when constructing using unsupported SocketAddress subclass");
            } catch (IllegalArgumentException ex) {
            }
            try {
                DatagramPacket thePacket = new DatagramPacket(buf, 1, null);
                fail("No exception when constructing address using null");
            } catch (IllegalArgumentException ex) {
            }
            InetSocketAddress theAddress = new InetSocketAddress(InetAddress
                    .getLocalHost(), Support_PortManager.getNextPortForUDP());
            DatagramPacket thePacket = new DatagramPacket(buf, 1, theAddress);
            assertTrue("Socket address not set correctly (1)", theAddress
                    .equals(thePacket.getSocketAddress()));
            assertTrue("Socket address not set correctly (2)", theAddress
                    .equals(new InetSocketAddress(thePacket.getAddress(),
                            thePacket.getPort())));
        } catch (Exception e) {
            fail("Exception during constructor test(1):" + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SocketException checking missed.",
        method = "DatagramPacket",
        args = {byte[].class, int.class, int.class, java.net.SocketAddress.class}
    )
    public void test_Constructor$BIILjava_net_SocketAddress() {
        class mySocketAddress extends SocketAddress {
            public mySocketAddress() {
            }
        }
        try {
            byte buf[] = new byte[2];
            try {
                DatagramPacket thePacket = new DatagramPacket(buf, 1, 1,
                        new mySocketAddress());
                fail("No exception when constructing using unsupported SocketAddress subclass");
            } catch (IllegalArgumentException ex) {
            }
            try {
                DatagramPacket thePacket = new DatagramPacket(buf, 1, 1, null);
                fail("No exception when constructing address using null");
            } catch (IllegalArgumentException ex) {
            }
            InetSocketAddress theAddress = new InetSocketAddress(InetAddress
                    .getLocalHost(), Support_PortManager.getNextPortForUDP());
            DatagramPacket thePacket = new DatagramPacket(buf, 1, 1, theAddress);
            assertTrue("Socket address not set correctly (1)", theAddress
                    .equals(thePacket.getSocketAddress()));
            assertTrue("Socket address not set correctly (2)", theAddress
                    .equals(new InetSocketAddress(thePacket.getAddress(),
                            thePacket.getPort())));
            assertEquals("Offset not set correctly", 1, thePacket.getOffset());
        } catch (Exception e) {
            fail("Exception during constructor test(2):" + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSocketAddress",
        args = {}
    )
    public void test_getSocketAddress() {
        try {
            byte buf[] = new byte[1];
            DatagramPacket thePacket = new DatagramPacket(buf, 1);
            InetSocketAddress theAddress = new InetSocketAddress(InetAddress
                    .getLocalHost(), Support_PortManager.getNextPortForUDP());
            thePacket = new DatagramPacket(buf, 1);
            thePacket.setSocketAddress(theAddress);
            assertTrue("Socket address not set correctly (1)", theAddress
                    .equals(thePacket.getSocketAddress()));
        } catch (Exception e) {
            fail(
                    "Exception during getSocketAddress test:" + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSocketAddress",
        args = {java.net.SocketAddress.class}
    )
    public void test_setSocketAddressLjava_net_SocketAddress() {
        class mySocketAddress extends SocketAddress {
            public mySocketAddress() {
            }
        }
        try {
            byte buf[] = new byte[1];
            DatagramPacket thePacket = new DatagramPacket(buf, 1);
            try {
                thePacket.setSocketAddress(new mySocketAddress());
                fail("No exception when setting address using unsupported SocketAddress subclass");
            } catch (IllegalArgumentException ex) {
            }
            thePacket = new DatagramPacket(buf, 1);
            try {
                thePacket.setSocketAddress(null);
                fail("No exception when setting address using null");
            } catch (IllegalArgumentException ex) {
            }
            InetSocketAddress theAddress = new InetSocketAddress(InetAddress
                    .getLocalHost(), Support_PortManager.getNextPortForUDP());
            thePacket = new DatagramPacket(buf, 1);
            thePacket.setSocketAddress(theAddress);
            assertTrue("Socket address not set correctly (1)", theAddress
                    .equals(thePacket.getSocketAddress()));
            assertTrue("Socket address not set correctly (2)", theAddress
                    .equals(new InetSocketAddress(thePacket.getAddress(),
                            thePacket.getPort())));
        } catch (Exception e) {
            fail(
                    "Exception during setSocketAddress test:" + e.toString());
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
    protected void doneSuite() {
    }
}

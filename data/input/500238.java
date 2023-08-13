@TestTargetClass(value = DatagramSocketImpl.class,
                 untestedMethods = {
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "bind",
                     args = {int.class, InetAddress.class}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "close",
                     args = {}
                 ),   
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "create",
                     args = {}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "getTimeToLive",
                     args = {}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "getTTL",
                     args = {}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "join",
                     args = {InetAddress.class}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "joinGroup",
                     args = { SocketAddress.class, NetworkInterface.class }
                 ),  
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "leave",
                     args = { InetAddress.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "leaveGroup",
                     args = { SocketAddress.class, NetworkInterface.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "peek",
                     args = { InetAddress.class }
                 ),     
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "peekData",
                     args = { DatagramPacket.class }
                 ),    
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "receive",
                     args = { DatagramPacket.class }
                 ), 
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "send",
                     args = { DatagramPacket.class }
                 ),    
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "setTimeToLive",
                     args = { int.class }
                 ),     
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "setTTL",
                     args = { byte.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "setOption",
                     args = { int.class, Object.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "getOption",
                     args = { int.class }
                 )                 
             }) 
public class DatagramSocketImplTest extends junit.framework.TestCase {
    MockDatagramSocketImpl ds;
    public void setUp() {
        ds = new MockDatagramSocketImpl();
    }
    public void tearDown() {
        ds.close();
        ds = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DatagramSocketImpl",
        args = {}
    )
    public void test_Constructor() throws Exception {
        MockDatagramSocketImpl impl = new MockDatagramSocketImpl();
        assertNull(impl.getFileDescriptor());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SocketException is not checked.",
        method = "connect",
        args = {java.net.InetAddress.class, int.class}
    )
    public void test_connect() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ds.connect(localHost, 0);
            DatagramPacket send = new DatagramPacket(new byte[10], 10,
                    localHost, 0);
            ds.send(send);
        } catch (IOException e) {
            fail("Unexpected IOException : " + e.getMessage());
        } finally {
            ds.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "disconnect",
        args = {}
    )
    public void test_disconnect() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ds.connect(localHost, 0);
            DatagramPacket send = new DatagramPacket(new byte[10], 10,
                    localHost, 0);
            ds.send(send);
            ds.disconnect();
        } catch (IOException e) {
            fail("Unexpected IOException : " + e.getMessage());
        } finally {
            ds.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFileDescriptor",
        args = {}
    )
    public void test_getFileDescriptor() {
        assertNull(ds.getFileDescriptor());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalPort",
        args = {}
    )
    @AndroidOnly("Bug in RI")
    public void test_getLocalPort() {
        assertEquals(-1, ds.getLocalPort());
    }
}
class MockDatagramSocketImpl extends DatagramSocketImpl {
    @Override
    public FileDescriptor getFileDescriptor() {
        return super.getFileDescriptor();
    }
    @Override
    public void bind(int port, InetAddress addr) throws SocketException {
    }
    @Override
    public void close() {
    }
    @Override
    public void create() throws SocketException {
    }
    @Override
    public byte getTTL() throws IOException {
        return 0;
    }
    @Override
    public int getTimeToLive() throws IOException {
        return 0;
    }
    @Override
    public void join(InetAddress addr) throws IOException {
    }
    @Override
    public void joinGroup(SocketAddress addr, NetworkInterface netInterface)
            throws IOException {
    }
    @Override
    public void leave(InetAddress addr) throws IOException {
    }
    @Override
    public void leaveGroup(SocketAddress addr, NetworkInterface netInterface)
            throws IOException {
    }
    @Override
    public int peek(InetAddress sender) throws IOException {
        return 0;
    }
    @Override
    public int peekData(DatagramPacket pack) throws IOException {
        return 0;
    }
    @Override
    public void receive(DatagramPacket pack) throws IOException {
    }
    @Override
    public void send(DatagramPacket pack) throws IOException {
    }
    @Override
    public void setTTL(byte ttl) throws IOException {
    }
    @Override
    public void setTimeToLive(int ttl) throws IOException {
    }
    public Object getOption(int optID) throws SocketException {
        return null;
    }
    public void setOption(int optID, Object value) throws SocketException {
    }
    public void connect(InetAddress address, int port) throws SocketException {
        super.connect(address, port);
    }
    public void disconnect() {
        super.disconnect();
    }
    public int getLocalPort() {
        return super.getLocalPort();
    }
}

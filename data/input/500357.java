@TestTargetClass(DatagramSocketImplFactory.class) 
public class DatagramSocketImplFactoryTest extends TestCase {
    DatagramSocketImplFactory oldFactory = null;
    Field factoryField = null;
    boolean isTestable = false;
    boolean isDatagramSocketImplCalled = false;
    boolean isCreateDatagramSocketImpl = false;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "createDatagramSocketImpl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies SecurityException.",
            clazz = DatagramSocket.class,
            method = "setDatagramSocketImplFactory",
            args = {java.net.DatagramSocketImplFactory.class}
        )
    })
    public void test_createDatagramSocketImpl() throws IllegalArgumentException, 
                                                                    IOException {
        if(isTestable) {
            DatagramSocketImplFactory factory = new TestDatagramSocketImplFactory();
            assertFalse(isCreateDatagramSocketImpl);
            DatagramSocket.setDatagramSocketImplFactory(factory);
            try {
                DatagramSocket ds = new java.net.DatagramSocket();
                assertTrue(isCreateDatagramSocketImpl);
                assertTrue(isDatagramSocketImplCalled);
            } catch (Exception e) {
                fail("Exception during test : " + e.getMessage());
            }
            try {
                DatagramSocket.setDatagramSocketImplFactory(factory);
                fail("SocketException was not thrown.");                
            } catch(SocketException se) {
            }
            try {
                DatagramSocket.setDatagramSocketImplFactory(null);
                fail("SocketException was not thrown.");                
            } catch(SocketException se) {
            }
        } else {
            TestDatagramSocketImplFactory dsf = new TestDatagramSocketImplFactory();
            DatagramSocketImpl dsi = dsf.createDatagramSocketImpl();
            try {
                assertNull(dsi.getOption(0));
            } catch (SocketException e) {
                fail("SocketException was thrown.");
            }
        }
    }
    public void setUp() {
        Field [] fields = DatagramSocket.class.getDeclaredFields();
        int counter = 0;
        for (Field field : fields) {
            if (DatagramSocketImplFactory.class.equals(field.getType())) {
                counter++;
                factoryField = field;
            }
        } 
        if(counter == 1) {
            isTestable = true;
            factoryField.setAccessible(true);
            try {
                oldFactory = (DatagramSocketImplFactory) factoryField.get(null);
            } catch (IllegalArgumentException e) {
                fail("IllegalArgumentException was thrown during setUp: " 
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                fail("IllegalAccessException was thrown during setUp: "
                        + e.getMessage());
            }        
        }
    }
    public void tearDown() {
        if(isTestable) {
            try {
                factoryField.set(null, oldFactory);
            } catch (IllegalArgumentException e) {
                fail("IllegalArgumentException was thrown during tearDown: " 
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                fail("IllegalAccessException was thrown during tearDown: "
                        + e.getMessage());
            }
        }
    }
    class TestDatagramSocketImplFactory implements DatagramSocketImplFactory {
        public DatagramSocketImpl createDatagramSocketImpl() {
            isCreateDatagramSocketImpl = true;
            return new TestDatagramSocketImpl();
        }
    }
    class TestDatagramSocketImpl extends DatagramSocketImpl {
        @Override
        protected void bind(int arg0, InetAddress arg1) throws SocketException {
        }
        @Override
        protected void close() {
        }
        @Override
        protected void create() throws SocketException {
            isDatagramSocketImplCalled = true;
        }
        @Override
        protected byte getTTL() throws IOException {
            return 0;
        }
        @Override
        protected int getTimeToLive() throws IOException {
            return 0;
        }
        @Override
        protected void join(InetAddress arg0) throws IOException {
        }
        @Override
        protected void joinGroup(SocketAddress arg0, NetworkInterface arg1) throws IOException {
        }
        @Override
        protected void leave(InetAddress arg0) throws IOException {
        }
        @Override
        protected void leaveGroup(SocketAddress arg0, NetworkInterface arg1) throws IOException {
        }
        @Override
        public int peek(InetAddress arg0) throws IOException {
            return 10;
        }
        @Override
        protected int peekData(DatagramPacket arg0) throws IOException {
            return 0;
        }
        @Override
        protected void receive(DatagramPacket arg0) throws IOException {
        }
        @Override
        protected void send(DatagramPacket arg0) throws IOException {
        }
        @Override
        protected void setTTL(byte arg0) throws IOException {
        }
        @Override
        protected void setTimeToLive(int arg0) throws IOException {
        }
        public Object getOption(int arg0) throws SocketException {
            return null;
        }
        public void setOption(int arg0, Object arg1) throws SocketException {
        }
    }    
}

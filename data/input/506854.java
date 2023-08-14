@TestTargetClass(InetSocketAddress.class) 
public class InetSocketAddressTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "InetSocketAddress",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SocketAddress.class,
            method = "SocketAddress",
            args = {}
        )  
    })
    public void test_ConstructorLjava_lang_StringI() throws Exception {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 0);
        assertNotNull(address.getHostName());
        try {
            new InetSocketAddress("127.0.0.1", -1);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            new InetSocketAddress("127.0.0.1", 65536);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }      
        class MockSecurityManager extends SecurityManager {        
            public void checkPermission(Permission permission) {
            }
            public void checkConnect(String host, int port) {
                if(host.equals("google.com")) {
                    throw new SecurityException();
                }
            }
        }
        SecurityManager oldman = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            boolean exception = false;
            try {
                new InetSocketAddress("google.com", 80);
                fail("SecurityException was not thrown.");
            } catch (SecurityException ex) {
            } catch (Exception ex) {
                fail("getByName threw wrong exception : " + ex.getMessage());
            }
        } finally {
            System.setSecurityManager(oldman);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "InetSocketAddress",
            args = {java.net.InetAddress.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getHostName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPort",
            args = {}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                clazz = SocketAddress.class,
                method = "SocketAddress",
                args = {}
        )  
    })
    public void test_ConstructorLInetAddressI() {
        String validIPAddresses[] = { "::1.2.3.4", "::", "::", "1::0", "1::",
                "::1", "0", 
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF",
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:255.255.255.255",
                "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0.0.0.0",
                "127.0.0.1", "localhost", "1.1", "42.42.42.42", "0.0.0.0"};
        String results [] = { "0:0:0:0:0:0:102:304", "0:0:0:0:0:0:0:0", 
                "0:0:0:0:0:0:0:0", "1:0:0:0:0:0:0:0", "1:0:0:0:0:0:0:0",
                "0:0:0:0:0:0:0:1", "0.0.0.0", 
                "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
                "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
                "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0:0",
                "localhost", "localhost", "1.0.0.1", "42.42.42.42", "0.0.0.0"};
        for(int i = 0; i < validIPAddresses.length; i++) {
            try {
                InetAddress ia = InetAddress.getByName(validIPAddresses[i]);             
                InetSocketAddress isa = new InetSocketAddress(ia, 80);  
                assertEquals(80,isa.getPort());
            } catch(UnknownHostException uhe) {
                fail("UnknownHostException was thrown for: " + 
                        validIPAddresses[i]);
            }
        }
        try {
            InetSocketAddress isa = new InetSocketAddress((InetAddress)null, 80);
            assertEquals("0.0.0.0", isa.getHostName());
        } catch(Exception e) {
            fail("Unexpected exception was thrown.");
        }
        try {
            InetAddress isa = InetAddress.getByName("localhost");  
            new InetSocketAddress(isa, 65536);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        } catch (UnknownHostException e) {
            fail("UnknownHostException was thrown.");
        }
        try {
            InetAddress isa = InetAddress.getByName("localhost");  
            new InetSocketAddress(isa, -1);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        } catch (UnknownHostException e) {
            fail("UnknownHostException was thrown.");
        }
    }
    @TestTargets ({
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                method = "InetSocketAddress",
                args = {int.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                clazz = SocketAddress.class,
                method = "SocketAddress",
                args = {}
        )        
    })
    public void test_ConstructorI() {
        InetSocketAddress isa = new  InetSocketAddress(65535);
        assertEquals("0.0.0.0", isa.getHostName());
        assertEquals(65535, isa.getPort());
        try {
            new  InetSocketAddress(-1);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException  iae) {
        }        
        try {
            new  InetSocketAddress(65536);
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException  iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is the complete subset of tests for createUnresolved method.",
        method = "createUnresolved",
        args = {java.lang.String.class, int.class}
    )
    public void test_createUnresolvedLjava_lang_StringI() {
        HostPortPair[] legalHostPortPairs = { new HostPortPair("127.0.0.1", 1234),
                new HostPortPair("192.168.0.1", 10000), new HostPortPair("127.0.0", 0),
                new HostPortPair("127.0.0", 65535),
                new HostPortPair("strange host", 65535) };
        for (int i = 0; i < legalHostPortPairs.length; i++) {
            InetSocketAddress isa = InetSocketAddress.createUnresolved(
                    legalHostPortPairs[i].host, legalHostPortPairs[i].port);
            assertTrue(isa.isUnresolved());
            assertNull(isa.getAddress());
            assertEquals(isa.getHostName(), legalHostPortPairs[i].host);
            assertEquals(isa.getPort(), legalHostPortPairs[i].port);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is the complete subset of tests for createUnresolved method.",
        method = "createUnresolved",
        args = {java.lang.String.class, int.class}
    )
    public void test_createUnresolvedLjava_lang_StringI_IllegalArgumentException() {
        HostPortPair[] illegalHostPortPairs = { new HostPortPair(null, 1),
                new HostPortPair("host", -1), new HostPortPair("host", 65536) };
        for (int i = 0; i < illegalHostPortPairs.length; i++) {
            try {
                InetSocketAddress.createUnresolved(
                        illegalHostPortPairs[i].host,
                        illegalHostPortPairs[i].port);
                fail("should throw IllegalArgumentException, host = "
                        + illegalHostPortPairs[i].host + ",port = "
                        + illegalHostPortPairs[i].port);
            } catch (IllegalArgumentException e) {
            }
        }
    }
    class HostPortPair {
        String host;
        int port;
        public HostPortPair(String host, int port) {
            this.host = host;
            this.port = port;
        }
    };
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            InetSocketAddress init = (InetSocketAddress) initial;
            InetSocketAddress desr = (InetSocketAddress) deserialized;
            assertEquals("HostName", init.getHostName(), desr.getHostName());
            assertEquals("Port", init.getPort(), desr.getPort());
            assertEquals("Address", init.getAddress(), desr.getAddress());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        Object[] testCases = {
                InetSocketAddress.createUnresolved("badhost", 1000), 
                new InetSocketAddress("Localhost", 1000) };
        SerializationTest.verifySelf(testCases, COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        Object[] testCases = {
                InetSocketAddress.createUnresolved("badhost", 1000), 
                new InetSocketAddress("Localhost", 1000) };
        SerializationTest.verifyGolden(this, testCases, COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() {
        InetSocketAddress isa1 = new InetSocketAddress(1);
        InetSocketAddress isa2 = new InetSocketAddress(2);
        assertFalse(isa1.equals(isa2));
        InetSocketAddress isa3 = new InetSocketAddress(1);
        assertTrue(isa1.equals(isa3));
        isa1 = new InetSocketAddress("localhost", 80);
        isa2 = new InetSocketAddress("127.0.0.1", 80);
        assertTrue(isa1.equals(isa2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAddress",
        args = {}
    )
    public void test_getAddress() {
        String validIPAddresses[] = { "::1.2.3.4", "::", "::", "1::0", "1::",
                "::1", "0", 
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF",
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:255.255.255.255",
                "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0.0.0.0",
                "127.0.0.1", "localhost", "1.1", "42.42.42.42", "0.0.0.0"};
        for(int i = 0; i < validIPAddresses.length; i++) {
            try {
                InetAddress ia = InetAddress.getByName(validIPAddresses[i]);             
                InetSocketAddress isa = new InetSocketAddress(ia, 0);
                assertEquals(ia, isa.getAddress());
            } catch(UnknownHostException uhe) {
                fail("UnknownHostException was thrown for: " + 
                        validIPAddresses[i]);
            }
        }
        InetSocketAddress isa = new InetSocketAddress((InetAddress) null, 0);
        assertNotNull(isa.getAddress());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        InetSocketAddress isa1 = new InetSocketAddress("localhost", 8080);
        InetSocketAddress isa2 = new InetSocketAddress("127.0.0.1", 8080);
        assertTrue(isa1.hashCode() == isa2.hashCode());
        InetSocketAddress isa3 = new InetSocketAddress("0.0.0.0", 8080);
        assertFalse(isa1.hashCode() == isa3.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isUnresolved",
        args = {}
    )
    public void test_isUnresolved() {
        InetSocketAddress isa1 = new InetSocketAddress("localhost", 80);
        assertFalse(isa1.isUnresolved());
        InetSocketAddress sockAddr = new InetSocketAddress("unknown.host", 1000);
        assertTrue(sockAddr.isUnresolved());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        InetSocketAddress isa = new InetSocketAddress("localhost", 80);
        assertNotNull(isa.toString());
    }
}

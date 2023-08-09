@TestTargetClass(InetAddress.class) 
public class InetAddressTest extends junit.framework.TestCase {
    private static boolean someoneDone[] = new boolean[2];
    protected static boolean threadedTestSucceeded;
    protected static String threadedTestErrorString;
    @Override protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
    }
    @Override protected void tearDown() throws Exception {
        TestEnvironment.reset();
        super.tearDown();
    }
    static class threadsafeTestThread extends Thread {
        private String lookupName;
        private InetAddress testAddress;
        private int testType;
        private static final int REP_NUM = 20000;
        public threadsafeTestThread(String name, String lookupName,
                InetAddress testAddress, int type) {
            super(name);
            this.lookupName = lookupName;
            this.testAddress = testAddress;
            testType = type;
        }
        public void run() {
            try {
                String correctName = testAddress.getHostName();
                String correctAddress = testAddress.getHostAddress();
                long startTime = System.currentTimeMillis();
                synchronized (someoneDone) {
                }
                for (int i = 0; i < REP_NUM; i++) {
                    if (someoneDone[testType]) {
                        break;
                    } else if ((i % 25) == 0
                            && System.currentTimeMillis() - startTime > 240000) {
                        System.out
                                .println("Exiting due to time limitation after "
                                        + i + " iterations");
                        break;
                    }
                    InetAddress ia = InetAddress.getByName(lookupName);
                    String hostName = ia.getHostName();
                    String hostAddress = ia.getHostAddress();
                    if (!hostName.startsWith(correctName)) {
                        threadedTestSucceeded = false;
                        threadedTestErrorString = (testType == 0 ? "gethostbyname"
                                : "gethostbyaddr")
                                + ": getHostName() returned "
                                + hostName
                                + " instead of " + correctName;
                        break;
                    }
                    if (!correctAddress.equals(hostAddress)) {
                        threadedTestSucceeded = false;
                        threadedTestErrorString = (testType == 0 ? "gethostbyname"
                                : "gethostbyaddr")
                                + ": getHostName() returned "
                                + hostAddress
                                + " instead of " + correctAddress;
                        break;
                    }
                }
                someoneDone[testType] = true;
            } catch (Exception e) {
                threadedTestSucceeded = false;
                threadedTestErrorString = e.toString();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        try {
            InetAddress ia1 = InetAddress
                    .getByName(Support_Configuration.InetTestAddress);
            InetAddress ia2 = InetAddress
                    .getByName(Support_Configuration.InetTestIP);
            assertTrue("Equals returned incorrect result - " + ia1 + " != "
                    + ia2, ia1.equals(ia2));
        } catch (Exception e) {
            fail("Exception during equals test : " + e.getMessage());
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
            InetAddress ia = InetAddress
                    .getByName(Support_Configuration.InetTestIP);
            byte[] caddr = Support_Configuration.InetTestAddr;
            byte[] addr = ia.getAddress();
            for (int i = 0; i < addr.length; i++)
                assertTrue("Incorrect address returned", caddr[i] == addr[i]);
        } catch (java.net.UnknownHostException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAllByName",
        args = {java.lang.String.class}
    )
    public void test_getAllByNameLjava_lang_String() throws Exception {
        InetAddress[] all = InetAddress
                .getAllByName(Support_Configuration.SpecialInetTestAddress);
        assertNotNull(all);
        assertTrue(all.length >= 1);
        for (InetAddress alias : all) {
            assertTrue(alias.getHostName().startsWith(
                    Support_Configuration.SpecialInetTestAddress));
        }
        SecurityManager oldman = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            boolean exception = false;
            try {
                InetAddress.getByName("3d.com");
            } catch (SecurityException ex) {
                exception = true;
            } catch (Exception ex) {
                fail("getByName threw wrong exception : " + ex.getMessage());
            }
            assertTrue("expected SecurityException", exception);
        } finally {
            System.setSecurityManager(oldman);
        }
        InetAddress[] addresses = InetAddress.getAllByName(null);
        assertTrue("getAllByName(null): no results", addresses.length > 0);
        for (int i = 0; i < addresses.length; i++) {
            InetAddress address = addresses[i];
            assertTrue("Assert 1: getAllByName(null): " + address +
                    " is not loopback", address.isLoopbackAddress());
        }
        try {
            InetAddress.getAllByName("unknown.host");
            fail("UnknownHostException was not thrown.");
        } catch(UnknownHostException uhe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getByName",
        args = {java.lang.String.class}
    )
    public void test_getByNameLjava_lang_String() throws Exception {
        InetAddress ia2 = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        InetAddress i = InetAddress.getByName("1.2.3");
        assertEquals("1.2.0.3",i.getHostAddress());
        i = InetAddress.getByName("1.2");
        assertEquals("1.0.0.2",i.getHostAddress());
        i = InetAddress.getByName(String.valueOf(0xffffffffL));
        assertEquals("255.255.255.255",i.getHostAddress());
        class MockSecurityManager extends SecurityManager {        
            public void checkPermission(Permission permission) {
                if (permission.getName().equals("setSecurityManager")){
                    return;
                }
                if (permission.getName().equals("3d.com")){
                    throw new SecurityException();
                }
                super.checkPermission(permission);
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
                InetAddress.getByName("google.com");
                fail("SecurityException was not thrown.");
            } catch (SecurityException ex) {
            } catch (Exception ex) {
                fail("getByName threw wrong exception : " + ex.getMessage());
            }
        } finally {
            System.setSecurityManager(oldman);
        }
        try {
            InetAddress.getByName("0.0.0.0.0");
            fail("UnknownHostException was not thrown.");
        } catch(UnknownHostException ue) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHostAddress",
        args = {}
    )
    public void test_getHostAddress() {
        try {
            InetAddress ia2 = InetAddress
                    .getByName(Support_Configuration.InetTestAddress);
            assertTrue("getHostAddress returned incorrect result: "
                    + ia2.getHostAddress() + " != "
                    + Support_Configuration.InetTestIP, ia2.getHostAddress()
                    .equals(Support_Configuration.InetTestIP));
        } catch (Exception e) {
            fail("Exception during getHostAddress test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHostName",
        args = {}
    )
    public void test_getHostName() throws Exception {
        InetAddress ia = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        SecurityManager oldman = System.getSecurityManager();
        try {
            String exp = Support_Configuration.InetTestIP;
            System.setSecurityManager(new MockSecurityManager());
            ia = InetAddress.getByName(exp);
            String ans = ia.getHostName();
        } finally {
            System.setSecurityManager(oldman);
        }
        System.setProperty("networkaddress.cache.ttl", "0");
        InetAddress lookup1 = InetAddress
                .getByName(Support_Configuration.InetTestAddress);
        assertTrue(lookup1 + " expected "
                + Support_Configuration.InetTestIP,
                Support_Configuration.InetTestIP.equals(lookup1
                        .getHostAddress()));
        InetAddress lookup2 = InetAddress
                .getByName(Support_Configuration.InetTestAddress2);
        assertTrue(lookup2 + " expected "
                + Support_Configuration.InetTestIP2,
                Support_Configuration.InetTestIP2.equals(lookup2
                        .getHostAddress()));
        threadsafeTestThread thread1 = new threadsafeTestThread("1",
                lookup1.getHostName(), lookup1, 0);
        threadsafeTestThread thread2 = new threadsafeTestThread("2",
                lookup2.getHostName(), lookup2, 0);
        threadsafeTestThread thread3 = new threadsafeTestThread("3",
                lookup1.getHostAddress(), lookup1, 1);
        threadsafeTestThread thread4 = new threadsafeTestThread("4",
                lookup2.getHostAddress(), lookup2, 1);
        threadedTestSucceeded = true;
        synchronized (someoneDone) {
            thread1.start();
            thread2.start();
            thread3.start();
            thread4.start();
        }
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "UnknownHostException should be thrown if no IP address for the host could be found.",
        method = "getLocalHost",
        args = {}
    )
    public void test_getLocalHost() {
        try {
            DatagramSocket dg = new DatagramSocket(0, InetAddress
                    .getLocalHost());
            assertTrue("Incorrect host returned", InetAddress.getLocalHost()
                    .equals(dg.getLocalAddress()));
            dg.close();
        } catch (Exception e) {
            fail("Exception during getLocalHost test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    int getHashCode(String literal) {
        InetAddress host = null;
        try {
            host = InetAddress.getByName(literal);
        } catch(UnknownHostException e) {
            fail("Exception during hashCode test : " + e.getMessage());
        }
        return host.hashCode();
    }
    public void test_hashCode() {
        int hashCode = getHashCode(Support_Configuration.InetTestIP);
        int ip6HashCode = getHashCode(Support_Configuration.InetTestIP6);
        int ip6LOHashCode = getHashCode(Support_Configuration.InetTestIP6LO);
        assertFalse("Hash collision", hashCode == ip6HashCode);
        assertFalse("Hash collision", ip6HashCode == ip6LOHashCode);
        assertFalse("Hash collision", hashCode == ip6LOHashCode);
        assertFalse("Hash collision", ip6LOHashCode == 0);
        assertFalse("Hash collision", ip6LOHashCode == 1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMulticastAddress",
        args = {}
    )
    public void test_isMulticastAddress() {
        try {
            InetAddress ia1 = InetAddress.getByName("ff02::1");
            assertTrue("isMulticastAddress returned incorrect result", ia1
                    .isMulticastAddress());
            InetAddress ia2 = InetAddress.getByName("239.255.255.255");
            assertTrue("isMulticastAddress returned incorrect result", ia2
                    .isMulticastAddress());
            InetAddress ia3 = InetAddress.getByName("fefb::");
            assertFalse("isMulticastAddress returned incorrect result", ia3
                    .isMulticastAddress());
            InetAddress ia4 = InetAddress.getByName("10.0.0.1");
            assertFalse("isMulticastAddress returned incorrect result", ia4
                    .isMulticastAddress());
        } catch (Exception e) {
            fail("Exception during isMulticastAddress test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() throws Exception {
        InetAddress ia2 = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        assertEquals("/" + Support_Configuration.InetTestIP, ia2.toString());
        InetAddress addr = InetAddress.getByName("localhost");
        assertEquals("Assert 0: wrong string from name", "localhost/127.0.0.1", addr.toString());
        InetAddress addr2 = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        assertEquals("Assert 1: wrong string from address", "/127.0.0.1", addr2.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getByAddress",
        args = {java.lang.String.class, byte[].class}
    )
    public void test_getByAddressLjava_lang_String$B() {
        byte ipAddress[] = { 127, 0, 0, 1 };
        String addressStr = "::1";
        try {
            InetAddress addr = InetAddress.getByAddress(addressStr, ipAddress);
            addr = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException e) {
            fail("Unexpected problem creating IP Address "
                    + ipAddress.length);
        }
        byte ipAddress2[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 127, 0, 0,
                1 };
        addressStr = "::1";
        try {
            InetAddress addr = InetAddress.getByAddress(addressStr, ipAddress2);
            addr = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException e) {
            fail("Unexpected problem creating IP Address "
                    + ipAddress.length);
        }
        try {
            InetAddress addr = InetAddress.getByAddress(addressStr, 
                    new byte [] {0, 0, 0, 0, 0});
            fail("UnknownHostException was thrown.");
        } catch(UnknownHostException uhe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCanonicalHostName",
        args = {}
    )
    public void test_getCanonicalHostName() throws Exception {
        InetAddress theAddress = null;
        theAddress = InetAddress.getLocalHost();
        assertTrue("getCanonicalHostName returned a zero length string ",
                theAddress.getCanonicalHostName().length() != 0);
        assertTrue("getCanonicalHostName returned an empty string ",
                !theAddress.equals(""));
        InetAddress ia = InetAddress
                .getByName(Support_Configuration.InetTestIP);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException checking missed (if network error occurs).",
        method = "isReachable",
        args = {int.class}
    )
    public void test_isReachableI() throws Exception {
        InetAddress ia = Inet4Address.getByName("127.0.0.1");
        assertTrue(ia.isReachable(10000));
        ia = Inet4Address.getByName("127.0.0.1");
        try {
            ia.isReachable(-1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "IOException checking missed (if network error occurs).",
        method = "isReachable",
        args = {java.net.NetworkInterface.class, int.class, int.class}
    )
    @BrokenTest("Depends on external network address and shows different" +
            "behavior with WLAN and 3G networks")
    public void test_isReachableLjava_net_NetworkInterfaceII() throws Exception {
        InetAddress ia = Inet4Address.getByName("127.0.0.1");
        assertTrue(ia.isReachable(null, 0, 10000));
        ia = Inet4Address.getByName("127.0.0.1");
        try {
            ia.isReachable(null, -1, 10000);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ia.isReachable(null, 0, -1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ia.isReachable(null, -1, -1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        ia = Inet4Address.getByName("1.1.1.1");
        assertFalse(ia.isReachable(1000));
        assertFalse(ia.isReachable(null, 0, 1000));
        ia = InetAddress.getByName("localhost"); 
        Enumeration<NetworkInterface> nif = NetworkInterface.getNetworkInterfaces();
        NetworkInterface netif;
        while(nif.hasMoreElements()) {
            netif = nif.nextElement();
            ia.isReachable(netif, 10, 1000);
        }
    } 
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            InetAddress initAddr = (InetAddress) initial;
            InetAddress desrAddr = (InetAddress) deserialized;
            byte[] iaAddresss = initAddr.getAddress();
            byte[] deIAAddresss = desrAddr.getAddress();
            for (int i = 0; i < iaAddresss.length; i++) {
                assertEquals(iaAddresss[i], deIAAddresss[i]);
            }
            assertEquals(initAddr.getHostName(), desrAddr.getHostName());
        }
    };
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "Regeression test. Functional test.",
        method = "isReachable",
        args = {java.net.NetworkInterface.class, int.class, int.class}
    )
    public void test_isReachableLjava_net_NetworkInterfaceII_loopbackInterface() throws IOException {
        final int TTL = 20;
        final int TIME_OUT = 3000;
        NetworkInterface loopbackInterface = null;
        ArrayList<InetAddress> localAddresses = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                .getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface
                    .getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress()) {
                    loopbackInterface = networkInterface;
                } else {
                    localAddresses.add(address);
                }
            }
        }
        if (null != loopbackInterface) {
            for (InetAddress destAddress : localAddresses) {
                assertTrue(destAddress.isReachable(loopbackInterface, TTL, TIME_OUT));
            }
        }
        InetAddress destAddress = InetAddress.getByName("www.google.com");
        assertFalse(destAddress.isReachable(loopbackInterface, TTL, TIME_OUT));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(InetAddress.getByName("localhost"),
                COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                InetAddress.getByName("localhost"), COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getByAddress",
        args = {byte[].class}
    )
    public void test_getByAddress() {
        byte ipAddress[] = { 127, 0, 0, 1 };
        try {
            InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException e) {
            fail("Unexpected problem creating IP Address "
                    + ipAddress.length);
        }
        byte ipAddress2[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 127, 0, 0,
                1 };
        try {
            InetAddress.getByAddress(ipAddress2);
        } catch (UnknownHostException e) {
            fail("Unexpected problem creating IP Address "
                    + ipAddress.length);
        }
        try {
            InetAddress.getByAddress(null);
            fail("Assert 0: UnknownHostException must be thrown");
        } catch (UnknownHostException e) {
        }
        try {
            byte [] byteArray = new byte[] {};
            InetAddress.getByAddress(byteArray);
            fail("Assert 1: UnknownHostException must be thrown");
        } catch (UnknownHostException e) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAnyLocalAddress",
        args = {}
    )
    public void test_isAnyLocalAddress() throws Exception {
        byte [] ipAddress1 = { 127, 42, 42, 42 };
        InetAddress ia1 = InetAddress.getByAddress(ipAddress1);
        assertFalse(ia1.isAnyLocalAddress());
        byte [] ipAddress2 = { 0, 0, 0, 0 };
        InetAddress ia2 = InetAddress.getByAddress(ipAddress2);
        assertTrue(ia2.isAnyLocalAddress());        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLinkLocalAddress",
        args = {}
    )
    public void test_isLinkLocalAddress() throws Exception {
        String addrName = "FE80::0";
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue(
                "IPv6 link local address " + addrName + " not detected.",
                addr.isLinkLocalAddress());
        addrName = "FEBF::FFFF:FFFF:FFFF:FFFF";
        addr = Inet6Address.getByName(addrName);
        assertTrue(
                "IPv6 link local address " + addrName + " not detected.",
                addr.isLinkLocalAddress());
        addrName = "FEC0::1";
        addr = Inet6Address.getByName(addrName);
        assertTrue("IPv6 address " + addrName
                + " detected incorrectly as a link local address.", !addr
                .isLinkLocalAddress()); 
        addrName = "42.42.42.42";
        addr = Inet4Address.getByName(addrName);
        assertTrue("IPv4 address " + addrName
                + " incorrectly reporting as a link local address.", !addr
                .isLinkLocalAddress());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLoopbackAddress",
        args = {}
    )
    public void test_isLoopbackAddress() throws Exception {
        String addrName = "127.0.0.0";
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue("Loopback address " + addrName + " not detected.", addr
                .isLoopbackAddress());
        addrName = "127.42.42.42"; 
        addr = InetAddress.getByName(addrName);
        assertTrue("Loopback address " + addrName + " not detected.", addr
                .isLoopbackAddress());
        addrName = "42.42.42.42"; 
        addr = Inet4Address.getByName(addrName);
        assertTrue("Address incorrectly " + addrName
                + " detected as a loopback address.", !addr
                .isLoopbackAddress());
        addrName = "::FFFF:127.42.42.42";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv4-compatible IPv6 loopback address " + addrName
                + " not detected.", addr.isLoopbackAddress());
        addrName = "::FFFF:42.42.42.42";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv4-compatible IPv6 address incorrectly " + addrName
                + " detected as a loopback address.", !addr
                .isLoopbackAddress());        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCGlobal",
        args = {}
    )
    public void test_isMCGlobal() throws Exception {
        String addrName = "224.0.0.255"; 
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue("IPv4 link-local multicast address " + addrName
                + " incorrectly identified as a global multicast address.",
                !addr.isMCGlobal());
        addrName = "224.0.1.0"; 
        addr = Inet4Address.getByName(addrName);
        assertTrue("IPv4 global multicast address " + addrName
                + " not identified as a global multicast address.", addr
                .isMCGlobal());
        addrName = "FFFE:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 global multicast address " + addrName
                + " not detected.", addr.isMCGlobal());
        addrName = "FF08:42:42:42:42:42:42:42";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 mulitcast organizational " + addrName
                + " incorrectly indicated as a global address.", !addr
                .isMCGlobal());        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCLinkLocal",
        args = {}
    )
    public void test_isMCLinkLocal() throws Exception {
        String addrName = "224.0.0.255";
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue("IPv4 link-local multicast address " + addrName
                + " not identified as a link-local multicast address.",
                addr.isMCLinkLocal());
        addrName = "224.0.1.0";
        addr = InetAddress.getByName(addrName);
        assertTrue(
                "IPv4 global multicast address "
                        + addrName
                        + " incorrectly identified as a link-local " +
                                "multicast address.",
                !addr.isMCLinkLocal());    
        addrName = "FFF2:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 link local multicast address " + addrName
                + " not detected.", addr.isMCLinkLocal());
        addrName = "FF08:42:42:42:42:42:42:42";
        addr = InetAddress.getByName(addrName);
        assertTrue(
                "IPv6 organization multicast address "
                        + addrName
                        + " incorrectly indicated as a link-local " +
                                "mulitcast address.",
                !addr.isMCLinkLocal());        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCNodeLocal",
        args = {}
    )
    public void test_isMCNodeLocal() throws Exception {
        String addrName = "224.42.42.42"; 
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue(
                "IPv4 multicast address "
                        + addrName
                        + " incorrectly identified as a node-local " +
                                "multicast address.",
                !addr.isMCNodeLocal());
        addrName = "FFF1:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 node-local multicast address " + addrName
                + " not detected.", addr.isMCNodeLocal());
        addrName = "FF08:42:42:42:42:42:42:42";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 mulitcast organizational address " + addrName
                + " incorrectly indicated as a node-local address.", !addr
                .isMCNodeLocal());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCOrgLocal",
        args = {}
    )
    public void test_isMCOrgLocal() throws Exception {
        String addrName = "239.252.0.0"; 
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue(
                "IPv4 site-local multicast address "
                        + addrName
                        + " incorrectly identified as a org-local multicast address.",
                !addr.isMCOrgLocal());
        addrName = "239.192.0.0"; 
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv4 org-local multicast address " + addrName
                + " not identified as a org-local multicast address.", addr
                .isMCOrgLocal());
        addrName = "FFF8:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 organization-local multicast address " + addrName
                + " not detected.", addr.isMCOrgLocal());
        addrName = "FF0E:42:42:42:42:42:42:42";
        addr = InetAddress.getByName(addrName);
        assertTrue(
                "IPv6 global multicast address "
                        + addrName
                        + " incorrectly indicated as an organization-local mulitcast address.",
                !addr.isMCOrgLocal());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCSiteLocal",
        args = {}
    )
    public void test_isMCSiteLocal() throws Exception {
        String addrName = "FFF5:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 site-local multicast address " + addrName
                + " not detected.", addr.isMCSiteLocal());
        addrName = "FF08:42:42:42:42:42:42:42";
        addr = Inet6Address.getByName(addrName);
        assertTrue(
                "IPv6 organization multicast address "
                        + addrName
                        + " incorrectly indicated as a site-local " +
                                "mulitcast address.",
                !addr.isMCSiteLocal());
        addrName = "239.0.0.0"; 
        addr = Inet4Address.getByName(addrName);
        assertTrue(
                "IPv4 reserved multicast address "
                        + addrName
                        + " incorrectly identified as a site-local " +
                                "multicast address.",
                !addr.isMCSiteLocal());
        addrName = "239.255.0.0";
        addr = Inet4Address.getByName(addrName);
        assertTrue("IPv4 site-local multicast address " + addrName
                + " not identified as a site-local multicast address.",
                addr.isMCSiteLocal());        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isSiteLocalAddress",
        args = {}
    )
    public void test_isSiteLocalAddress() throws Exception {
        String addrName = "42.42.42.42";
        InetAddress addr = InetAddress.getByName(addrName);
        assertTrue("IPv4 address " + addrName
                + " incorrectly reporting as a site local address.", !addr
                .isSiteLocalAddress());
        addrName = "FEFF::FFFF:FFFF:FFFF:FFFF:FFFF";
        addr = InetAddress.getByName(addrName);
        assertTrue(
                "IPv6 site local address " + addrName + " not detected.",
                addr.isSiteLocalAddress());
        addrName = "FEBF::FFFF:FFFF:FFFF:FFFF:FFFF";
        addr = InetAddress.getByName(addrName);
        assertTrue("IPv6 address " + addrName
                + " detected incorrectly as a site local address.", !addr
                .isSiteLocalAddress());
    }
    class MockSecurityManager extends SecurityManager {        
        public void checkPermission(Permission permission) {
            if (permission.getName().equals("setSecurityManager")){
                return;
            }
            if (permission.getName().equals("3d.com")){
                throw new SecurityException();
            }
            super.checkPermission(permission);
        }
    }
}

@TestTargetClass(Inet6Address.class) 
public class Inet6AddressTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMulticastAddress",
        args = {}
    )
    public void test_isMulticastAddress() {
        String addrName = "";
        InetAddress addr = null;
        try {
            addrName = "FFFF::42:42"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("Multicast address " + addrName + " not detected.", addr
                    .isMulticastAddress());
            addrName = "42::42:42"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("Non multicast address " + addrName
                    + " reporting as a multicast address.", !addr
                    .isMulticastAddress());
            addrName = "::224.42.42.42"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 compatable address " + addrName
                    + " reported incorrectly as multicast.", !addr
                    .isMulticastAddress());
            addrName = "::42.42.42.42"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 compatable address " + addrName
                    + " reported incorrectly as multicast.", !addr
                    .isMulticastAddress());
            addrName = "::FFFF:224.42.42.42"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-mapped IPv6 multicast address " + addrName
                    + " not detected.", addr.isMulticastAddress());
            addrName = "::FFFF:42.42.42.42"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-mapped IPv6 non-multicast address " + addrName
                    + " reporting as a multicast address.", !addr
                    .isMulticastAddress());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAnyLocalAddress",
        args = {}
    )
    public void test_isAnyLocalAddress() {
        String addrName = "";
        InetAddress addr = null;
        try {
            addrName = "::0"; 
            addr = InetAddress.getByName(addrName);
            assertTrue(
                    "The unspecified (also known as wildcard and any local address) "
                            + addrName + " not detected.", addr
                            .isAnyLocalAddress());
            addrName = "::"; 
            addr = InetAddress.getByName(addrName);
            assertTrue(
                    "The unspecified (also known as wildcard and any local address) "
                            + addrName + " not detected.", addr
                            .isAnyLocalAddress());
            addrName = "::1"; 
            addr = InetAddress.getByName(addrName);
            assertTrue("The addresses " + addrName
                    + " incorrectly reporting an the unspecified address.",
                    !addr.isAnyLocalAddress());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLoopbackAddress",
        args = {}
    )
    public void test_isLoopbackAddress() {
        String addrName = "";
        try {
            addrName = "::1";
            InetAddress addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 loopback address " + addrName + " not detected.",
                    addr.isLoopbackAddress());
            addrName = "::2";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 address incorrectly " + addrName
                    + " detected as a loopback address.", !addr
                    .isLoopbackAddress());
            addrName = "42:42::42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 address incorrectly " + addrName
                    + " detected as a loopback address.", !addr
                    .isLoopbackAddress());
            addrName = "::127.0.0.0"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-compatible IPv6 address " + addrName
                    + " detected incorrectly as a loopback.", !addr
                    .isLoopbackAddress());
            addrName = "::127.42.42.42"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-compatible IPv6 address " + addrName
                    + " detected incorrectly as a loopback.", !addr
                    .isLoopbackAddress());
            addrName = "::42.42.42.42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-compatible IPv6 address " + addrName
                    + " detected incorrectly as a loopback.", !addr
                    .isLoopbackAddress());
            addrName = "::FFFF:127.0.0.0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-compatible IPv6 loopback address " + addrName
                    + " not detected.", addr.isLoopbackAddress());
            addrName = "::FFFF:127.42.42.42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-compatible IPv6 loopback address " + addrName
                    + " not detected.", addr.isLoopbackAddress());
            addrName = "::FFFF:42.42.42.42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4-compatible IPv6 address incorrectly " + addrName
                    + " detected as a loopback address.", !addr
                    .isLoopbackAddress());
        } catch (UnknownHostException e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLinkLocalAddress",
        args = {}
    )
    public void test_isLinkLocalAddress() {
        String addrName = "";
        try {
            addrName = "FE80::0";
            InetAddress addr = Inet6Address.getByName(addrName);
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
            addrName = "FD80::1:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 address " + addrName
                    + " detected incorrectly as a link local address.", !addr
                    .isLinkLocalAddress());
            addrName = "FE7F::FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 address " + addrName
                    + " detected incorrectly as a link local address.", !addr
                    .isLinkLocalAddress());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isSiteLocalAddress",
        args = {}
    )
    public void test_isSiteLocalAddress() {
        String addrName = "";
        try {
            addrName = "FEC0::0";
            InetAddress addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 site local address " + addrName + " not detected.",
                    addr.isSiteLocalAddress());
            addrName = "FEFF::FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 site local address " + addrName + " not detected.",
                    addr.isSiteLocalAddress());
            addrName = "FEBF::FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 address " + addrName
                    + " detected incorrectly as a site local address.", !addr
                    .isSiteLocalAddress());
            addrName = "FFC0::0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 address " + addrName
                    + " detected incorrectly as a site local address.", !addr
                    .isSiteLocalAddress());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCGlobal",
        args = {}
    )
    public void test_isMCGlobal() {
        String addrName = "";
        try {
            addrName = "FF0E::0";
            InetAddress addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 global mutlicast address " + addrName
                    + " not detected.", addr.isMCGlobal());
            addrName = "FF0E:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 global multicast address " + addrName
                    + " not detected.", addr.isMCGlobal());
            addrName = "FFFE::0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 global mutlicast address " + addrName
                    + " not detected.", addr.isMCGlobal());
            addrName = "FFFE:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 global multicast address " + addrName
                    + " not detected.", addr.isMCGlobal());
            addrName = "FF08:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast organizational " + addrName
                    + " incorrectly indicated as a global address.", !addr
                    .isMCGlobal());
            addrName = "FF05:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast site address " + addrName
                    + " incorrectly indicated as a global address.", !addr
                    .isMCGlobal());
            addrName = "FF02:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast link address " + addrName
                    + " incorrectly indicated as a global address.", !addr
                    .isMCGlobal());
            addrName = "FF01:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast node address " + addrName
                    + " incorrectly indicated as a global address.", !addr
                    .isMCGlobal());
            addrName = "::FFFF:224.0.1.0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 global multicast address " + addrName
                    + " not identified as a global multicast address.", addr
                    .isMCGlobal());
            addrName = "::FFFF:238.255.255.255";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 global multicast address " + addrName
                    + " not identified as a global multicast address.", addr
                    .isMCGlobal());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCNodeLocal",
        args = {}
    )
    public void test_isMCNodeLocal() {
        String addrName = "";
        try {
            addrName = "FF01::0";
            InetAddress addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 node-local mutlicast address " + addrName
                    + " not detected.", addr.isMCNodeLocal());
            addrName = "FF01:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 node-local multicast address " + addrName
                    + " not detected.", addr.isMCNodeLocal());
            addrName = "FFF1::0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 node-local mutlicast address " + addrName
                    + " not detected.", addr.isMCNodeLocal());
            addrName = "FFF1:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 node-local multicast address " + addrName
                    + " not detected.", addr.isMCNodeLocal());
            addrName = "FF08:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast organizational address " + addrName
                    + " incorrectly indicated as a node-local address.", !addr
                    .isMCNodeLocal());
            addrName = "FF05:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast site address " + addrName
                    + " incorrectly indicated as a node-local address.", !addr
                    .isMCNodeLocal());
            addrName = "FF02:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast link address " + addrName
                    + " incorrectly indicated as a node-local address.", !addr
                    .isMCNodeLocal());
            addrName = "FF0E:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 mulitcast node address " + addrName
                    + " incorrectly indicated as a node-local address.", !addr
                    .isMCNodeLocal());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCLinkLocal",
        args = {}
    )
    public void test_isMCLinkLocal() {
        String addrName = "";
        try {
            addrName = "FF02::0";
            InetAddress addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 link local multicast address " + addrName
                    + " not detected.", addr.isMCLinkLocal());
            addrName = "FF02:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 link local multicast address " + addrName
                    + " not detected.", addr.isMCLinkLocal());
            addrName = "FFF2::0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 link local multicast address " + addrName
                    + " not detected.", addr.isMCLinkLocal());
            addrName = "FFF2:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 link local multicast address " + addrName
                    + " not detected.", addr.isMCLinkLocal());
            addrName = "FF08:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 organization multicast address "
                            + addrName
                            + " incorrectly indicated as a link-local mulitcast address.",
                    !addr.isMCLinkLocal());
            addrName = "FF05:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 site-local mulitcast address "
                            + addrName
                            + " incorrectly indicated as a link-local mulitcast address.",
                    !addr.isMCLinkLocal());
            addrName = "FF0E:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 global multicast address "
                            + addrName
                            + " incorrectly indicated as a link-local mulitcast address.",
                    !addr.isMCLinkLocal());
            addrName = "FF01:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 mulitcast node address "
                            + addrName
                            + " incorrectly indicated as a link-local mulitcast address.",
                    !addr.isMCLinkLocal());
            addrName = "::FFFF:224.0.0.0"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 link-local multicast address " + addrName
                    + " not identified as a link-local multicast address.",
                    addr.isMCLinkLocal());
            addrName = "::FFFF:224.0.0.255"; 
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 link-local multicast address " + addrName
                    + " not identified as a link-local multicast address.",
                    addr.isMCLinkLocal());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCSiteLocal",
        args = {}
    )
    public void test_isMCSiteLocal() {
        String addrName = "";
        try {
            addrName = "FF05::0";
            InetAddress addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 site-local mutlicast address " + addrName
                    + " not detected.", addr.isMCSiteLocal());
            addrName = "FF05:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 site-local multicast address " + addrName
                    + " not detected.", addr.isMCSiteLocal());
            addrName = "FFF5::0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 site-local mutlicast address " + addrName
                    + " not detected.", addr.isMCSiteLocal());
            addrName = "FFF5:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 site-local multicast address " + addrName
                    + " not detected.", addr.isMCSiteLocal());
            addrName = "FF08:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 organization multicast address "
                            + addrName
                            + " incorrectly indicated as a site-local mulitcast address.",
                    !addr.isMCSiteLocal());
            addrName = "FF0E:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 global mulitcast address "
                            + addrName
                            + " incorrectly indicated as a site-local mulitcast address.",
                    !addr.isMCSiteLocal());
            addrName = "FF02:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 link-local multicast address "
                            + addrName
                            + " incorrectly indicated as a site-local mulitcast address.",
                    !addr.isMCSiteLocal());
            addrName = "FF01:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 mulitcast node address "
                            + addrName
                            + " incorrectly indicated as a site-local mulitcast address.",
                    !addr.isMCSiteLocal());
            addrName = "::FFFF:239.255.0.0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 site-local multicast address " + addrName
                    + " not identified as a site-local multicast address.",
                    addr.isMCSiteLocal());
            addrName = "::FFFF:239.255.255.255";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 site-local multicast address " + addrName
                    + " not identified as a site-local multicast address.",
                    addr.isMCSiteLocal());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isMCOrgLocal",
        args = {}
    )
    public void test_isMCOrgLocal() {
        String addrName = "";
        try {
            addrName = "FF08::0";
            InetAddress addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 organization-local mutlicast address " + addrName
                    + " not detected.", addr.isMCOrgLocal());
            addrName = "FF08:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 organization-local multicast address " + addrName
                    + " not detected.", addr.isMCOrgLocal());
            addrName = "FFF8::0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 organization-local mutlicast address " + addrName
                    + " not detected.", addr.isMCOrgLocal());
            addrName = "FFF8:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv6 organization-local multicast address " + addrName
                    + " not detected.", addr.isMCOrgLocal());
            addrName = "FF0E:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 global multicast address "
                            + addrName
                            + " incorrectly indicated as an organization-local mulitcast address.",
                    !addr.isMCOrgLocal());
            addrName = "FF05:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 site-local mulitcast address "
                            + addrName
                            + " incorrectly indicated as an organization-local mulitcast address.",
                    !addr.isMCOrgLocal());
            addrName = "FF02:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 link-local multicast address "
                            + addrName
                            + " incorrectly indicated as an organization-local mulitcast address.",
                    !addr.isMCOrgLocal());
            addrName = "FF01:42:42:42:42:42:42:42";
            addr = Inet6Address.getByName(addrName);
            assertTrue(
                    "IPv6 mulitcast node address "
                            + addrName
                            + " incorrectly indicated as an organization-local mulitcast address.",
                    !addr.isMCOrgLocal());
            addrName = "::FFFF:239.192.0.0";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 org-local multicast address " + addrName
                    + " not identified as a org-local multicast address.", addr
                    .isMCOrgLocal());
            addrName = "::FFFF:239.195.255.255";
            addr = Inet6Address.getByName(addrName);
            assertTrue("IPv4 org-local multicast address " + addrName
                    + " not identified as a org-local multicast address.", addr
                    .isMCOrgLocal());
        } catch (Exception e) {
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isIPv4CompatibleAddress",
        args = {}
    )
    public void test_isIPv4CompatibleAddress() {
        String addrName = "";
        Inet6Address addr = null;
        try {
            addrName = "FFFF::42:42"; 
            addr = (Inet6Address) InetAddress.getByName(addrName);
            assertTrue("A non-compatable IPv6 address " + addrName
                    + " incorrectly identified as a IPv4 compatable address.",
                    !addr.isIPv4CompatibleAddress());
            addrName = "::0.0.0.0";
            addr = (Inet6Address) InetAddress.getByName(addrName);
            assertTrue("IPv4 compatable address " + addrName
                    + " not detected correctly.", addr
                    .isIPv4CompatibleAddress());
            addrName = "::255.255.255.255"; 
            addr = (Inet6Address) InetAddress.getByName(addrName);
            assertTrue("IPv4 compatable address " + addrName
                    + " not detected correctly.", addr
                    .isIPv4CompatibleAddress());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unknown address : " + addrName);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getByName",
        args = {java.lang.String.class}
    )
    public void test_getByNameLjava_lang_String() throws Exception {
        String validIPAddresses[] = { "::1.2.3.4", "::", "::", "1::0", "1::",
                "::1", "0", 
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF",
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:255.255.255.255",
                "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0.0.0.0" };
        String invalidIPAddresses[] = { "FFFF:FFFF" };
        for (int i = 0; i < validIPAddresses.length; i++) {
            InetAddress.getByName(validIPAddresses[i]);
            InetAddress.getByName(validIPAddresses[i]);
            if (!validIPAddresses[i].equals("0")) {
                String tempIPAddress = "[" + validIPAddresses[i] + "]";
                InetAddress.getByName(tempIPAddress);
            }
        }
        for (int i = 0; i < invalidIPAddresses.length; i++) {
            try {
                InetAddress.getByName(invalidIPAddresses[i]);
                fail("Invalid IP address incorrectly recognized as valid: "
                        + invalidIPAddresses[i]);
            } catch (Exception e) {
            }
            try {
                InetAddress.getByName(invalidIPAddresses[i]);
                fail("Invalid IP address incorrectly recognized as valid: "
                        + invalidIPAddresses[i]);
            } catch (Exception e) {
            }
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
        SecurityManager oldman = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            boolean exception = false;
            try {
                Inet6Address.getByName("3d.com");
                fail("SecurityException was not thrown.");                
            } catch (SecurityException ex) {
            } catch (Exception ex) {
                fail("getByName threw wrong exception : " + ex.getMessage());
            }
        } finally {
            System.setSecurityManager(oldman);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getByAddress",
        args = {java.lang.String.class, byte[].class, int.class}
    )
    public void test_getByAddressLString$BI() throws UnknownHostException{
        try {
            Inet6Address.getByAddress("123", null, 0);
            fail("should throw UnknownHostException");
        } catch (UnknownHostException uhe) {
        }
        byte[] addr1 = { (byte) 127, 0, 0, 1 };
        try {
            Inet6Address.getByAddress("123", addr1, 0);
            fail("should throw UnknownHostException");
        } catch (UnknownHostException uhe) {
        }
        byte[] addr2 = { (byte) 0xFE, (byte) 0x80, 0, 0, 0, 0, 0, 0, 0x02,
                0x11, 0x25, (byte) 0xFF, (byte) 0xFE, (byte) 0xF8, (byte) 0x7C,
                (byte) 0xB2 };
        Inet6Address.getByAddress("123", addr2, 3);
        Inet6Address.getByAddress("123", addr2, 0);
        Inet6Address.getByAddress("123", addr2, -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getByAddress",
        args = {java.lang.String.class, byte[].class, java.net.NetworkInterface.class}
    )
    public void test_getByAddressLString$BLNetworkInterface()
            throws UnknownHostException {
        NetworkInterface nif = null;
        try {
            Inet6Address.getByAddress("123", null, nif);
            fail("should throw UnknownHostException");
        } catch (UnknownHostException uhe) {
        }
        byte[] addr1 = { (byte) 127, 0, 0, 1 };
        try {
            Inet6Address.getByAddress("123", addr1, nif);
            fail("should throw UnknownHostException");
        } catch (UnknownHostException uhe) {
        }
        byte[] addr2 = { (byte) 0xFE, (byte) 0x80, 0, 0, 0, 0, 0, 0, 0x02,
                0x11, 0x25, (byte) 0xFF, (byte) 0xFE, (byte) 0xF8, (byte)
                0x7C, (byte) 0xB2 };
        Inet6Address.getByAddress("123", addr2, nif);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getScopeId",
        args = {}
    )
    public void test_getScopeID() throws UnknownHostException {
        Inet6Address v6ia;
        byte[] addr = { (byte) 0xFE, (byte) 0x80, 0, 0, 0, 0, 0, 0, 0x02, 0x11,
                0x25, (byte) 0xFF, (byte) 0xFE, (byte) 0xF8, (byte) 0x7C,
                (byte) 0xB2 };
        v6ia = Inet6Address.getByAddress("123", addr, 3);
        assertEquals(3, v6ia.getScopeId());
        v6ia = Inet6Address.getByAddress("123", addr, 0);
        assertEquals(0, v6ia.getScopeId());
        v6ia = Inet6Address.getByAddress("123", addr, -1);
        assertEquals(0, v6ia.getScopeId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getScopedInterface",
        args = {}
    )
    public void test_getScopedInterface() throws UnknownHostException {
        byte[] addr = { (byte) 0xFE, (byte) 0x80, (byte) 0x09, (byte) 0xb5,
                (byte) 0x6b, (byte) 0xa4, 0, 0, 0, 0, 0, 0, (byte) 0x09,
                (byte) 0xb5, (byte) 0x6b, (byte) 0xa4 };
        Inet6Address v6Addr;
        v6Addr = Inet6Address.getByAddress("123", addr, null);
        assertNull(v6Addr.getScopedInterface());
    }
    int bytesToInt(byte bytes[], int start) {
        int byteMask = 255;
        int value = ((bytes[start + 3] & byteMask))
                | ((bytes[start + 2] & byteMask) << 8)
                | ((bytes[start + 1] & byteMask) << 16)
                | ((bytes[start] & byteMask) << 24);
        return value;
    }
    String byteArrayToHexString(byte bytes[], boolean leadingZeros) {
        String fullString = "";
        int times = bytes.length / 4;
        int intArray[] = new int[times];
        for (int i = 0; i < times; i++) {
            intArray[i] = bytesToInt(bytes, i * 4);
        }
        return intArrayToHexString(intArray, leadingZeros);
    }
    void intToBytes(int value, byte bytes[], int start) {
        int byteMask = 255;
        bytes[start + 3] = (byte) (value & byteMask);
        bytes[start + 2] = (byte) ((value >> 8) & byteMask);
        bytes[start + 1] = (byte) ((value >> 16) & byteMask);
        bytes[start] = (byte) ((value >> 24) & byteMask);
    }
    String intArrayToHexString(int ints[], boolean leadingZeros) {
        String fullString = "";
        String tempString;
        int intsLength = ints.length;
        for (int i = 0; i < intsLength; i++) {
            tempString = Integer.toHexString(ints[i]);
            while (tempString.length() < 4 && leadingZeros) {
                tempString = "0" + tempString;
            }
            if (i + 1 < intsLength) {
                tempString += ":";
            }
            fullString += tempString;
        }
        return fullString.toUpperCase();
    }
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            Inet6Address initAddr = (Inet6Address) initial;
            Inet6Address desrAddr = (Inet6Address) deserialized;
            byte[] iaAddresss = initAddr.getAddress();
            byte[] deIAAddresss = desrAddr.getAddress();
            for (int i = 0; i < iaAddresss.length; i++) {
                assertEquals(iaAddresss[i], deIAAddresss[i]);
            }
            assertEquals(initAddr.getScopeId(), desrAddr.getScopeId());
            assertEquals(initAddr.getScopedInterface(), desrAddr
                    .getScopedInterface());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        byte[] localv6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        SerializationTest.verifySelf(InetAddress.getByAddress(localv6),
                COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        byte[] localv6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        Object[] addresses = { InetAddress.getByAddress(localv6),
                InetAddress.getByAddress(localv6) };
        SerializationTest.verifyGolden(this, addresses, COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() throws Exception {
        InetAddress addr = Inet6Address.getByName("239.191.255.255");
        assertTrue(addr.equals(addr));
        InetAddress addr1 = Inet6Address.getByName("127.0.0.1");
        InetAddress addr2 = Inet6Address.getByName("localhost");
        assertTrue(addr1.equals(addr2));
        assertFalse(addr.equals(addr1));
        InetAddress addr3 = Inet6Address.getByName("127.0.0");
        assertFalse(addr1.equals(addr3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHostAddress",
        args = {}
    )
    public void test_getHostAddress() throws Exception {
        InetAddress aAddr = Inet6Address.getByName("localhost");
        assertEquals("127.0.0.1", aAddr.getHostAddress());
        aAddr = Inet6Address.getByName("127.0.0.1");
        assertEquals("127.0.0.1", aAddr.getHostAddress());
        aAddr = Inet6Address.getByName("224.0.0.0");
        assertEquals("224.0.0.0", aAddr.getHostAddress());
        aAddr = Inet6Address.getByName("1");
        assertEquals("0.0.0.1", aAddr.getHostAddress());
        aAddr = Inet6Address.getByName("1.1");
        assertEquals("1.0.0.1", aAddr.getHostAddress());
        aAddr = Inet6Address.getByName("1.1.1");
        assertEquals("1.1.0.1", aAddr.getHostAddress());
        byte[] bAddr = { (byte) 0xFE, (byte) 0x80, 0, 0, 0, 0, 0, 0, 0x02, 0x11,
                0x25, (byte) 0xFF, (byte) 0xFE, (byte) 0xF8, (byte) 0x7C,
                (byte) 0xB2 };
        aAddr = Inet6Address.getByAddress(bAddr);
        String aString = aAddr.getHostAddress();
        assertTrue(aString.equals("fe80:0:0:0:211:25ff:fef8:7cb2") ||
                   aString.equals("fe80::211:25ff:fef8:7cb2"));
        byte[] cAddr = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        aAddr = Inet6Address.getByAddress(cAddr);
        assertEquals("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", aAddr.getHostAddress());
        byte[] dAddr = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        aAddr = Inet6Address.getByAddress(dAddr);
        aString = aAddr.getHostAddress();
        assertTrue(aString.equals("0:0:0:0:0:0:0:0") ||
                   aString.equals("::"));
        byte[] eAddr = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
                (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
                (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b,
                (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f};
        aAddr = Inet6Address.getByAddress(eAddr);
        assertEquals("1:203:405:607:809:a0b:c0d:e0f", aAddr.getHostAddress());
        byte[] fAddr = { (byte) 0x00, (byte) 0x10, (byte) 0x20, (byte) 0x30,
                (byte) 0x40, (byte) 0x50, (byte) 0x60, (byte) 0x70,
                (byte) 0x80, (byte) 0x90, (byte) 0xa0, (byte) 0xb0,
                (byte) 0xc0, (byte) 0xd0, (byte) 0xe0, (byte) 0xf0};
        aAddr = Inet6Address.getByAddress(fAddr);
        assertEquals("10:2030:4050:6070:8090:a0b0:c0d0:e0f0", aAddr.getHostAddress());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() throws Exception {
        InetAddress addr1 = Inet6Address.getByName("1.1");
        InetAddress addr2 = Inet6Address.getByName("1.1.1");
        assertFalse(addr1.hashCode() == addr2.hashCode());
        addr2 = Inet6Address.getByName("1.0.0.1");
        assertTrue(addr1.hashCode() == addr2.hashCode());
        addr1 = Inet6Address.getByName("127.0.0.1");
        addr2 = Inet6Address.getByName("localhost");
        assertTrue(addr1.hashCode() == addr2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() throws Exception {
        String validIPAddresses[] = { "::1.2.3.4", "::", "::", "1::0", "1::",
                "::1", "0", 
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF",
                "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:255.255.255.255",
                "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0.0.0.0" };
        String [] resultStrings = {"/0:0:0:0:0:0:102:304", "/0:0:0:0:0:0:0:0", 
                "/0:0:0:0:0:0:0:0", "/1:0:0:0:0:0:0:0", "/1:0:0:0:0:0:0:0", 
                "/0:0:0:0:0:0:0:1",
                "/0.0.0.0", "/ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", 
                "/ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", "/0:0:0:0:0:0:0:0", 
                "/0:0:0:0:0:0:0:0"};
        for(int i = 0; i < validIPAddresses.length; i++) {
            InetAddress ia = Inet6Address.getByName(validIPAddresses[i]);
            String result = ia.toString();
            assertNotNull(result);
        }
    }
}

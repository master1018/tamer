@TestTargetClass(NetworkInterface.class) 
public class NetworkInterfaceTest extends junit.framework.TestCase {
    boolean atLeastOneInterface = false;
    boolean atLeastTwoInterfaces = false;
    private NetworkInterface networkInterface1 = null;
    private NetworkInterface sameAsNetworkInterface1 = null;
    private NetworkInterface networkInterface2 = null;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        if (atLeastOneInterface) {
            assertNotNull("validate that non null name is returned",
                    networkInterface1.getName());
            assertFalse("validate that non-zero length name is generated",
                    networkInterface1.getName().equals(""));
        }
        if (atLeastTwoInterfaces) {
            assertFalse(
                    "Validate strings are different for different interfaces",
                    networkInterface1.getName().equals(
                            networkInterface2.getName()));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInetAddresses",
        args = {}
    )
    public void test_getInetAddresses() {
        class mySecurityManager extends SecurityManager {
            ArrayList disallowedNames = null;
            public mySecurityManager(ArrayList addresses) {
                disallowedNames = new ArrayList();
                for (int i = 0; i < addresses.size(); i++) {
                    disallowedNames.add(((InetAddress) addresses.get(i))
                            .getHostName());
                    disallowedNames.add(((InetAddress) addresses.get(i))
                            .getHostAddress());
                }
            }
            public void checkConnect(String host, int port) {
                if (host == null) {
                    throw new NullPointerException("host was null)");
                }
                for (int i = 0; i < disallowedNames.size(); i++) {
                    if (((String) disallowedNames.get(i)).equals(host)) {
                        throw new SecurityException("not allowed");
                    }
                }
            }
            public void checkPermission(Permission perm) {
            }
        }
        if (atLeastOneInterface) {
            Enumeration theAddresses = networkInterface1.getInetAddresses();
            if (theAddresses != null) {
                while (theAddresses.hasMoreElements()) {
                    InetAddress theAddress = (InetAddress) theAddresses
                            .nextElement();
                    assertTrue("validate that address is not null",
                            null != theAddress);
                }
            }
        }
        if (atLeastTwoInterfaces) {
            Enumeration theAddresses = networkInterface2.getInetAddresses();
            if (theAddresses != null) {
                while (theAddresses.hasMoreElements()) {
                    InetAddress theAddress = (InetAddress) theAddresses
                            .nextElement();
                    assertTrue("validate that address is not null",
                            null != theAddress);
                }
            }
        }
        if (atLeastOneInterface) {
            ArrayList okAddresses = new ArrayList();
            Enumeration addresses = networkInterface1.getInetAddresses();
            int index = 0;
            ArrayList notOkAddresses = new ArrayList();
            if (addresses != null) {
                while (addresses.hasMoreElements()) {
                    InetAddress theAddress = (InetAddress) addresses
                            .nextElement();
                    if (index != 0) {
                        okAddresses.add(theAddress);
                    } else {
                        notOkAddresses.add(theAddress);
                    }
                    index++;
                }
            }
            if (atLeastTwoInterfaces) {
                addresses = networkInterface2.getInetAddresses();
                index = 0;
                if (addresses != null) {
                    while (addresses.hasMoreElements()) {
                        InetAddress theAddress = (InetAddress) addresses
                                .nextElement();
                        if (index != 0) {
                            okAddresses.add(theAddress);
                        } else {
                            notOkAddresses.add(theAddress);
                        }
                        index++;
                    }
                }
            }
            System.setSecurityManager(new mySecurityManager(notOkAddresses));
            for (int i = 0; i < notOkAddresses.size(); i++) {
                Enumeration reducedAddresses = networkInterface1
                        .getInetAddresses();
                if (reducedAddresses != null) {
                    while (reducedAddresses.hasMoreElements()) {
                        InetAddress nextAddress = (InetAddress) reducedAddresses
                                .nextElement();
                        assertTrue(
                                "validate that address without permission is not returned",
                                !nextAddress.equals(notOkAddresses.get(i)));
                    }
                }
                if (atLeastTwoInterfaces) {
                    reducedAddresses = networkInterface2.getInetAddresses();
                    if (reducedAddresses != null) {
                        while (reducedAddresses.hasMoreElements()) {
                            InetAddress nextAddress = (InetAddress) reducedAddresses
                                    .nextElement();
                            assertTrue(
                                    "validate that address without permission is not returned",
                                    !nextAddress.equals(notOkAddresses.get(i)));
                        }
                    }
                }
            }
            for (int i = 0; i < okAddresses.size(); i++) {
                boolean addressReturned = false;
                Enumeration reducedAddresses = networkInterface1
                        .getInetAddresses();
                if (reducedAddresses != null) {
                    while (reducedAddresses.hasMoreElements()) {
                        InetAddress nextAddress = (InetAddress) reducedAddresses
                                .nextElement();
                        if (nextAddress.equals(okAddresses.get(i))) {
                            addressReturned = true;
                        }
                    }
                }
                if (atLeastTwoInterfaces) {
                    reducedAddresses = networkInterface2.getInetAddresses();
                    if (reducedAddresses != null) {
                        while (reducedAddresses.hasMoreElements()) {
                            InetAddress nextAddress = (InetAddress) reducedAddresses
                                    .nextElement();
                            if (nextAddress.equals(okAddresses.get(i))) {
                                addressReturned = true;
                            }
                        }
                    }
                }
                assertTrue("validate that address with permission is returned",
                        addressReturned);
            }
            for (int i = 0; i < notOkAddresses.size(); i++) {
                try {
                    assertNotNull(
                            "validate we cannot get the NetworkInterface with an address for which we have no privs",
                            NetworkInterface
                                    .getByInetAddress((InetAddress) notOkAddresses
                                            .get(i)));
                } catch (Exception e) {
                    fail("get NetworkInterface for address with no perm - exception");
                }
            }
            try {
                for (int i = 0; i < okAddresses.size(); i++) {
                    assertNotNull(
                            "validate we cannot get the NetworkInterface with an address fro which we have no privs",
                            NetworkInterface
                                    .getByInetAddress((InetAddress) okAddresses
                                            .get(i)));
                }
            } catch (Exception e) {
                fail("get NetworkInterface for address with perm - exception");
            }
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDisplayName",
        args = {}
    )
    public void test_getDisplayName() {
        if (atLeastOneInterface) {
            assertNotNull("validate that non null display name is returned",
                    networkInterface1.getDisplayName());
            assertFalse(
                    "validate that non-zero lengtj display name is generated",
                    networkInterface1.getDisplayName().equals(""));
        }
        if (atLeastTwoInterfaces) {
            assertFalse(
                    "Validate strings are different for different interfaces",
                    networkInterface1.getDisplayName().equals(
                            networkInterface2.getDisplayName()));
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SocketException checking missed.",
        method = "getByName",
        args = {java.lang.String.class}
    )
    public void test_getByNameLjava_lang_String() {
        try {
            assertNull("validate null handled ok",
                                   NetworkInterface.getByName(null));
            fail("getByName did not throw NullPointerException for null argument");
        } catch (NullPointerException e) {
        } catch (Exception e) {
            fail("getByName, null inetAddress - raised exception : "
                    + e.getMessage());
        }
        try {
            assertNull("validate handled ok if we ask for name not associated with any interface",
                                   NetworkInterface.getByName("8not a name4"));
        } catch (Exception e) {
            fail("getByName, unknown inetAddress - raised exception : "
                    + e.getMessage());
        }
        if (atLeastOneInterface) {
            String theName = networkInterface1.getName();
            if (theName != null) {
                try {
                    assertTrue(
                            "validate that Interface can be obtained with its name",
                            NetworkInterface.getByName(theName).equals(
                                    networkInterface1));
                } catch (Exception e) {
                    fail("validate to get network interface using name - socket exception");
                }
            }
            try {
                NetworkInterface.getByName(null);
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            } catch (SocketException e) {
                fail("SocketException was thrown.");
            }
        }
        if (atLeastTwoInterfaces) {
            String theName = networkInterface2.getName();
            if (theName != null) {
                try {
                    assertTrue(
                            "validate that Interface can be obtained with its name",
                            NetworkInterface.getByName(theName).equals(
                                    networkInterface2));
                } catch (Exception e) {
                    fail("validate to get network interface using name - socket exception");
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SocketException checking missed.",
        method = "getByInetAddress",
        args = {java.net.InetAddress.class}
    )
    public void test_getByInetAddressLjava_net_InetAddress() {
        byte addressBytes[] = new byte[4];
        addressBytes[0] = 0;
        addressBytes[1] = 0;
        addressBytes[2] = 0;
        addressBytes[3] = 0;
        try {
            assertNull("validate null handled ok",
                                   NetworkInterface.getByInetAddress(null));
            fail("should not get here if getByInetAddress throws "
                    + "NullPointerException if null passed in");
        } catch (NullPointerException e) {
        } catch (Exception e) {
            fail("getByInetAddress, null inetAddress should have raised NPE"
                    + " but instead threw a : " + e.getMessage());
        }
        try {
            assertNull("validate handled ok if we ask for address not associated with any interface",
                                   NetworkInterface.getByInetAddress(InetAddress
                            .getByAddress(addressBytes)));
        } catch (Exception e) {
            fail("getByInetAddress, unknown inetAddress threw exception : " + e);
        }
        if (atLeastOneInterface) {
            Enumeration addresses = networkInterface1.getInetAddresses();
            if (addresses != null) {
                while (addresses.hasMoreElements()) {
                    InetAddress theAddress = (InetAddress) addresses
                            .nextElement();
                    try {
                        assertTrue(
                                "validate that Interface can be obtained with any one of its addresses",
                                NetworkInterface.getByInetAddress(theAddress)
                                        .equals(networkInterface1));
                    } catch (Exception e) {
                        fail("validate to get address using inetAddress " +
                                "threw exception : " + e);
                    }
                }
            }
            try {
                NetworkInterface.getByInetAddress(null);
                fail("NullPointerException should be thrown.");
            } catch(NullPointerException npe) {
            } catch (SocketException e) {
                fail("SocketException was thrown.");
            }
        }
        if (atLeastTwoInterfaces) {
            Enumeration addresses = networkInterface2.getInetAddresses();
            if (addresses != null) {
                while (addresses.hasMoreElements()) {
                    InetAddress theAddress = (InetAddress) addresses
                            .nextElement();
                    try {
                        assertTrue(
                                "validate that Interface can be obtained with any one of its addresses",
                                NetworkInterface.getByInetAddress(theAddress)
                                        .equals(networkInterface2));
                    } catch (Exception e) {
                        fail("validate to get address using inetAddress "
                                + "threw exception : " + e);
                    }
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SocketException checking missed.",
        method = "getNetworkInterfaces",
        args = {}
    )
    public void test_getNetworkInterfaces() {
        try {
            Enumeration theInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (Exception e) {
            fail("get Network Interfaces - raised exception : "
                    + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        if (atLeastOneInterface) {
            assertTrue("If objects are the same true is returned",
                    networkInterface1.equals(sameAsNetworkInterface1));
            assertFalse("Validate Null handled ok", networkInterface1
                    .equals(null));
        }
        if (atLeastTwoInterfaces) {
            assertFalse("If objects are different false is returned",
                    networkInterface1.equals(networkInterface2));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        if (atLeastOneInterface) {
            assertTrue(
                    "validate that hash codes are the same for two calls on the same object",
                    networkInterface1.hashCode() == networkInterface1
                            .hashCode());
            assertTrue(
                    "validate that hash codes are the same for two objects for which equals is true",
                    networkInterface1.hashCode() == sameAsNetworkInterface1
                            .hashCode());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        if (atLeastOneInterface) {
            assertNotNull("validate that non null string is generated",
                    networkInterface1.toString());
            assertFalse("validate that non-zero length string is generated",
                    networkInterface1.toString().equals(""));
        }
        if (atLeastTwoInterfaces) {
            assertFalse(
                    "Validate strings are different for different interfaces",
                    networkInterface1.toString().equals(
                            networkInterface2.toString()));
        }
    }
    protected void setUp() {
        Enumeration theInterfaces = null;
        try {
            theInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (Exception e) {
            fail("Exception occurred getting network interfaces : " + e);
        }
        if ((theInterfaces != null) && (theInterfaces.hasMoreElements())) {
            while ((theInterfaces.hasMoreElements())
                    && (atLeastOneInterface == false)) {
                NetworkInterface theInterface = (NetworkInterface) theInterfaces
                        .nextElement();
                if (theInterface.getInetAddresses() != null) {
                    Enumeration addrs = theInterface.getInetAddresses();
                    if ((addrs != null) && (addrs.hasMoreElements())) {
                        atLeastOneInterface = true;
                        networkInterface1 = theInterface;
                    }
                }
            }
            while ((theInterfaces.hasMoreElements())
                    && (atLeastTwoInterfaces == false)) {
                NetworkInterface theInterface = (NetworkInterface) theInterfaces
                        .nextElement();
                if (theInterface.getInetAddresses() != null) {
                    Enumeration addrs = theInterface.getInetAddresses();
                    if ((addrs != null) && (addrs.hasMoreElements())) {
                        atLeastTwoInterfaces = true;
                        networkInterface2 = theInterface;
                    }
                }
            }
            if (atLeastOneInterface) {
                Enumeration addresses = networkInterface1.getInetAddresses();
                if (addresses != null) {
                    try {
                        if (addresses.hasMoreElements()) {
                            sameAsNetworkInterface1 = NetworkInterface
                            .getByInetAddress((InetAddress) addresses
                                    .nextElement());
                        }
                    } catch (SocketException e) {
                        fail("SocketException occurred : " + e);
                    }
                }
            }
        }
    }
    protected void tearDown() {
        System.setSecurityManager(null);
    }
}

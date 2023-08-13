@TestTargetClass(value = Authenticator.class,
                 untestedMethods = {
                    @TestTargetNew(
                         level = TestLevel.NOT_FEASIBLE,
                         notes = "",
                         method = "getRequestingHost",
                         args = {}
                     ),
                     @TestTargetNew(
                         level = TestLevel.NOT_FEASIBLE,
                         notes = "",
                         method = "getRequestingPort",
                         args = {}
                     ),
                     @TestTargetNew(
                         level = TestLevel.NOT_FEASIBLE,
                         notes = "",
                         method = "getRequestingSite",
                         args = {}
                     ),
                     @TestTargetNew(
                         level = TestLevel.NOT_FEASIBLE,
                         notes = "",
                         method = "getRequestingProtocol",
                         args = {}
                     ),
                     @TestTargetNew(
                         level = TestLevel.NOT_FEASIBLE,
                         notes = "",
                         method = "getRequestingPrompt",
                         args = {}
                     ),
                     @TestTargetNew(
                         level = TestLevel.NOT_FEASIBLE,
                         notes = "",
                         method = "getRequestingScheme",
                         args = {}
                     )}
    ) 
public class AuthenticatorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Test for checking RequestorType values.",
        method = "!Constants",
        args = {}
    )
    public void test_RequestorType_valueOfLjava_lang_String() throws Exception {
        assertEquals(RequestorType.PROXY, Authenticator.RequestorType
                .valueOf("PROXY"));
        assertEquals(RequestorType.SERVER, Authenticator.RequestorType
                .valueOf("SERVER"));
        try {
            RequestorType rt = Authenticator.RequestorType.valueOf("BADNAME");
            fail("Must throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Authenticator.RequestorType.valueOf(null);
            fail("Must throw an exception");
        } catch (NullPointerException e) {
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Test for checking RequestorType values.",
        method = "!Constants",
        args = {}
    )
    public void test_RequestorType_values() throws Exception {        
        RequestorType[] rt = RequestorType.values();
        assertEquals(RequestorType.PROXY, rt[0]);
        assertEquals(RequestorType.SERVER, rt[1]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "requestPasswordAuthentication",
        args = {java.net.InetAddress.class, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_requestPasswordAuthentication_InetAddress_int_String_String_String() throws Exception {
        MockAuthenticator mock = new MockAuthenticator();
        InetAddress addr = InetAddress.getLocalHost();
        Authenticator.setDefault(mock);
        Authenticator.requestPasswordAuthentication(addr, -1, "http", "promt", "HTTP");
        assertEquals(mock.getRequestorType(), RequestorType.SERVER);
        SecurityManager sm = new SecurityManager() {
            final String permissionName = "requestPasswordAuthentication";
            public void checkPermission(Permission perm) {
                if (perm.getName().equals(permissionName)) {
                    throw new SecurityException();
                }
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Authenticator.requestPasswordAuthentication("test_host", addr, -1, 
                    "http", "promt", "HTTP");
            fail("Should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
            Authenticator.setDefault(null);
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "requestPasswordAuthentication",
        args = {java.lang.String.class, java.net.InetAddress.class, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_requestPasswordAuthentication_String_InetAddress_int_String_String_String() throws Exception {
        MockAuthenticator mock = new MockAuthenticator();
        InetAddress addr = InetAddress.getLocalHost();
        Authenticator.setDefault(mock);
        Authenticator.requestPasswordAuthentication("test_host", addr, -1, "http", "promt", "HTTP");
        assertEquals(mock.getRequestorType(), RequestorType.SERVER);
        SecurityManager sm = new SecurityManager() {
            final String permissionName = "requestPasswordAuthentication";
            public void checkPermission(Permission perm) {
                if (perm.getName().equals(permissionName)) {
                    throw new SecurityException();
                }
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Authenticator.requestPasswordAuthentication("test_host", addr, -1, 
                    "http", "promt", "HTTP");
            fail("Should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
            Authenticator.setDefault(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "requestPasswordAuthentication",
        args = {java.lang.String.class, java.net.InetAddress.class, int.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.net.URL.class, java.net.Authenticator.RequestorType.class}
    )
    public void test_requestPasswordAuthentication_String_InetAddress_int_String_String_String_URL_Authenticator_RequestorType()
            throws UnknownHostException, MalformedURLException {
        MockAuthenticator mock = new MockAuthenticator();
        URL url = new URL("http:
        Authenticator.requestPasswordAuthentication("localhost", InetAddress
                .getByName("127.0.0.1"), 80, "HTTP", "", "", url,
                RequestorType.PROXY);
        assertNull(mock.getRequestingURL());
        assertNull(mock.getRequestorType());
        SecurityManager sm = new SecurityManager() {
            final String permissionName = "requestPasswordAuthentication";
            public void checkPermission(Permission perm) {
                if (perm.getName().equals(permissionName)) {
                    throw new SecurityException();
                }
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Authenticator.requestPasswordAuthentication("localhost", InetAddress
                    .getByName("127.0.0.1"), 80, "HTTP", "", "", url,
                    RequestorType.PROXY);
            fail("Should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
            Authenticator.setDefault(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRequestingURL",
        args = {}
    )
        public void test_getRequestingURL() throws Exception {
        MockAuthenticator mock = new MockAuthenticator();
        assertNull(mock.getRequestingURL());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRequestorType",
        args = {}
    )
    public void test_getRequestorType() throws Exception {
        MockAuthenticator mock = new MockAuthenticator();
        assertNull(mock.getRequestorType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDefault",
        args = {java.net.Authenticator.class}
    )
    public void test_setDefault() {
        StubAuthenticator stub = new StubAuthenticator();
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            PasswordAuthentication  pa = Authenticator.
            requestPasswordAuthentication(addr, 8080, "http", "promt", "HTTP");
            assertNull(pa);
        } catch (UnknownHostException e) {
            fail("UnknownHostException was thrown.");
        }
        MockAuthenticator mock = new MockAuthenticator();
        Authenticator.setDefault(mock);
        try {
            addr = InetAddress.getLocalHost();
            PasswordAuthentication  pa = Authenticator.
            requestPasswordAuthentication(addr, 80, "http", "promt", "HTTP");
            assertNull(pa);
        } catch (UnknownHostException e) {
            fail("UnknownHostException was thrown.");
        }
        Authenticator.setDefault(null);
        SecurityManager sm = new SecurityManager() {
            final String permissionName = "setDefaultAuthenticator";
            public void checkPermission(Permission perm) {
                if (perm.getName().equals(permissionName)) {
                    throw new SecurityException();
                }
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Authenticator.setDefault(stub);
            fail("Should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
            Authenticator.setDefault(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Authenticator",
        args = {}
    )
    public void test_Constructor() {
        MockAuthenticator ma = new MockAuthenticator();
        assertNull(ma.getRequestingURL());
        assertNull(ma.getRequestorType());
    }    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPasswordAuthentication",
        args = {}
    )
    public void test_getPasswordAuthentication() {
        MockAuthenticator ma = new MockAuthenticator();
        assertNull(ma.getPasswordAuthentication());
    }
    class MockAuthenticator extends java.net.Authenticator {
        public MockAuthenticator() {
            super();
        }
        public URL getRequestingURL() {
            return super.getRequestingURL();
        }
        public Authenticator.RequestorType getRequestorType() {
            return super.getRequestorType();
        }
        public PasswordAuthentication getPasswordAuthentication() {
            return super.getPasswordAuthentication();
        }
        public String getMockRequestingHost() {
            return super.getRequestingHost();
        }
        public int getMockRequestingPort() {
            return super.getRequestingPort();
        }
        public String getMockRequestingPrompt() {
            return super.getRequestingPrompt();
        }
        public String getMockRequestingProtocol() {
            return super.getRequestingProtocol();
        }
        public String getMockRequestingScheme() {
            return super.getRequestingScheme();
        }
        public InetAddress getMockRequestingSite() {
            return super.getRequestingSite();
        }
    }
    class StubAuthenticator extends java.net.Authenticator {
        public StubAuthenticator() {
            super();
        }
        public URL getRequestingURL() {
            return null;
        }
        public Authenticator.RequestorType getRequestorType() {
            return null;
        }
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("test", 
                    new char[] {'t', 'e', 's', 't'});
        }
    }
}

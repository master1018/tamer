@TestTargetClass(CookieHandler.class) 
public class CookieHandlerTest extends TestCase {
    URI getURI, putURI;
    String link = "http:
    boolean isGetCalled = false;
    boolean isPutCalled = false;
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void test_GetDefault() {
        assertNull(CookieHandler.getDefault());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setDefault method.",
        method = "setDefault",
        args = {java.net.CookieHandler.class}
    )
    public void test_SetDefault_java_net_cookieHandler() {
        MockCookieHandler rc1 = new MockCookieHandler();
        MockCookieHandler rc2 = new MockCookieHandler();
        CookieHandler.setDefault(rc1);
        assertSame(CookieHandler.getDefault(), rc1);
        CookieHandler.setDefault(rc2);
        assertSame(CookieHandler.getDefault(), rc2);
        CookieHandler.setDefault(null);
        assertNull(CookieHandler.getDefault());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void testGetDefault_Security() {
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSM());
        } catch (SecurityException e) {
            System.err.println("Unable to reset securityManager,test ignored");
            return;
        }
        try {
            CookieHandler.getDefault();
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(old);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setDefault method.",
        method = "setDefault",
        args = {java.net.CookieHandler.class}
    )    public void testSetDefault_Security() {
        CookieHandler rc = new MockCookieHandler();
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSM());
        } catch (SecurityException e) {
            System.err.println("Unable to reset securityManager,test ignored");
            return;
        }
        try {
            CookieHandler.setDefault(rc);
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(old);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CookieHandler",
        args = {}
    )
    public void test_CookieHandler() {
        MockCookieHandler mch = new MockCookieHandler();
        assertNull(mch.getDefault());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "get",
            args = {java.net.URI.class, java.util.Map.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "put",
            args = {java.net.URI.class, java.util.Map.class}
        )
    })
    @KnownFailure("Cache is not used")
    public void test_get_put() {
        MockCookieHandler mch = new MockCookieHandler();
        CookieHandler defaultHandler = CookieHandler.getDefault();
        CookieHandler.setDefault(mch);
        class TestThread extends Thread {
            public void run() {
                try {
                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();
                    Object obj = conn.getContent();
                    url = new URL(link);
                    conn = url.openConnection();
                    obj = conn.getContent();
                } catch (MalformedURLException e) {
                    fail("MalformedURLException was thrown: " + e.toString());
                } catch (IOException e) {
                    fail("IOException was thrown.");
               }                
            }
        };
        try {
            TestThread thread = new TestThread();
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail("InterruptedException was thrown.");
            }
            assertTrue(isGetCalled);
            assertTrue(isPutCalled);
        } finally {
            CookieHandler.setDefault(defaultHandler);
        }
    }
    class MockCookieHandler extends CookieHandler {
        public Map get(URI uri, Map requestHeaders) throws IOException {
            getURI = uri;
            isGetCalled = true;
            return requestHeaders;
        }
        public void put(URI uri, Map responseHeaders) throws IOException {
            putURI = uri;
            isPutCalled = true;
        }
    }
    class MockSM extends SecurityManager {
        public void checkPermission(Permission permission) {
            if (permission instanceof NetPermission) {
                if ("setCookieHandler".equals(permission.getName())) {
                    throw new SecurityException();
                }
            }
            if (permission instanceof NetPermission) {
                if ("getCookieHandler".equals(permission.getName())) {
                    throw new SecurityException();
                }
            }
            if (permission instanceof RuntimePermission) {
                if ("setSecurityManager".equals(permission.getName())) {
                    return;
                }
            }
        }
    }
}

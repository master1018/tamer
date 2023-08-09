@TestTargetClass(SSLSessionContext.class) 
public class SSLSessionContextTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSessionCacheSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSessionCacheSize",
            args = {int.class}
        )
    })
    public final void test_sessionCacheSize() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        SSLSessionContext sc = context
                .getClientSessionContext();
        sc.setSessionCacheSize(10);
        assertEquals("10 wasn't returned", 10, sc.getSessionCacheSize());
        sc.setSessionCacheSize(5);
        assertEquals("5 wasn't returned", 5, sc.getSessionCacheSize());
        try {
            sc.setSessionCacheSize(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSessionTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSessionTimeout",
            args = {int.class}
        )
    })
    public final void test_sessionTimeout() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        SSLSessionContext sc = context
                .getClientSessionContext();
        sc.setSessionTimeout(100);
        assertEquals("100 wasn't returned", 100, sc.getSessionTimeout());
        sc.setSessionTimeout(5000);
        assertEquals("5000 wasn't returned", 5000, sc.getSessionTimeout());
        try {
            sc.setSessionTimeout(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSession",
        args = {byte[].class}
    )
    public final void test_getSession() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        SSLSessionContext sc = context
                .getClientSessionContext();
        try {
            sc.getSession(null);
        } catch (NullPointerException e) {
        }
        assertNull(sc.getSession(new byte[5]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIds",
        args = {}
    )
    public final void test_getIds() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        SSLSessionContext sc = context
                .getClientSessionContext();
        assertFalse(sc.getIds().hasMoreElements());
    }
}
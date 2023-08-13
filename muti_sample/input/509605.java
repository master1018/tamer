@TestTargetClass(AuthProvider.class)
public class AuthProviderTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AuthProvider",
            args = {java.lang.String.class, double.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "login",
            args = {javax.security.auth.Subject.class, javax.security.auth.callback.CallbackHandler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "logout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setCallbackHandler",
            args = {javax.security.auth.callback.CallbackHandler.class}
        )
    })
    public void testConstructor01() {
        AuthProviderStub ap = new AuthProviderStub("name", 1.0, "info");
        CallbackHandler handler = null;
        String[] str = {"", null, "!@#$%^&*()"};
        double[] version = {0.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.NEGATIVE_INFINITY};
        assertEquals("name", ap.getName());
        assertEquals(1.0, ap.getVersion());
        assertEquals("info", ap.getInfo());
        assertNotNull(ap.getServices());
        assertTrue(ap.getServices().isEmpty());
        for (int i = 0; i < str.length; i++) {
            for (int j = 0; j < version.length; j++) {
                try {
                    ap = new AuthProviderStub(str[i], version[j], str[i]);
                } catch (Exception ex) {
                    fail("Unexpected exception was thrown");
                }
            }
        }
        try {
            ap.setCallbackHandler(handler);
            ap.login(null, handler);
            ap.logout();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    public class AuthProviderStub extends AuthProvider {
        public AuthProviderStub(String name, double version, String info) {
            super( name,  version, info);
        }
        public void login(Subject subject, CallbackHandler handler) {}
        public void logout() {}
        public void setCallbackHandler(CallbackHandler handler){}
    }
}

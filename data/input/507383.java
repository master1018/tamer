@TestTargetClass(KeyStore.CallbackHandlerProtection.class)
public class KSCallbackHandlerProtectionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CallbackHandlerProtection",
        args = {javax.security.auth.callback.CallbackHandler.class}
    )
    public void testCallbackHandlerProtection() {
        try {
            new KeyStore.CallbackHandlerProtection(null);
            fail("NullPointerException must be thrown when handler is null");
        } catch (NullPointerException e) {
        }
        class TestCallbackHandler implements CallbackHandler {
            public void handle(Callback[] callbacks) throws IOException,
                    UnsupportedCallbackException {
            }
        }
        try {
            new KeyStore.CallbackHandlerProtection(new TestCallbackHandler());
        } catch (Exception e) {
            fail("unexpected exception: " + e);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "CallbackHandlerProtection",
            args = {javax.security.auth.callback.CallbackHandler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCallbackHandler",
            args = {}
        )
    })
    public void testGetCallBackHandler() {
        CallbackHandler cbh = new tmpCallbackHandler();
        KeyStore.CallbackHandlerProtection ksCBH = new KeyStore.CallbackHandlerProtection(cbh);
        assertEquals("Incorrect CallbackHandler", cbh,
                ksCBH.getCallbackHandler());
    }
}

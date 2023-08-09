@TestTargetClass(CallbackHandler.class) 
public class CallbackHandlerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "handle",
        args = {Callback[].class}
    )
    public void test_CallbackHandler() {
        CallbackHandlerImpl ch = new CallbackHandlerImpl();
        assertFalse(ch.called);
        ch.handle(null);
        assertTrue(ch.called);
    }
    private class CallbackHandlerImpl implements CallbackHandler {
        boolean called = false;
        public void handle(Callback[] callbacks) {
            called = true;
        }
    }
}

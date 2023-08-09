@TestTargetClass(UnsupportedCallbackException.class) 
public class UnsupportedCallbackExceptionTest extends TestCase {
    public static void main(String[] args) {
    }
    public UnsupportedCallbackExceptionTest(String arg0) {
        super(arg0);
    }
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "UnsupportedCallbackException",
            args = {Callback.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getCallback",
            args = {}
        )
    })
    public void testUnsupportedCallbackException01() {
        Callback c = null;
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(c);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertNull("getCallback() must return null", ucE.getCallback());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "UnsupportedCallbackException",
            args = {Callback.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getCallback",
            args = {}
        )
    })
    public void testUnsupportedCallbackException02() {
        myCallback c = new myCallback();
        assertNotNull("Callback object is null", c);
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(c);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertEquals("Incorrect callback object was returned", c, ucE.getCallback());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException03() {
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(null, null);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertNull("getCallback() must return null.", ucE.getCallback());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException04() {
        UnsupportedCallbackException ucE;
        for (int i = 0; i < msgs.length; i++) {
            ucE = new UnsupportedCallbackException(null, msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), ucE.getMessage(), msgs[i]);
            assertNull("getCallback() must return null.", ucE.getCallback());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException05() {
        myCallback c = new myCallback();
        assertNotNull("Callback object is null", c);
        UnsupportedCallbackException ucE = new UnsupportedCallbackException(c, null);
        assertNull("getMessage() must return null.", ucE.getMessage());
        assertEquals("Incorrect callback object was returned", c, ucE.getCallback());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnsupportedCallbackException",
        args = {Callback.class, String.class}
    )
    public void testUnsupportedCallbackException06() {
        myCallback c = new myCallback();
        assertNotNull("Callback object is null", c);
        UnsupportedCallbackException ucE;
        for (int i = 0; i < msgs.length; i++) {
            ucE = new UnsupportedCallbackException(c, msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), ucE.getMessage(), msgs[i]);
            assertEquals("Incorrect callback object was returned", c, ucE.getCallback());
        }
    }
}
class myCallback implements Callback {
    myCallback(){
    }
}

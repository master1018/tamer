@TestTargetClass(DestroyFailedException.class) 
public class DestroyFailedExceptionTest extends TestCase {
    public static void main(String[] args) {
    }
    public DestroyFailedExceptionTest(String arg0) {
        super(arg0);
    }
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DestroyFailedException",
        args = {}
    )
    public void testDestroyFailedException01() {
        DestroyFailedException dfE = new DestroyFailedException();
        assertNull("getMessage() must return null.", dfE.getMessage());
        assertNull("getCause() must return null", dfE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "DestroyFailedException",
        args = {String.class}
    )
    public void testDestroyFailedException02() {
        DestroyFailedException dfE;
        for (int i = 0; i < msgs.length; i++) {
            dfE = new DestroyFailedException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), dfE.getMessage(), msgs[i]);
            assertNull("getCause() must return null", dfE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "DestroyFailedException",
        args = {String.class}
    )
    public void testDestroyFailedException03() {
        String msg = null;
        DestroyFailedException dfE = new DestroyFailedException(msg);
        assertNull("getMessage() must return null.", dfE.getMessage());
        assertNull("getCause() must return null", dfE.getCause());
    }
}

@TestTargetClass(IllegalBlockSizeException.class)
public class IllegalBlockSizeExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalBlockSizeException",
        args = {}
    )
    public void testIllegalBlockSizeException01() {
        IllegalBlockSizeException tE = new IllegalBlockSizeException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalBlockSizeException",
        args = {java.lang.String.class}
    )
    public void testIllegalBlockSizeException02() {
        IllegalBlockSizeException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new IllegalBlockSizeException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalBlockSizeException",
        args = {java.lang.String.class}
    )
    public void testIllegalBlockSizeException03() {
        String msg = null;
        IllegalBlockSizeException tE = new IllegalBlockSizeException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}

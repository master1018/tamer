@TestTargetClass(InvalidParameterException.class)
public class InvalidParameterExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidParameterException",
        args = {}
    )
    public void testInvalidParameterException01() {
        InvalidParameterException tE = new InvalidParameterException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidParameterException",
        args = {java.lang.String.class}
    )
    public void testInvalidParameterException02() {
        InvalidParameterException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidParameterException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidParameterException",
        args = {java.lang.String.class}
    )
    public void testInvalidParameterException03() {
        String msg = null;
        InvalidParameterException tE = new InvalidParameterException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}

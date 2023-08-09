@TestTargetClass(InvalidParameterSpecException.class)
public class InvalidParameterSpecExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidParameterSpecException",
        args = {}
    )
    public void testInvalidParameterSpecException01() {
        InvalidParameterSpecException tE = new InvalidParameterSpecException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidParameterSpecException",
        args = {java.lang.String.class}
    )
    public void testInvalidParameterSpecException02() {
        InvalidParameterSpecException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidParameterSpecException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "InvalidParameterSpecException",
        args = {java.lang.String.class}
    )
    public void testInvalidParameterSpecException03() {
        String msg = null;
        InvalidParameterSpecException tE = new InvalidParameterSpecException(
                msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}

@TestTargetClass( UnrecoverableEntryException.class)
public class UnrecoverableEntryExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static String errNotExc = "Not UnrecoverableEntryException object";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnrecoverableEntryException",
        args = {}
    )
    public void testUnrecoverableEntryException() {
        UnrecoverableEntryException tE = new UnrecoverableEntryException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnrecoverableEntryException",
        args = {java.lang.String.class}
    )
    public void testUnrecoverableEntryExceptionString() {
        UnrecoverableEntryException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new UnrecoverableEntryException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
        try {
            tE = new UnrecoverableEntryException(null);
        } catch (Exception e) {
            fail("Exception " + e + " was thrown for NULL parameter");
        }
    }
}

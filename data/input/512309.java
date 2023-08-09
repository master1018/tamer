@TestTargetClass(ShortBufferException.class)
public class ShortBufferExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ShortBufferException",
        args = {}
    )
    public void testShortBufferException01() {
        ShortBufferException tE = new ShortBufferException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ShortBufferException",
        args = {java.lang.String.class}
    )
    public void testShortBufferException02() {
        ShortBufferException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new ShortBufferException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ShortBufferException",
        args = {java.lang.String.class}
    )
    public void testShortBufferException03() {
        String msg = null;
        ShortBufferException tE = new ShortBufferException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}

@TestTargetClass(SAXNotSupportedException.class)
public class SAXNotSupportedExceptionTest extends TestCase {
    public static final String ERR = "Houston, we have a problem";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotSupportedException",
        args = { }
    )
    public void testSAXNotSupportedException() {
        SAXNotSupportedException e = new SAXNotSupportedException();
        assertNull(e.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotSupportedException",
        args = { String.class }
    )
    public void testSAXNotSupportedException_String() {
        SAXNotSupportedException e = new SAXNotSupportedException(ERR);
        assertEquals(ERR, e.getMessage());
        e = new SAXNotSupportedException(null);
        assertNull(e.getMessage());
    }
}

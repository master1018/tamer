@TestTargetClass(SAXNotRecognizedException.class)
public class SAXNotRecognizedExceptionTest extends TestCase {
    public static final String ERR = "Houston, we have a problem";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotRecognizedException",
        args = { }
    )
    public void testSAXNotRecognizedException() {
        SAXNotRecognizedException e = new SAXNotRecognizedException();
        assertNull(e.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotRecognizedException",
        args = { String.class }
    )
    public void testSAXNotRecognizedException_String() {
        SAXNotRecognizedException e = new SAXNotRecognizedException(ERR);
        assertEquals(ERR, e.getMessage());
        e = new SAXNotRecognizedException(null);
        assertNull(e.getMessage());
    }
}

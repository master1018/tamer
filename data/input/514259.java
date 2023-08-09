@TestTargetClass(ZipException.class)
public class ZipExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ZipException",
        args = {}
    )
    public void testZipException() {
        ZipException zz = new ZipException();
        assertEquals(zz.getMessage(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ZipException",
        args = {java.lang.String.class}
    )
    public void testZipExceptionLjava_lang_String() {
        ZipException zz = new ZipException("Test");
        assertEquals(zz.getMessage(), "Test");
    }
}

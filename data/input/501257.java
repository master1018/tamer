@TestTargetClass(AnnotationFormatError.class) 
public class AnnotationFormatErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AnnotationFormatError",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_String() {
        AnnotationFormatError e = new AnnotationFormatError("some message");
        assertEquals("some message", e.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AnnotationFormatError",
        args = {java.lang.Throwable.class}
    )
    public void test_constructorLjava_lang_Throwable() {
        IllegalArgumentException iae = new IllegalArgumentException();
        AnnotationFormatError e = new AnnotationFormatError(iae);
        assertSame(iae, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AnnotationFormatError",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_StringLjava_lang_Throwable() {
        IllegalArgumentException iae = new IllegalArgumentException();
        AnnotationFormatError e = new AnnotationFormatError("some message", iae);
        assertEquals("some message", e.getMessage());
        assertSame(iae, e.getCause());
    }
}

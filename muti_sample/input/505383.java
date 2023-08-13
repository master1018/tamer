@TestTargetClass(IncompleteAnnotationException.class)
public class IncompleteAnnotationExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies NullPointerException.",
        method = "IncompleteAnnotationException",
        args = {java.lang.Class.class, java.lang.String.class}
    )
    public void testNullType() {
        try {
            new IncompleteAnnotationException(null, "str");
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "IncompleteAnnotationException",
            args = {java.lang.Class.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "annotationType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "elementName",
            args = {}
        )
    })
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_Class_Ljava_lang_String()
            throws Exception {
        Class clazz = String.class;
        String elementName = "some element";
        IncompleteAnnotationException e = new IncompleteAnnotationException(
                clazz, elementName);
        assertNotNull("can not instantiate IncompleteAnnotationException", e);
        assertSame("wrong annotation type", clazz, e.annotationType());
        assertSame("wrong element name", elementName, e.elementName());
    }
}

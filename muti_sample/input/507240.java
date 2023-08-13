@TestTargetClass(AnnotationTypeMismatchException.class) 
public class AnnotationTypeMismatchExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AnnotationTypeMismatchException",
            args = {java.lang.reflect.Method.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "element",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "foundType",
            args = {}
        )
    })
    @SuppressWarnings("nls")
    public void test_constructorLjava_lang_reflect_MethodLjava_lang_String() throws SecurityException, ClassNotFoundException {
        Method[] methods = Class.forName("java.lang.String").getMethods();
        Method m = methods[0];
        AnnotationTypeMismatchException e = new AnnotationTypeMismatchException(
                m, "some type");
        assertNotNull("can not instantiate AnnotationTypeMismatchException", e);
        assertSame("wrong method name", m, e.element());
        assertEquals("wrong found type", "some type", e.foundType());
    }
}

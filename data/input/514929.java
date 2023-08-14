@TestTargetClass(JarException.class)
public class JarExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "JarException",
        args = {}
    )
    public void test_Constructor() throws Exception {
        JarException ex = new JarException();
        JarException ex1 = new JarException("Test string");
        JarException ex2 = new JarException(null);
        assertNotSame(ex, ex1);
        assertNotSame(ex.getMessage(), ex1.getMessage());
        assertNotSame(ex, ex2);
        assertSame(ex.getMessage(), ex2.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "JarException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() throws Exception {
        JarException ex1 = new JarException("Test string");
        JarException ex2 = new JarException(null);
        assertNotSame(ex1, ex2);
        assertNotSame(ex1.getMessage(), ex2.getMessage());
    }
}

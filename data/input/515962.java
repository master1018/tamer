@TestTargetClass(AssertionError.class) 
public class AssertionErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {}
    )
    public void test_Constructor() {
        AssertionError e = new AssertionError();
        assertNull(e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {java.lang.Object.class}
    )
    public void test_ConstructorObject() {
        Object obj = "toString";
        AssertionError e = new AssertionError(obj);
        assertEquals("toString", e.getMessage());
        assertNull(e.getCause());
        NullPointerException npe = new NullPointerException("null value");
        e = new AssertionError(npe);
        assertEquals(npe.toString(), e.getMessage());
        assertSame(npe, e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {boolean.class}
    )
    public void test_ConstructorBoolean() {
        AssertionError e = new AssertionError(true);
        assertEquals("true", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {char.class}
    )
    public void test_ConstructorChar() {
        AssertionError e = new AssertionError('a');
        assertEquals("a", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {int.class}
    )
    public void test_ConstructorInt() {
        AssertionError e = new AssertionError(1);
        assertEquals("1", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {long.class}
    )
    public void test_ConstructorLong() {
        AssertionError e = new AssertionError(1L);
        assertEquals("1", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {float.class}
    )
    public void test_ConstructorFloat() {
        AssertionError e = new AssertionError(1.0F);
        assertEquals("1.0", e.getMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AssertionError",
        args = {double.class}
    )
    public void test_ConstructorDouble() {
        AssertionError e = new AssertionError(1.0D);
        assertEquals("1.0", e.getMessage());
        assertNull(e.getCause());
    }
}

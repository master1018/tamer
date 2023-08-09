@TestTargetClass(NoSuchElementException.class) 
public class NoSuchElementExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchElementException",
        args = {}
    )
    public void test_Constructor() {
        assertNotNull(new NoSuchElementException());
        try {
            Vector v = new Vector();
            v.elements().nextElement();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchElementException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        assertNotNull(new NoSuchElementException("String"));
        assertNotNull(new NoSuchElementException(null));
        try {
            Vector v = new Vector();
            v.firstElement();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

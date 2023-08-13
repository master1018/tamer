@TestTargetClass(EmptyStackException.class) 
public class EmptyStackExceptionTest extends junit.framework.TestCase {
    Object[] objArray = new Object[10];
    Stack s;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EmptyStackException",
        args = {}
    )
    public void test_Constructor() {
        try {
            for (int counter = 0; counter < objArray.length + 1; counter++)
                s.pop();
        } catch (EmptyStackException e) {
            return;
        }
        fail("Expected EmptyStackException not thrown");
    }
    protected void setUp() {
        for (int counter = 0; counter < objArray.length; counter++) {
            objArray[counter] = new Integer(counter);
        }
        s = new Stack();
        for (int counter = 0; counter < objArray.length; counter++) {
            s.push(objArray[counter]);
        }
    }
    protected void tearDown() {
        objArray = null;
        s = null;
    }
}

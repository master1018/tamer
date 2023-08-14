@TestTargetClass(InterruptedIOException.class) 
public class InterruptedIOExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InterruptedIOException",
        args = {}
    )    
    public void test_Constructor() {
        try {
            throw new InterruptedIOException();
        } catch (InterruptedIOException e) {
            return;
        } catch (Exception e) {
            fail("Exception during InterruptedIOException test"
                    + e.toString());
        }
        fail("Failed to generate exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InterruptedIOException",
        args = {java.lang.String.class}
    )    
    public void test_ConstructorLjava_lang_String() {
        try {
            throw new InterruptedIOException("Some error message");
        } catch (InterruptedIOException e) {
            return;
        } catch (Exception e) {
            fail("Exception during InterruptedIOException test"
                    + e.toString());
        }
        fail("Failed to generate exception");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

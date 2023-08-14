@TestTargetClass(BindException.class) 
public class BindExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BindException",
        args = {}
    )
    public void test_Constructor() {
        try {
            throw new BindException();
        } catch (BindException e) {
            return;
        } catch (Exception e) {
            fail("Exception during BindException test" + e.toString());
        }
        fail("Failed to generate exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "BindException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            throw new BindException("Some error message");
        } catch (BindException e) {
            return;
        } catch (Exception e) {
            fail("Exception during BindException test : " + e.getMessage());
        }
        fail("Failed to generate exception");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

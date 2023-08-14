@TestTargetClass(ProtocolException.class) 
public class ProtocolExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ProtocolException",
        args = {}
    )
    public void test_Constructor() {
        try {
            throw new ProtocolException();
        } catch (ProtocolException e) {
            return;
        } catch (Exception e) {
            fail("Exception during ProtocolException test : " + e.getMessage());
        }
        fail("Failed to generate expected exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ProtocolException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            throw new ProtocolException("Some error message");
        } catch (ProtocolException e) {
            return;
        } catch (Exception e) {
            fail("Exception during ProtocolException test : " + e.getMessage());
        }
        fail("Failed to generate expected exception");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

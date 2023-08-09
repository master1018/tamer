@TestTargetClass(SocketException.class) 
public class SocketExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SocketException",
        args = {}
    )
    public void test_Constructor() {
        try {
            throw new SocketException();
        } catch (SocketException e) {
            return;
        } catch (Exception e) {
            fail("Exception during SocketException test : " + e.getMessage());
        }
        fail("Failed to generate expected exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SocketException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            throw new SocketException("Some error message");
        } catch (SocketException e) {
            return;
        } catch (Exception e) {
            fail("Exception during SocketException test" + e.toString());
        }
        fail("Failed to generate expected exception");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

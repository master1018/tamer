@TestTargetClass(SocketTimeoutException.class) 
public class SocketTimeoutExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SocketTimeoutException",
        args = {}
    )
    public void test_Constructor() {
        SocketTimeoutException ste = new SocketTimeoutException();
        assertNull(ste.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SocketTimeoutException",
        args = {String.class}
    )
    public void test_ConstructorLString() {
        String [] strs = {"", null, "Test String"};
        for(String str:strs) {
            SocketTimeoutException ste = new SocketTimeoutException(str);
            assertEquals(str, ste.getMessage());
        } 
    }
}

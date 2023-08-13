@TestTargetClass(ConnectException.class) 
public class ConnectExceptionTest extends junit.framework.TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ConnectException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ConnectException",
            args = {java.lang.String.class}
        )
    })
    public void test_Constructor() {
        assertNull("Wrong message", new ConnectException().getMessage());
        assertEquals("Wrong message", "message", new ConnectException("message").getMessage());
    }
}

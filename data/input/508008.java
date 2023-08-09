@TestTargetClass(AbstractMethodError.class)
public class AbstractMethodErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AbstractMethodError",
        args = {}
    )
    public void test_AbstractMethodError() {
        AbstractMethodError ame = new AbstractMethodError();
        assertNull(ame.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AbstractMethodError",
        args = {java.lang.String.class}
    )
    public void test_AbstractMethodError_String() {
        String message = "Test Message";
        AbstractMethodError ame = new AbstractMethodError(message);
        assertEquals(message, ame.getMessage());
    }
}

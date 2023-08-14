@TestTargetClass(NotOwnerException.class)
public class NotOwnerExceptionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(NotOwnerExceptionTest.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NotOwnerException",
        args = {}
    )
    public void testNotOwnerException() {
        assertNotNull(new NotOwnerException());
        assertNull(new NotOwnerException().getMessage());
        assertNull(new NotOwnerException().getCause());
    }
}

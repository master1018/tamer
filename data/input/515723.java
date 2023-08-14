@TestTargetClass(LastOwnerException.class)
public class LastOwnerExceptionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(LastOwnerExceptionTest.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LastOwnerException",
        args = {}
    )
    public void testLastOwnerException() {
        assertNotNull(new LastOwnerException());
        assertNull(new LastOwnerException().getMessage());
        assertNull(new LastOwnerException().getCause());
    }
}

@TestTargetClass(PackageManager.NameNotFoundException.class)
public class PackageManager_NameNotFoundExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test NameNotFoundException",
            method = "PackageManager.NameNotFoundException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test NameNotFoundException",
            method = "PackageManager.NameNotFoundException",
            args = {java.lang.String.class}
        )
    })
    public void testNameNotFoundException() {
        PackageManager.NameNotFoundException exception = new PackageManager.NameNotFoundException();
        try {
            throw exception;
        } catch (NameNotFoundException e) {
            assertNull(e.getMessage());
        }
        final String message = "test";
        exception = new PackageManager.NameNotFoundException(message);
        try {
            throw exception;
        } catch (NameNotFoundException e) {
            assertEquals(message, e.getMessage());
        }
    }
}

@TestTargetClass(android.database.StaleDataException.class)
public class StaleDataExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of StaleDataException.",
            method = "StaleDataException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of StaleDataException.",
            method = "StaleDataException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructors() {
        String expected1 = "Expected exception message";
        try {
            throw new StaleDataException();
        } catch (StaleDataException e) {
            assertNull(e.getMessage());
        }
        try {
            throw new StaleDataException(expected1);
        } catch (StaleDataException e) {
            assertEquals(expected1, e.getMessage());
        }
    }
}

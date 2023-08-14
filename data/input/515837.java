@TestTargetClass(android.content.ActivityNotFoundException.class)
public class ActivityNotFoundExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of ActivityNotFoundException.",
            method = "ActivityNotFoundException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of ActivityNotFoundException.",
            method = "ActivityNotFoundException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructors() {
        new ActivityNotFoundException();
        new ActivityNotFoundException("TEST_STRING");
    }
}

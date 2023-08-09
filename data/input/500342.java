@TestTargetClass(AndroidException.class)
public class AndroidExceptionTest extends AndroidTestCase {
    private static final String NAME = "Test_AndroidException";
    private static final Exception CAUSE = new Exception();
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test exception methods",
            method = "AndroidException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test exception methods",
            method = "AndroidException",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test exception methods",
            method = "AndroidException",
            args = {java.lang.Exception.class}
        )
    })
    public void testAndroidException() {
        try {
            throw new AndroidException();
        } catch (AndroidException e) {
        }
        try {
            throw new AndroidException(NAME);
        } catch (AndroidException e) {
            assertEquals(NAME, e.getMessage());
        }
        try {
            throw new AndroidException(CAUSE);
        } catch (AndroidException e) {
            assertEquals(CAUSE, e.getCause());
        }
    }
}

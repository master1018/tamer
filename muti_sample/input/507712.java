@TestTargetClass(AndroidRuntimeException.class)
public class AndroidRuntimeExceptionTest extends AndroidTestCase {
    private static final String NAME = "Test_AndroidRuntimeException";
    private static final Exception CAUSE = new Exception();
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test AndroidRuntimeException",
            method = "AndroidRuntimeException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test AndroidRuntimeException",
            method = "AndroidRuntimeException",
            args = {java.lang.Exception.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test AndroidRuntimeException",
            method = "AndroidRuntimeException",
            args = {java.lang.String.class}
        )
    })
    public void testAndroidRuntimeException() {
        try {
            throw new AndroidRuntimeException();
        } catch (AndroidRuntimeException e) {
        }
        try {
            throw new AndroidRuntimeException(NAME);
        } catch (AndroidRuntimeException e) {
            assertEquals(NAME, e.getMessage());
        }
        try {
            throw new AndroidRuntimeException(CAUSE);
        } catch (AndroidRuntimeException e) {
            assertEquals(CAUSE, e.getCause());
        }
    }
}

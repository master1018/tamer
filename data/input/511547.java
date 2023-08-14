@TestTargetClass(BadTokenException.class)
public class WindowManager_BadTokenExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test BadTokenException constructor",
            method = "WindowManager.BadTokenException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test BadTokenException constructor",
            method = "WindowManager.BadTokenException",
            args = {String.class}
        )
    })
    public void testBadTokenException(){
        BadTokenException badTokenException = new BadTokenException();
        try {
            throw badTokenException;
        } catch (BadTokenException e) {
            assertNull(e.getMessage());
        }
        String message = "WindowManager_BadTokenExceptionTest";
        badTokenException = new BadTokenException(message);
        try {
            throw badTokenException;
        } catch (BadTokenException e) {
            assertEquals(message, e.getMessage());
        }
    }
}

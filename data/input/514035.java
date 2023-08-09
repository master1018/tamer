@TestTargetClass(PendingIntent.CanceledException.class)
public class PendingIntent_CanceledExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PendingIntent.CanceledException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PendingIntent.CanceledException",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PendingIntent.CanceledException",
            args = {Exception.class}
        )
    })
    public void testConstructor() {
        PendingIntent.CanceledException canceledException = new PendingIntent.CanceledException();
        try {
            throw canceledException;
        } catch (CanceledException e) {
            assertNull(e.getMessage());
        }
        final String message = "test";
        canceledException = new PendingIntent.CanceledException(message);
        try {
            throw canceledException;
        } catch (CanceledException e) {
            assertEquals(message, canceledException.getMessage());
        }
        Exception ex = new Exception();
        canceledException = new PendingIntent.CanceledException(ex);
        try {
            throw canceledException;
        } catch (CanceledException e) {
            assertSame(ex, e.getCause());
        }
    }
}

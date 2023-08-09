@TestTargetClass(android.content.ReceiverCallNotAllowedException.class)
public class ReceiverCallNotAllowedExceptionTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor of ReceiverCallNotAllowedException.",
        method = "ReceiverCallNotAllowedException",
        args = {java.lang.String.class}
    )
    public void testConstructor() {
        new ReceiverCallNotAllowedException("TEST_STRING");
    }
}

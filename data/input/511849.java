@TestTargetClass(Resources.NotFoundException.class)
public class Resources_NotFoundExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Resources.NotFoundException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Resources.NotFoundException",
            args = {java.lang.String.class}
        )
    })
    public void testNotFoundException() {
        NotFoundException ne = null;
        boolean isThrowed = false;
        boolean isFinnalyRun = false;
        try {
            ne = new NotFoundException();
            throw ne;
        } catch (NotFoundException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out NotFoundException");
            }
            isFinnalyRun = true;
        }
        assertTrue(isFinnalyRun);
        isThrowed = false;
        isFinnalyRun = false;
        final String MESSAGE = "test";
        try {
            ne = new NotFoundException(MESSAGE);
            throw ne;
        } catch (NotFoundException e) {
            assertSame(ne, e);
            assertEquals(MESSAGE, e.getMessage());
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out NotFoundException");
            }
            isFinnalyRun = true;
        }
        assertTrue(isFinnalyRun);
    }
}

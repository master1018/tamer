@TestTargetClass(BadParcelableException.class)
public class BadParcelableExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method: BadParcelableException",
            method = "BadParcelableException",
            args = {java.lang.Exception.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method: BadParcelableException",
            method = "BadParcelableException",
            args = {java.lang.String.class}
        )
    })
    public void testBadParcelableException(){
        BadParcelableException ne = null;
        boolean isThrowed = false;
        try {
            ne = new BadParcelableException("BadParcelableException");
            throw ne;
        } catch (BadParcelableException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out BadParcelableException");
            }
        }
        isThrowed = false;
        try {
            ne = new BadParcelableException(new Exception());
            throw ne;
        } catch (BadParcelableException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out BadParcelableException");
            }
        }
    }
}

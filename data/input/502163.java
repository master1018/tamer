@TestTargetClass(DeadObjectException.class)
public class DeadObjectExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test method: DeadObjectException",
        method = "DeadObjectException",
        args = {}
    )
    public void testDeadObjectException(){
        DeadObjectException ne = null;
        boolean isThrowed = false;
        try {
            ne = new DeadObjectException();
            throw ne;
        } catch (DeadObjectException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out DeadObjectException");
            }
        }
    }
}

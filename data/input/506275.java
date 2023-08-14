@TestTargetClass(BadSurfaceTypeException.class)
public class SurfaceHolder_BadSurfaceTypeExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test BadSurfaceTypeException",
            method = "SurfaceHolder.BadSurfaceTypeException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test BadSurfaceTypeException",
            method = "SurfaceHolder.BadSurfaceTypeException",
            args = {java.lang.String.class}
        )
    })
    public void testBadSurfaceTypeException(){
        BadSurfaceTypeException ne = null;
        boolean isThrowed = false;
        try {
            ne = new BadSurfaceTypeException();
            throw ne;
        } catch (BadSurfaceTypeException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out InflateException");
            }
        }
        String name = "SurfaceHolder_BadSurfaceTypeExceptionTest";
        isThrowed = false;
        try {
            ne = new BadSurfaceTypeException(name);
            throw ne;
        } catch (BadSurfaceTypeException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out InflateException");
            }
        }
    }
}

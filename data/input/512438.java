@TestTargetClass(OutOfResourcesException.class)
public class Surface_OutOfResourcesExceptionTest extends AndroidTestCase {
    private static final String NAME = "Test_Surface_OutOfResourcesException";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Surface.OutOfResourcesException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Surface.OutOfResourcesException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new Surface.OutOfResourcesException();
        new Surface.OutOfResourcesException(NAME);
    }
}

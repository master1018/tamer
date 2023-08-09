@TestTargetClass(ParcelFormatException.class)
public class ParcelFormatExceptionTest extends TestCase{
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method: ParcelFormatException",
            method = "ParcelFormatException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method: ParcelFormatException",
            method = "ParcelFormatException",
            args = {java.lang.String.class}
        )
    })
    public void testParcelFormatException(){
        ParcelFormatException ne = null;
        boolean isThrowed = false;
        try {
            ne = new ParcelFormatException();
            throw ne;
        } catch (ParcelFormatException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out ParcelFormatException");
            }
        }
        isThrowed = false;
        try {
            ne = new ParcelFormatException("ParcelFormatException");
            throw ne;
        } catch (ParcelFormatException e) {
            assertSame(ne, e);
            isThrowed = true;
        } finally {
            if (!isThrowed) {
                fail("should throw out ParcelFormatException");
            }
        }
    }
}

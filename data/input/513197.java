@TestTargetClass(DataFormatException.class)
public class DataFormatExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DataFormatException",
        args = {}
    )
    public void testDataFormatException() {
        DataFormatException dfe = new DataFormatException();
        assertEquals(dfe.getMessage(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DataFormatException",
        args = {java.lang.String.class}
    )
    public void testDataFormatExceptionString() {
        DataFormatException dfe = new DataFormatException("Test");
        assertEquals(dfe.getMessage(), "Test");
    }
}

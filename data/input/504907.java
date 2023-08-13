@TestTargetClass(UnsupportedClassVersionError.class) 
public class UnsupportedClassVersionErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsupportedClassVersionError",
        args = {}
    )
    public void test_Constructor() {
        UnsupportedClassVersionError ucve = new UnsupportedClassVersionError();
        assertNull(ucve.getMessage());
        assertNull(ucve.getCause());        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnsupportedClassVersionError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLString() {
        String message = "Test message";
        UnsupportedClassVersionError ucve = new UnsupportedClassVersionError(
                message);
        assertEquals(message, ucve.getMessage());
        ucve = new UnsupportedClassVersionError(null);
        assertNull(ucve.getMessage());
    }
}

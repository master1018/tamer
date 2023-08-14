@TestTargetClass(ClassFormatError.class) 
public class ClassFormatErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassFormatError",
        args = {}
    )
    public void test_ClassFormatError() {
      ClassFormatError cfe = new ClassFormatError();
      assertNull(cfe.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassFormatError",
        args = {java.lang.String.class}
    )
    public void test_ClassFormatError_String() {
      String message = "Test message";
      ClassFormatError cfe = new ClassFormatError(message);
      assertEquals(message, cfe.getMessage());
    }    
}

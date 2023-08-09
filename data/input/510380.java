@TestTargetClass(ClassCircularityError.class) 
public class ClassCircularityErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassCircularityError",
        args = {}
    )
    public void test_ClassCircularityError() {
      ClassCircularityError cce = new ClassCircularityError();
      assertNull(cce.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassCircularityError",
        args = {java.lang.String.class}
    )
    public void test_ClassCircularityError_String() {
      String message = "Test message";
      ClassCircularityError cce = new ClassCircularityError(message);
      assertEquals(message, cce.getMessage());
    }    
}

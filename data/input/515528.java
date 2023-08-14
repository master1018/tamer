@TestTargetClass(EnumConstantNotPresentException.class) 
public class EnumConstantNotPresentExceptionTest extends TestCase {
    public enum Fixture {
        ONE,TWO,THREE
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EnumConstantNotPresentException",
        args = {java.lang.Class.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_ClassLjava_lang_String() {
        String tm = "Test Message";
        EnumConstantNotPresentException ecnpe = new 
                    EnumConstantNotPresentException(Fixture.class, tm);
        assertEquals("Constant name is incorrect: " + ecnpe.constantName() + 
                " instead of " + tm, tm, ecnpe.constantName());
        try {
            new EnumConstantNotPresentException(null, "");
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "enumType",
        args = {}
    )
    public void test_enumType() {
        EnumConstantNotPresentException e = new EnumConstantNotPresentException(Fixture.class, "FOUR");
        assertEquals(Fixture.class, e.enumType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "constantName",
        args = {}
    )
    public void test_constantName() {
        EnumConstantNotPresentException e = new EnumConstantNotPresentException(Fixture.class, "FOUR");
        assertEquals("FOUR", e.constantName());
    }
}

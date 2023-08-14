@TestTargetClass(ParserConfigurationException.class) 
public class ParserConfigurationExceptionTest extends TestCase{
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ParserConfigurationException",
        args = {}
    )
    public void test_Constructor() {
        ParserConfigurationException pce = new ParserConfigurationException();
        assertNull(pce.getMessage());
        assertNull(pce.getLocalizedMessage());
        assertNull(pce.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ParserConfigurationException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        ParserConfigurationException pce =
            new ParserConfigurationException("Oops!");
        assertEquals("Oops!", pce.getMessage());
        assertEquals("Oops!", pce.getLocalizedMessage());
        assertNull(pce.getCause());
    }
}

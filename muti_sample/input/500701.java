@TestTargetClass(MalformedURLException.class) 
public class MalformedURLExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MalformedURLException",
        args = {}
    )
    public void test_Constructor() {
        boolean passed;
        passed = false;
        try {
            new URL("notAProtocol:
        } catch (MalformedURLException e) {
            passed = true;
        } catch (Exception e) {
            fail("Wrong exception thrown : " + e.getMessage());
        }
        assertTrue("Failed to throw correct exception", passed);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MalformedURLException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        final String myString = "Gawsh!";
        try {
            if (true)
                throw new MalformedURLException(myString);
        } catch (MalformedURLException e) {
            assertTrue("Incorrect exception text", e.toString().indexOf(
                    myString) >= 0);
            return;
        }
        fail("Exception not thrown");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

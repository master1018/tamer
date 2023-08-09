@TestTargetClass(EOFException.class) 
public class EOFExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "EOFException",
        args = {}
    )   
    public void test_Constructor() {
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[1]))
                    .readShort();
            fail("Test 1: EOFException expected.");
        } catch (EOFException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        } catch (Exception e) {
            fail("Test 3: Unexpected exception: " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "EOFException",
        args = {java.lang.String.class}
    )      
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new EOFException("Something went wrong.");
            fail("Test 1: EOFException expected.");
        } catch (EOFException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

@TestTargetClass(InvalidClassException.class) 
public class InvalidClassExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidClassException",
        args = {java.lang.String.class}
    )    
    public void test_ConstructorLjava_lang_String() {
        final String message = "A message";
        try {
            if (true)
                throw new java.io.InvalidClassException(message);
        } catch (InvalidClassException e) {
            assertTrue("Incorrect message read", e.getMessage().equals(message));
            return;
        }
        fail("Failed to throw exception");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "InvalidClassException",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMessage",
            args = {}
        )
    })        
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        final String message = "A message";
        final String className = "Object";
        try {
            if (true)
                throw new java.io.InvalidClassException(className, message);
        } catch (InvalidClassException e) {
            String returnedMessage = e.getMessage();
            assertTrue("Incorrect message read: " + e.getMessage(),
                    returnedMessage.indexOf(className) >= 0
                            && returnedMessage.indexOf(message) >= 0);
            return;
        }
        fail("Failed to throw exception");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

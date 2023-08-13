@TestTargetClass(ObjectStreamException.class) 
public class ObjectStreamExceptionTest extends junit.framework.TestCase {
    class MyObjectStreamException extends ObjectStreamException {
        private static final long serialVersionUID = 1L;
        public MyObjectStreamException() {
            super();
        }
        public MyObjectStreamException(String detailMessage) {
            super(detailMessage);
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ObjectStreamException",
            args = {}
        )     
    public void test_Constructor() {
        try {
            if (true) 
                throw new MyObjectStreamException();
            fail("Test 1: MyObjectStreamException expected.");
        } catch (MyObjectStreamException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ObjectStreamException",
            args = {java.lang.String.class}
        )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new MyObjectStreamException("Something went wrong.");
            fail("Test 1: MyObjectStreamException expected.");
        } catch (MyObjectStreamException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}

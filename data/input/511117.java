public class BadPaddingExceptionTest extends SerializationTest {
    public static String[] msgs = {
            "New message",
            "Long message for Exception. Long message for Exception. Long message for Exception." };
    protected Object[] getData() {
        return new Object[] { new BadPaddingException(),
                new BadPaddingException(null), new BadPaddingException(msgs[1]) };
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BadPaddingExceptionTest.class);
    }
}

@TestTargetClass(NotImplementedException.class)
public class NYITest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NotImplementedException",
        args = {java.io.PrintStream.class}
    )      
    public void testNYI() throws UnsupportedEncodingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(400);
        PrintStream stream = new PrintStream(bos, true, "UTF-8");
        new NotImplementedException(stream);
        String message = new String(bos.toByteArray(), "UTF-8");
        assertFalse(message.indexOf("NYITest") == -1);
    }
}

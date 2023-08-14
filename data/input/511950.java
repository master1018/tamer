@TestTargetClass(java.nio.charset.Charset.class)
public class UTF16CharsetTest extends AbstractCharsetTestCase {
    public UTF16CharsetTest() {
        super("UTF-16", new String[] { "UTF_16" }, true, true);
    }
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Empty test.",
        method = "",
        args = {}
    )
    public void testEncode_Normal() {
    }
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Empty test.",
        method = "",
        args = {}
    )
    public void testDecode_Normal() {
    }
}

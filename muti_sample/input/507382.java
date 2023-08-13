@TestTargetClass(java.nio.charset.Charset.class)
public class UTF16LECharsetTest extends AbstractCharsetTestCase {
    public UTF16LECharsetTest() {
        super("UTF-16LE", new String[] { "UTF_16LE", "X-UTF-16LE" },
                true, true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functional test, text source: AbstractCharsetTestCase.internalTestEncode. Exceptions checking missed.",
        method = "encode",
        args = {java.lang.String.class}
    )
    public void testEncode_Normal() {
        String input = "ab\u5D14\u654F";
        byte[] output = new byte[] { 97, 0, 98, 0, 20, 93, 79, 101 };
        internalTestEncode(input, output);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functional test, text source: AbstractCharsetTestCase.internalTestDecode. Exceptions checking missed.",
        method = "decode",
        args = {java.nio.ByteBuffer.class}
    )
    public void testDecode_Normal() {
        byte[] input = new byte[] { 97, 0, 98, 0, 20, 93, 79, 101 };
        char[] output = "ab\u5D14\u654F".toCharArray();
        internalTestDecode(input, output);
    }
}

@TestTargetClass(java.nio.charset.Charset.class)
public class UTF16BECharsetTest extends AbstractCharsetTestCase {
    public UTF16BECharsetTest() {
        super("UTF-16BE", new String[] { "X-UTF-16BE", "UTF_16BE" },
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
        byte[] output = new byte[] { 0, 97, 0, 98, 93, 20, 101, 79 };
        internalTestEncode(input, output);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functional test, text source: AbstractCharsetTestCase.internalTestDecode. Exceptions checking missed.",
        method = "decode",
        args = {java.nio.ByteBuffer.class}
    )
    public void testDecode_Normal() {
        byte[] input = new byte[] { 0, 97, 0, 98, 93, 20, 101, 79 };
        char[] output = "ab\u5D14\u654F".toCharArray();
        internalTestDecode(input, output);
    }
}

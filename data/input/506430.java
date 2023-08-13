@TestTargetClass(java.nio.charset.Charset.class)
public class UTF8CharsetTest extends AbstractCharsetTestCase {
    public UTF8CharsetTest() {
        super("UTF-8", new String[] { "UTF8" }, true, true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functional test, text source: AbstractCharsetTestCase.internalTestDecode. Exceptions checking missed.",
        method = "decode",
        args = {java.nio.ByteBuffer.class}
    )
    public void testDecode_Normal() {
        byte[] input = new byte[] { 97, 98, -27, -76, -108, -26, -107, -113 };
        char[] output = "ab\u5D14\u654F".toCharArray();
        internalTestDecode(input, output);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Functional test, text source: AbstractCharsetTestCase.internalTestEncode. Exceptions checking missed.",
        method = "encode",
        args = {java.lang.String.class}
    )
    public void testEncode_Normal() {
        String input = "ab\u5D14\u654F";
        byte[] output = new byte[] { 97, 98, -27, -76, -108, -26, -107, -113 };
        internalTestEncode(input, output);
    }
}

@TestTargetClass(java.nio.charset.Charset.class)
public class ASCCharsetTest extends AbstractCharsetTestCase {
    public ASCCharsetTest() {
        super("US-ASCII", new String[] { "ISO646-US", "ASCII", "cp367",
                "ascii7", "ANSI_X3.4-1986", "iso-ir-6", "us", "646",
                "iso_646.irv:1983", "csASCII", "ANSI_X3.4-1968",
                "ISO_646.irv:1991" }, true, true); 
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "encode",
        args = {java.lang.String.class}
    )
    public void testEncode_Normal() {
        String input = "ab\u5D14\u654F";
        byte[] output = new byte[] { 97, 98,
                this.testingCharset.newEncoder().replacement()[0],
                this.testingCharset.newEncoder().replacement()[0] };
        internalTestEncode(input, output);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "decode",
        args = {java.nio.ByteBuffer.class}
    )
    public void testDecode_Normal() {
        byte[] input = new byte[] { 97, 98, 63, 63 };
        char[] output = "ab??".toCharArray();
        internalTestDecode(input, output);
    }
}

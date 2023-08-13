@TestTargetClass(java.nio.charset.Charset.class)
public class ISOCharsetTest extends AbstractCharsetTestCase {
    public ISOCharsetTest() {
        super("ISO-8859-1", new String[] { "iso-ir-100", "8859_1",
                "ISO_8859-1", "ISO8859_1", "819", "csISOLatin1", "IBM-819",
                "ISO_8859-1:1987", "latin1", "cp819", "ISO8859-1", "IBM819",
                "ISO_8859_1", "l1" }, true, true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "ATTENTION: THIS TEST IS FOR THE ISOCharsetEncoder.encode AND NOT THE TARGET ANNOTATED!!! the Functional test, text source: AbstractCharsetTestCase.internalTestEncode. Exceptions checking missed.",
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "ATTENTION: THIS TEST IS FOR THE ISOCharsetEncoder.encode AND NOT THE TARGET ANNOTATED!!! Functional test, text source: AbstractCharsetTestCase.internalTestDecode. Exceptions checking missed.",
            method = "decode",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testDecode_Normal() {
        byte[] input = new byte[] { 97, 98, 63, 63 };
        char[] output = "ab??".toCharArray();
        internalTestDecode(input, output);
    }
}

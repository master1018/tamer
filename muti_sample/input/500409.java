public class SimpleIcsWriterTests extends TestCase {
    private static final String CRLF = "\r\n";
    private static final String UTF8_1_BYTE = "a";
    private static final String UTF8_2_BYTES = "\u00A2";
    private static final String UTF8_3_BYTES = "\u20AC";
    private static final String UTF8_4_BYTES = "\uD852\uDF62";
    public void testWriteTag() {
        final SimpleIcsWriter ics = new SimpleIcsWriter();
        ics.writeTag("TAG1", null);
        ics.writeTag("TAG2", "");
        ics.writeTag("TAG3", "xyz");
        ics.writeTag("SUMMARY", "TEST-TEST,;\r\n\\TEST");
        ics.writeTag("SUMMARY2", "TEST-TEST,;\r\n\\TEST");
        final String actual = Utility.fromUtf8(ics.getBytes());
        assertEquals(
                "TAG3:xyz" + CRLF +
                "SUMMARY:TEST-TEST\\,\\;\\n\\\\TEST" + CRLF + 
                "SUMMARY2:TEST-TEST,;\r\n\\TEST" + CRLF 
                , actual);
    }
    public void testWriteLine() {
        for (String last : new String[] {UTF8_1_BYTE, UTF8_2_BYTES, UTF8_3_BYTES, UTF8_4_BYTES}) {
            for (int i = 70; i < 160; i++) {
                String input = stringOfLength(i) + last;
                checkWriteLine(input);
            }
        }
    }
    private static String stringOfLength(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('0' +(i % 10));
        }
        return sb.toString();
    }
    private void checkWriteLine(String input) {
        final SimpleIcsWriter ics = new SimpleIcsWriter();
        ics.writeLine(input);
        final byte[] bytes = ics.getBytes();
        int numBytes = 0;
        for (byte b : bytes) {
            if (b == '\r') {
                continue; 
            }
            if (b == '\n') {
                assertTrue("input=" + input, numBytes <= 75);
                numBytes = 0;
                continue;
            }
            numBytes++;
        }
        assertTrue("input=" + input, numBytes <= 75);
        final String actual = Utility.fromUtf8(bytes);
        final String unfolded = actual.replace("\r\n\t", "");
        assertEquals("input=" + input, input + "\r\n", unfolded);
    }
    public void testQuoteParamValue() {
        assertNull(SimpleIcsWriter.quoteParamValue(null));
        assertEquals("\"\"", SimpleIcsWriter.quoteParamValue(""));
        assertEquals("\"a\"", SimpleIcsWriter.quoteParamValue("a"));
        assertEquals("\"''\"", SimpleIcsWriter.quoteParamValue("\"'"));
        assertEquals("\"abc\"", SimpleIcsWriter.quoteParamValue("abc"));
        assertEquals("\"a'b'c\"", SimpleIcsWriter.quoteParamValue("a\"b\"c"));
    }
}

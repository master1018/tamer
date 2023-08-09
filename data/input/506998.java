public class GsmAlphabetTest extends TestCase {
    private static final String sGsmExtendedChars = "{|}\\[~]\f\u20ac";
    @SmallTest
    public void test7bitWithHeader() throws Exception {
        SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
        concatRef.refNumber = 1;
        concatRef.seqNumber = 2;
        concatRef.msgCount = 2;
        concatRef.isEightBits = true;
        SmsHeader header = new SmsHeader();
        header.concatRef = concatRef;
        String message = "aaaaaaaaaabbbbbbbbbbcccccccccc";
        byte[] userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message,
                SmsHeader.toByteArray(header));
        int septetCount = GsmAlphabet.countGsmSeptets(message, false);
        String parsedMessage = GsmAlphabet.gsm7BitPackedToString(
                userData, SmsHeader.toByteArray(header).length+2, septetCount, 1);
        assertEquals(message, parsedMessage);
    }
    @LargeTest
    public void testBasic() throws Exception {
        assertEquals(0, GsmAlphabet.charToGsm('@'));
        assertEquals(0x7f, GsmAlphabet.charToGsm('\u00e0'));
        for (int i = 0, s = sGsmExtendedChars.length(); i < s; i++) {
            assertEquals(GsmAlphabet.GSM_EXTENDED_ESCAPE,
                    GsmAlphabet.charToGsm(sGsmExtendedChars.charAt(i)));
        }
        assertEquals(GsmAlphabet.GSM_EXTENDED_ESCAPE,
                GsmAlphabet.charToGsm('\u20ac'));
        assertEquals(GsmAlphabet.charToGsm(' '),
                GsmAlphabet.charToGsm('\u00a2'));
        assertEquals(1, GsmAlphabet.countGsmSeptets('\u00a2'));
        for (int i = 0, s = sGsmExtendedChars.length(); i < s; i++) {
            assertEquals(sGsmExtendedChars.charAt(i),
                    GsmAlphabet.gsmExtendedToChar(
                            GsmAlphabet.charToGsmExtended(sGsmExtendedChars.charAt(i))));
        }
        assertEquals(GsmAlphabet.charToGsm(' '),
                GsmAlphabet.charToGsmExtended('@'));
        assertEquals('@', GsmAlphabet.gsmToChar(0));
        assertEquals('\u00e0', GsmAlphabet.gsmToChar(0x7f));
        assertEquals('\uffff',
                GsmAlphabet.gsmToChar(GsmAlphabet.GSM_EXTENDED_ESCAPE));
        assertEquals(' ', GsmAlphabet.gsmToChar(0x80));
        assertEquals('{', GsmAlphabet.gsmExtendedToChar(0x28));
        assertEquals(' ', GsmAlphabet.gsmExtendedToChar(
                GsmAlphabet.GSM_EXTENDED_ESCAPE));
        assertEquals(' ', GsmAlphabet.gsmExtendedToChar(0));
        byte[] packed;
        StringBuilder testString = new StringBuilder(300);
        for (int i = 0; i < 9; i++, testString.append('@')) {
            packed = GsmAlphabet.stringToGsm7BitPacked(testString.toString());
            assertEquals(testString.toString(),
                    GsmAlphabet.gsm7BitPackedToString(packed, 1, 0xff & packed[0]));
        }
        for (int i = 0; i < 0x80; i++) {
            char c;
            if (i == GsmAlphabet.GSM_EXTENDED_ESCAPE) {
                continue;
            }
            c = GsmAlphabet.gsmToChar(i);
            testString.append(c);
            assertEquals(1, GsmAlphabet.countGsmSeptets(c));
        }
        packed = GsmAlphabet.stringToGsm7BitPacked(testString.toString());
        assertEquals(testString.toString(),
                GsmAlphabet.gsm7BitPackedToString(packed, 1, 0xff & packed[0]));
        testString.append(sGsmExtendedChars);
        for (int i = 0, s = sGsmExtendedChars.length(); i < s; i++) {
            assertEquals(2, GsmAlphabet.countGsmSeptets(sGsmExtendedChars.charAt(i)));
        }
        packed = GsmAlphabet.stringToGsm7BitPacked(testString.toString());
        assertEquals(testString.toString(),
                GsmAlphabet.gsm7BitPackedToString(packed, 1, 0xff & packed[0]));
        testString.setLength(0);
        for (int i = 0; i < 255; i++) {
            testString.append('@');
        }
        packed = GsmAlphabet.stringToGsm7BitPacked(testString.toString());
        assertEquals(testString.toString(),
                GsmAlphabet.gsm7BitPackedToString(packed, 1, 0xff & packed[0]));
        testString.append('@');
        try {
            GsmAlphabet.stringToGsm7BitPacked(testString.toString());
            fail("expected exception");
        } catch (EncodeException ex) {
        }
        testString.setLength(0);
        for (int i = 0; i < (255 / 2); i++) {
            testString.append('{');
        }
        packed = GsmAlphabet.stringToGsm7BitPacked(testString.toString());
        assertEquals(testString.toString(),
                GsmAlphabet.gsm7BitPackedToString(packed, 1, 0xff & packed[0]));
        testString.append('{');
        try {
            GsmAlphabet.stringToGsm7BitPacked(testString.toString());
            fail("expected exception");
        } catch (EncodeException ex) {
        }
        byte unpacked[];
        unpacked = IccUtils.hexStringToBytes("566F696365204D61696C");
        assertEquals("Voice Mail",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 0, unpacked.length));
        assertEquals(IccUtils.bytesToHexString(unpacked),
                IccUtils.bytesToHexString(
                        GsmAlphabet.stringToGsm8BitPacked("Voice Mail")));
        unpacked = GsmAlphabet.stringToGsm8BitPacked(sGsmExtendedChars);
        assertEquals(2 * sGsmExtendedChars.length(), unpacked.length);
        assertEquals(sGsmExtendedChars,
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 0, unpacked.length));
        assertEquals(2 * sGsmExtendedChars.length(), unpacked.length);
        unpacked = new byte[3];
        GsmAlphabet.stringToGsm8BitUnpackedField(sGsmExtendedChars, unpacked,
                0, unpacked.length);
        assertEquals(0xff, 0xff & unpacked[2]);
        assertEquals(sGsmExtendedChars.substring(0, 1),
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 0, unpacked.length));
        unpacked = new byte[3];
        GsmAlphabet.stringToGsm8BitUnpackedField("abcd", unpacked,
                0, unpacked.length);
        assertEquals("abc",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 0, unpacked.length));
        unpacked = new byte[3];
        GsmAlphabet.stringToGsm8BitUnpackedField("a{cd", unpacked,
                0, unpacked.length);
        assertEquals("a{",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 0, unpacked.length));
        unpacked = new byte[3];
        GsmAlphabet.stringToGsm8BitUnpackedField("a", unpacked,
                0, unpacked.length);
        assertEquals("a",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 0, unpacked.length));
        assertEquals(0xff, 0xff & unpacked[1]);
        assertEquals(0xff, 0xff & unpacked[2]);
        unpacked[0] = 0;
        unpacked[1] = 0;
        unpacked[2] = GsmAlphabet.GSM_EXTENDED_ESCAPE;
        assertEquals("@@",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 0, unpacked.length));
        assertEquals("@",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 1, unpacked.length - 1));
        unpacked[0] = 0;
        GsmAlphabet.stringToGsm8BitUnpackedField("abcd", unpacked,
                1, unpacked.length - 1);
        assertEquals(0, unpacked[0]);
        assertEquals("ab",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 1, unpacked.length - 1));
        unpacked[0] = 0;
        GsmAlphabet.stringToGsm8BitUnpackedField("a{", unpacked,
                1, unpacked.length - 1);
        assertEquals(0, unpacked[0]);
        assertEquals("a",
                GsmAlphabet.gsm8BitUnpackedToString(unpacked, 1, unpacked.length - 1));
    }
}

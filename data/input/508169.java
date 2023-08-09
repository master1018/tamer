public class PhoneNumberUtilsTest extends TestCase {
    @SmallTest
    public void testExtractNetworkPortion() throws Exception {
        assertEquals(
                "+17005554141",
                PhoneNumberUtils.extractNetworkPortion("+17005554141")
        );
        assertEquals(
                "+17005554141",
                PhoneNumberUtils.extractNetworkPortion("+1 (700).555-4141")
        );
        assertEquals(
                "17005554141",
                PhoneNumberUtils.extractNetworkPortion("1 (700).555-4141")
        );
        assertEquals(
                "17005554141*#",
                PhoneNumberUtils.extractNetworkPortion("1 (700).555-4141*#")
        );
        assertEquals(
                "170055541NN",
                PhoneNumberUtils.extractNetworkPortion("1 (700).555-41NN")
        );
        assertEquals(
                "170055541NN",
                PhoneNumberUtils.extractNetworkPortion("1 (700).555-41NN,1234")
        );
        assertEquals(
                "170055541NN",
                PhoneNumberUtils.extractNetworkPortion("1 (700).555-41NN;1234")
        );
        assertEquals(
                "**21**17005554141#",
                PhoneNumberUtils.extractNetworkPortion("**21**+17005554141#")
        );
        assertEquals("", PhoneNumberUtils.extractNetworkPortion(""));
        assertEquals("", PhoneNumberUtils.extractNetworkPortion(",1234"));
        byte [] b = new byte[20];
        b[0] = (byte) 0x81; b[1] = (byte) 0x71; b[2] = (byte) 0x00; b[3] = (byte) 0x55;
        b[4] = (byte) 0x05; b[5] = (byte) 0x20; b[6] = (byte) 0xF0;
        assertEquals("17005550020",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 7));
        b[0] = (byte) 0x91; b[1] = (byte) 0x71; b[2] = (byte) 0x00; b[3] = (byte) 0x55;
        b[4] = (byte) 0x05; b[5] = (byte) 0x20; b[6] = (byte) 0xF0;
        assertEquals("+17005550020",
                PhoneNumberUtils.calledPartyBCDToString(b, 0, 7));
        byte[] bRet = PhoneNumberUtils.networkPortionToCalledPartyBCD("+17005550020");
        assertEquals(7, bRet.length);
        for (int i = 0; i < 7; i++) {
            assertEquals(b[i], bRet[i]);
        }
        bRet = PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength("+17005550020");
        assertEquals(8, bRet.length);
        assertEquals(bRet[0], 7);
        for (int i = 1; i < 8; i++) {
            assertEquals(b[i - 1], bRet[i]);
        }
        bRet = PhoneNumberUtils.networkPortionToCalledPartyBCD("7005550020");
        assertEquals("7005550020",
            PhoneNumberUtils.calledPartyBCDToString(bRet, 0, bRet.length));
        b[0] = (byte) 0x81; b[1] = (byte) 0x71; b[2] = (byte) 0x00; b[3] = (byte) 0x55;
        b[4] = (byte) 0x05; b[5] = (byte) 0x20; b[6] = (byte) 0xB0;
        assertEquals("17005550020#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 7));
        b[0] = (byte) 0x91; b[1] = (byte) 0x71; b[2] = (byte) 0x00; b[3] = (byte) 0x55;
        b[4] = (byte) 0x05; b[5] = (byte) 0x20; b[6] = (byte) 0xB0;
        assertEquals("+17005550020#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 7));
        b[0] = (byte) 0x81; b[1] = (byte) 0x2A; b[2] = (byte) 0xB1;
        assertEquals("*21#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 3));
        b[0] = (byte) 0x81; b[1] = (byte) 0x2B; b[2] = (byte) 0xB1;
        assertEquals("#21#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 3));
        b[0] = (byte) 0x91; b[1] = (byte) 0x2A; b[2] = (byte) 0xB1;
        assertEquals("*21#+",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 3));
        b[0] = (byte) 0x81; b[1] = (byte) 0xAA; b[2] = (byte) 0x12; b[3] = (byte) 0xFB;
        assertEquals("**21#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 4));
        b[0] = (byte) 0x91; b[1] = (byte) 0xAA; b[2] = (byte) 0x12; b[3] = (byte) 0xFB;
        assertEquals("**21#+",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 4));
        b[0] = (byte) 0x81; b[1] = (byte) 0x9A; b[2] = (byte) 0xA9; b[3] = (byte) 0x71;
        b[4] = (byte) 0x00; b[5] = (byte) 0x55; b[6] = (byte) 0x05; b[7] = (byte) 0x20;
        b[8] = (byte) 0xB0;
        assertEquals("*99*17005550020#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 9));
        b[0] = (byte) 0x91; b[1] = (byte) 0x9A; b[2] = (byte) 0xA9; b[3] = (byte) 0x71;
        b[4] = (byte) 0x00; b[5] = (byte) 0x55; b[6] = (byte) 0x05; b[7] = (byte) 0x20;
        b[8] = (byte) 0xB0;
        assertEquals("*99*+17005550020#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 9));
        b[0] = (byte) 0x81; b[1] = (byte) 0xAA; b[2] = (byte) 0x12; b[3] = (byte) 0x1A;
        b[4] = (byte) 0x07; b[5] = (byte) 0x50; b[6] = (byte) 0x55; b[7] = (byte) 0x00;
        b[8] = (byte) 0x02; b[9] = (byte) 0xFB;
        assertEquals("**21*17005550020#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 10));
        b[0] = (byte) 0x91; b[1] = (byte) 0xAA; b[2] = (byte) 0x12; b[3] = (byte) 0x1A;
        b[4] = (byte) 0x07; b[5] = (byte) 0x50; b[6] = (byte) 0x55; b[7] = (byte) 0x00;
        b[8] = (byte) 0x02; b[9] = (byte) 0xFB;
        assertEquals("**21*+17005550020#",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 10));
        b[0] = (byte) 0x81; b[1] = (byte) 0x2A; b[2] = (byte) 0xA1; b[3] = (byte) 0x71;
        b[4] = (byte) 0x00; b[5] = (byte) 0x55; b[6] = (byte) 0x05; b[7] = (byte) 0x20;
        b[8] = (byte) 0xF0;
        assertEquals("*21*17005550020",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 9));
        b[0] = (byte) 0x91; b[1] = (byte) 0x2A; b[2] = (byte) 0xB1; b[3] = (byte) 0x71;
        b[4] = (byte) 0x00; b[5] = (byte) 0x55; b[6] = (byte) 0x05; b[7] = (byte) 0x20;
        b[8] = (byte) 0xF0;
        assertEquals("*21#+17005550020",
            PhoneNumberUtils.calledPartyBCDToString(b, 0, 9));
        assertNull(PhoneNumberUtils.extractNetworkPortion(null));
        assertNull(PhoneNumberUtils.extractPostDialPortion(null));
        assertTrue(PhoneNumberUtils.compare(null, null));
        assertFalse(PhoneNumberUtils.compare(null, "123"));
        assertFalse(PhoneNumberUtils.compare("123", null));
        assertNull(PhoneNumberUtils.toCallerIDMinMatch(null));
        assertNull(PhoneNumberUtils.getStrippedReversed(null));
        assertNull(PhoneNumberUtils.stringFromStringAndTOA(null, 1));
    }
    @SmallTest
    public void testExtractNetworkPortionAlt() throws Exception {
        assertEquals(
                "+17005554141",
                PhoneNumberUtils.extractNetworkPortionAlt("+17005554141")
        );
        assertEquals(
                "+17005554141",
                PhoneNumberUtils.extractNetworkPortionAlt("+1 (700).555-4141")
        );
        assertEquals(
                "17005554141",
                PhoneNumberUtils.extractNetworkPortionAlt("1 (700).555-4141")
        );
        assertEquals(
                "17005554141*#",
                PhoneNumberUtils.extractNetworkPortionAlt("1 (700).555-4141*#")
        );
        assertEquals(
                "170055541NN",
                PhoneNumberUtils.extractNetworkPortionAlt("1 (700).555-41NN")
        );
        assertEquals(
                "170055541NN",
                PhoneNumberUtils.extractNetworkPortionAlt("1 (700).555-41NN,1234")
        );
        assertEquals(
                "170055541NN",
                PhoneNumberUtils.extractNetworkPortionAlt("1 (700).555-41NN;1234")
        );
        assertEquals(
                "**21**+17005554141#",
                PhoneNumberUtils.extractNetworkPortionAlt("**21**+17005554141#")
        );
        assertEquals(
                "*31#+447966164208",
                PhoneNumberUtils.extractNetworkPortionAlt("*31#+447966164208")
        );
        assertEquals(
                "*31#+447966164208",
                PhoneNumberUtils.extractNetworkPortionAlt("*31# (+44) 79 6616 4208")
        );
        assertEquals("", PhoneNumberUtils.extractNetworkPortionAlt(""));
        assertEquals("", PhoneNumberUtils.extractNetworkPortionAlt(",1234"));
        assertNull(PhoneNumberUtils.extractNetworkPortionAlt(null));
    }
    @SmallTest
    public void testB() throws Exception {
        assertEquals("", PhoneNumberUtils.extractPostDialPortion("+17005554141"));
        assertEquals("", PhoneNumberUtils.extractPostDialPortion("+1 (700).555-4141"));
        assertEquals("", PhoneNumberUtils.extractPostDialPortion("+1 (700).555-41NN"));
        assertEquals(",1234", PhoneNumberUtils.extractPostDialPortion("+1 (700).555-41NN,1234"));
        assertEquals(";1234", PhoneNumberUtils.extractPostDialPortion("+1 (700).555-41NN;1234"));
        assertEquals(";1234,;N",
                PhoneNumberUtils.extractPostDialPortion("+1 (700).555-41NN;1-2.34 ,;N"));
    }
    @SmallTest
    public void testCompare() throws Exception {
        assertFalse(PhoneNumberUtils.compare("", ""));
        assertTrue(PhoneNumberUtils.compare("911", "911"));
        assertFalse(PhoneNumberUtils.compare("911", "18005550911"));
        assertTrue(PhoneNumberUtils.compare("5555", "5555"));
        assertFalse(PhoneNumberUtils.compare("5555", "180055555555"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "+17005554141"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "+1 (700).555-4141"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "+1 (700).555-4141,1234"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "17005554141"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "7005554141"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "5554141"));
        assertTrue(PhoneNumberUtils.compare("17005554141", "5554141"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "01117005554141"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "0017005554141"));
        assertTrue(PhoneNumberUtils.compare("17005554141", "0017005554141"));
        assertTrue(PhoneNumberUtils.compare("+17005554141", "**31#+17005554141"));
        assertFalse(PhoneNumberUtils.compare("+1 999 7005554141", "+1 7005554141"));
        assertTrue(PhoneNumberUtils.compare("011 1 7005554141", "7005554141"));
        assertFalse(PhoneNumberUtils.compare("011 11 7005554141", "+17005554141"));
        assertFalse(PhoneNumberUtils.compare("+17005554141", "7085882300"));
        assertTrue(PhoneNumberUtils.compare("+44 207 792 3490", "0 207 792 3490"));
        assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "00 207 792 3490"));
        assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "011 207 792 3490"));
        assertFalse(PhoneNumberUtils.compare("+17005554141", "**31#17005554141"));
        assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "+44 (0) 207 792 3490"));
        assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "010 44 207 792 3490"));
        assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "0011 44 207 792 3490"));
        assertFalse(PhoneNumberUtils.compare("+7(095)9100766", "8(095)9100766"));
        assertTrue(PhoneNumberUtils.compare("+444 207 792 3490", "0 207 792 3490"));
        assertTrue(PhoneNumberUtils.compare("404-04", "40404"));
    }
    @SmallTest
    public void testToCallerIDIndexable() throws Exception {
        assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("17005554141"));
        assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-4141"));
        assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-4141,1234"));
        assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-4141;1234"));
        assertEquals("NN14555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-41NN"));
        assertEquals("", PhoneNumberUtils.toCallerIDMinMatch(""));
        assertEquals("0032", PhoneNumberUtils.toCallerIDMinMatch("2300"));
        assertEquals("0032+", PhoneNumberUtils.toCallerIDMinMatch("+2300"));
        assertEquals("#130#*", PhoneNumberUtils.toCallerIDMinMatch("*#031#"));
    }
    @SmallTest
    public void testGetIndexable() throws Exception {
        assertEquals("14145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-4141"));
        assertEquals("14145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-4141,1234"));
        assertEquals("14145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-4141;1234"));
        assertEquals("NN145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-41NN"));
        assertEquals("", PhoneNumberUtils.getStrippedReversed(""));
        assertEquals("0032", PhoneNumberUtils.getStrippedReversed("2300"));
        assertEquals("0032+", PhoneNumberUtils.getStrippedReversed("+2300"));
        assertEquals("#130#*", PhoneNumberUtils.getStrippedReversed("*#031#"));
    }
    @SmallTest
    public void testNanpFormatting() {
        SpannableStringBuilder number = new SpannableStringBuilder();
        number.append("8005551212");
        PhoneNumberUtils.formatNanpNumber(number);
        assertEquals("800-555-1212", number.toString());
        number.clear();
        number.append("800555121");
        PhoneNumberUtils.formatNanpNumber(number);
        assertEquals("800-555-121", number.toString());
        number.clear();
        number.append("555-1212");
        PhoneNumberUtils.formatNanpNumber(number);
        assertEquals("555-1212", number.toString());
        number.clear();
        number.append("800-55512");
        PhoneNumberUtils.formatNanpNumber(number);
        assertEquals("800-555-12", number.toString());
        number.clear();
        number.append("46645");
        PhoneNumberUtils.formatNanpNumber(number);
        assertEquals("46645", number.toString());
    }
    @SmallTest
    public void testConvertKeypadLettersToDigits() {
        assertEquals("1-800-4664-411",
                     PhoneNumberUtils.convertKeypadLettersToDigits("1-800-GOOG-411"));
        assertEquals("18004664411",
                     PhoneNumberUtils.convertKeypadLettersToDigits("1800GOOG411"));
        assertEquals("1-800-466-4411",
                     PhoneNumberUtils.convertKeypadLettersToDigits("1-800-466-4411"));
        assertEquals("18004664411",
                     PhoneNumberUtils.convertKeypadLettersToDigits("18004664411"));
        assertEquals("222-333-444-555-666-7777-888-9999",
                     PhoneNumberUtils.convertKeypadLettersToDigits(
                             "ABC-DEF-GHI-JKL-MNO-PQRS-TUV-WXYZ"));
        assertEquals("222-333-444-555-666-7777-888-9999",
                     PhoneNumberUtils.convertKeypadLettersToDigits(
                             "abc-def-ghi-jkl-mno-pqrs-tuv-wxyz"));
        assertEquals("(800) 222-3334",
                     PhoneNumberUtils.convertKeypadLettersToDigits("(800) ABC-DEFG"));
    }
    public void testCheckAndProcessPlusCode() {
        assertEquals("0118475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+8475797000"));
        assertEquals("18475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+18475797000"));
        assertEquals("0111234567",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+1234567"));
        assertEquals("01123456700000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+23456700000"));
        assertEquals("01111875767800",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+11875767800"));
        assertEquals("8475797000,18475231753",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000,+18475231753"));
        assertEquals("0118475797000,18475231753",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+8475797000,+18475231753"));
        assertEquals("8475797000;0118469312345",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000;+8469312345"));
        assertEquals("8475797000,0111234567",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000,+1234567"));
        assertEquals("847597000;01111875767000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("847597000;+11875767000"));
        assertEquals("8475797000,,0118469312345",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000,,+8469312345"));
        assertEquals("8475797000;,0118469312345",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000;,+8469312345"));
        assertEquals("8475797000,;18475231753",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000,;+18475231753"));
        assertEquals("8475797000;,01111875767000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000;,+11875767000"));
        assertEquals("8475797000,;01111875767000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000,;+11875767000"));
        assertEquals("8475797000,,,01111875767000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000,,,+11875767000"));
        assertEquals("8475797000;,,01111875767000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000;,,+11875767000"));
        assertEquals("+;,8475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+;,8475797000"));
        assertEquals("8475797000,",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("8475797000,"));
        assertEquals("847+579-7000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("847+579-7000"));
        assertEquals(",8475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode(",8475797000"));
        assertEquals(";;8475797000,,",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode(";;8475797000,,"));
        assertEquals("+this+is$weird;,+",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode("+this+is$weird;,+"));
        assertEquals("",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCode(""));
        assertNull(PhoneNumberUtils.cdmaCheckAndProcessPlusCode(null));
    }
    @SmallTest
    public void testCheckAndProcessPlusCodeByNumberFormat() {
        assertEquals("18475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat("+18475797000",
                PhoneNumberUtils.FORMAT_NANP,PhoneNumberUtils.FORMAT_NANP));
        assertEquals("+18475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat("+18475797000",
                PhoneNumberUtils.FORMAT_NANP,PhoneNumberUtils.FORMAT_JAPAN));
        assertEquals("+18475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat("+18475797000",
                PhoneNumberUtils.FORMAT_NANP,PhoneNumberUtils.FORMAT_UNKNOWN));
        assertEquals("+18475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat("+18475797000",
                PhoneNumberUtils.FORMAT_JAPAN,PhoneNumberUtils.FORMAT_JAPAN));
        assertEquals("+18475797000",
                PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat("+18475797000",
                PhoneNumberUtils.FORMAT_UNKNOWN,PhoneNumberUtils.FORMAT_UNKNOWN));
    }
}

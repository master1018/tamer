public class VCardUtilsTests extends TestCase {
    public void testContainsOnlyPrintableAscii() {
        assertTrue(VCardUtils.containsOnlyPrintableAscii((String)null));
        assertTrue(VCardUtils.containsOnlyPrintableAscii((String[])null));
        assertTrue(VCardUtils.containsOnlyPrintableAscii((List<String>)null));
        assertTrue(VCardUtils.containsOnlyPrintableAscii(""));
        assertTrue(VCardUtils.containsOnlyPrintableAscii("abcdefghijklmnopqrstuvwxyz"));
        assertTrue(VCardUtils.containsOnlyPrintableAscii("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        StringBuilder builder = new StringBuilder();
        for (int i = 0x20; i < 0x7F; i++) {
            builder.append((char)i);
        }
        assertTrue(VCardUtils.containsOnlyPrintableAscii(builder.toString()));
        assertTrue(VCardUtils.containsOnlyPrintableAscii("\r\n"));
        assertFalse(VCardUtils.containsOnlyPrintableAscii("\u0019"));
        assertFalse(VCardUtils.containsOnlyPrintableAscii("\u007F"));
    }
    public void testContainsOnlyNonCrLfPrintableAscii() {
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii((String)null));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii((String[])null));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii((List<String>)null));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii(""));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii("abcdefghijklmnopqrstuvwxyz"));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        StringBuilder builder = new StringBuilder();
        for (int i = 0x20; i < 0x7F; i++) {
            builder.append((char)i);
        }
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii(builder.toString()));
        assertFalse(VCardUtils.containsOnlyNonCrLfPrintableAscii("\u0019"));
        assertFalse(VCardUtils.containsOnlyNonCrLfPrintableAscii("\u007F"));
        assertFalse(VCardUtils.containsOnlyNonCrLfPrintableAscii("\r"));
        assertFalse(VCardUtils.containsOnlyNonCrLfPrintableAscii("\n"));
    }
    public void testContainsOnlyAlphaDigitHyphen() {
        assertTrue(VCardUtils.containsOnlyAlphaDigitHyphen((String)null));
        assertTrue(VCardUtils.containsOnlyAlphaDigitHyphen((String[])null));
        assertTrue(VCardUtils.containsOnlyAlphaDigitHyphen((List<String>)null));
        assertTrue(VCardUtils.containsOnlyAlphaDigitHyphen(""));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii("abcdefghijklmnopqrstuvwxyz"));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertTrue(VCardUtils.containsOnlyNonCrLfPrintableAscii("0123456789-"));
        for (int i = 0; i < 0x30; i++) {
            if (i == 0x2D) {  
                continue;
            }
            assertFalse(VCardUtils.containsOnlyAlphaDigitHyphen(String.valueOf((char)i)));
        }
        for (int i = 0x3A; i < 0x41; i++) {
            assertFalse(VCardUtils.containsOnlyAlphaDigitHyphen(String.valueOf((char)i)));
        }
        for (int i = 0x5B; i < 0x61; i++) {
            assertFalse(VCardUtils.containsOnlyAlphaDigitHyphen(String.valueOf((char)i)));
        }
        for (int i = 0x7B; i < 0x100; i++) {
            assertFalse(VCardUtils.containsOnlyAlphaDigitHyphen(String.valueOf((char)i)));
        }
    }
}

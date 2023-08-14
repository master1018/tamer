public class StaticLayoutBidiTest extends TestCase {
    public static final int REQ_DL = 2; 
    public static final int REQ_DR = -2; 
    public static final int REQ_L = 1; 
    public static final int REQ_R = -1; 
    public static final int L = Layout.DIR_LEFT_TO_RIGHT;
    public static final int R = Layout.DIR_RIGHT_TO_LEFT;
    public static final String SP = " ";
    public static final String ALEF = "\u05d0";
    public static final String BET = "\u05d1";
    public static final String GIMEL = "\u05d2";
    public static final String DALET = "\u05d3";
    public void testAllLtr() {
        expectBidi(REQ_DL, "a test", "000000", L);
    }
    public void testLtrRtl() {
        expectBidi(REQ_DL, "abc " + ALEF + BET + GIMEL, "0000111", L);
    }
    public void testAllRtl() {
        expectBidi(REQ_DL, ALEF + SP + ALEF + BET + GIMEL + DALET, "111111", R);
    }
    public void testRtlLtr() {
        expectBidi(REQ_DL,  ALEF + BET + GIMEL + " abc", "1111000", R);
    }
    public void testRAllLtr() {
        expectBidi(REQ_R, "a test", "000000", R);
    }
    public void testRLtrRtl() {
        expectBidi(REQ_R, "abc " + ALEF + BET + GIMEL, "0001111", R);
    }
    public void testLAllRtl() {
        expectBidi(REQ_L, ALEF + SP + ALEF + BET + GIMEL + DALET, "111111", L);
    }
    public void testLRtlLtr() {
        expectBidi(REQ_L,  ALEF + BET + GIMEL + " abc", "1110000", L);
    }
    private void expectBidi(int dir, String text, 
            String expectedLevels, int expectedDir) {
        char[] chs = text.toCharArray();
        int n = chs.length;
        byte[] chInfo = new byte[n];
        int resultDir = StaticLayout.bidi(dir, chs, chInfo, n, false);
        {
            StringBuilder sb = new StringBuilder("info:");
            for (int i = 0; i < n; ++i) {
                sb.append(" ").append(String.valueOf(chInfo[i]));
            }
            Log.i("BIDI", sb.toString());
        }
        char[] resultLevelChars = new char[n];
        for (int i = 0; i < n; ++i) {
            resultLevelChars[i] = (char)('0' + chInfo[i]);
        }
        String resultLevels = new String(resultLevelChars);
        assertEquals("direction", expectedDir, resultDir);
        assertEquals("levels", expectedLevels, resultLevels);
    }
    public void testNativeBidi() {
        expectNativeBidi(REQ_DL,  ALEF + BET + GIMEL + " abc", "1111222", R);
    }
    private void expectNativeBidi(int dir, String text, 
            String expectedLevels, int expectedDir) {
        char[] chs = text.toCharArray();
        int n = chs.length;
        byte[] chInfo = new byte[n];
        int resultDir = AndroidBidi.bidi(dir, chs, chInfo, n, false);
        {
            StringBuilder sb = new StringBuilder("info:");
            for (int i = 0; i < n; ++i) {
                sb.append(" ").append(String.valueOf(chInfo[i]));
            }
            Log.i("BIDI", sb.toString());
        }
        char[] resultLevelChars = new char[n];
        for (int i = 0; i < n; ++i) {
            resultLevelChars[i] = (char)('0' + chInfo[i]);
        }
        String resultLevels = new String(resultLevelChars);
        assertEquals("direction", expectedDir, resultDir);
        assertEquals("levels", expectedLevels, resultLevels);
    }
}

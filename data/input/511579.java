public class TextMethodUtils {
    public static void assertEquals(char[] expected, char[] actual) {
        if (expected != actual) {
            if (expected == null || actual == null) {
                Assert.fail("the char arrays are not equal");
            }
            Assert.assertEquals(String.valueOf(expected), String.valueOf(actual));
        }
    }
    public static int getUnacceptedKeyCode(char[] acceptedChars) {
        for (int keyCode = KeyEvent.KEYCODE_A; keyCode <= KeyEvent.KEYCODE_Z; keyCode++) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            if ('\0' == event.getMatch(acceptedChars)) {
                return keyCode;
            }
        }
        return -1;
    }
}

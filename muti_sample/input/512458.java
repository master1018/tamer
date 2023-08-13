public class EnglishInputProcessor {
    private int mLastKeyCode = KeyEvent.KEYCODE_UNKNOWN;
    public boolean processKey(InputConnection inputContext, KeyEvent event,
            boolean upperCase, boolean realAction) {
        if (null == inputContext || null == event) return false;
        int keyCode = event.getKeyCode();
        CharSequence prefix = null;
        prefix = inputContext.getTextBeforeCursor(2, 0);
        int keyChar;
        keyChar = 0;
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            keyChar = keyCode - KeyEvent.KEYCODE_A + 'a';
            if (upperCase) {
                keyChar = keyChar + 'A' - 'a';
            }
        } else if (keyCode >= KeyEvent.KEYCODE_0
                && keyCode <= KeyEvent.KEYCODE_9)
            keyChar = keyCode - KeyEvent.KEYCODE_0 + '0';
        else if (keyCode == KeyEvent.KEYCODE_COMMA)
            keyChar = ',';
        else if (keyCode == KeyEvent.KEYCODE_PERIOD)
            keyChar = '.';
        else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE)
            keyChar = '\'';
        else if (keyCode == KeyEvent.KEYCODE_AT)
            keyChar = '@';
        else if (keyCode == KeyEvent.KEYCODE_SLASH) keyChar = '/';
        if (0 == keyChar) {
            mLastKeyCode = keyCode;
            String insert = null;
            if (KeyEvent.KEYCODE_DEL == keyCode) {
                if (realAction)  {
                    inputContext.deleteSurroundingText(1, 0);
                }
            } else if (KeyEvent.KEYCODE_ENTER == keyCode) {
                insert = "\n";
            } else if (KeyEvent.KEYCODE_SPACE == keyCode) {
                insert = " ";
            } else {
                return false;
            }
            if (null != insert && realAction)
                inputContext.commitText(insert, insert.length());
            return true;
        }
        if (!realAction)
            return true;
        if (KeyEvent.KEYCODE_SHIFT_LEFT == mLastKeyCode
                || KeyEvent.KEYCODE_SHIFT_LEFT == mLastKeyCode) {
            if (keyChar >= 'a' && keyChar <= 'z')
                keyChar = keyChar - 'a' + 'A';
        } else if (KeyEvent.KEYCODE_ALT_LEFT == mLastKeyCode) {
        }
        String result = String.valueOf((char) keyChar);
        inputContext.commitText(result, result.length());
        mLastKeyCode = keyCode;
        return true;
    }
}

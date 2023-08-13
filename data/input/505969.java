public class StkDigitsKeyListener extends NumberKeyListener {
    @Override
    protected char[] getAcceptedChars() {
        return CHARACTERS;
    }
    public int getInputType() {
        return EditorInfo.TYPE_CLASS_PHONE;
    }
    public static StkDigitsKeyListener getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        sInstance = new StkDigitsKeyListener();
        return sInstance;
    }
    public static final char[] CHARACTERS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '#', '+'};
    private static StkDigitsKeyListener sInstance;
}

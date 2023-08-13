public final class KeyInfo {
    public int vKey;
    public int keyLocation;
    public final StringBuffer keyChars;
    public static final int DEFAULT_VKEY = KeyEvent.VK_UNDEFINED;
    public static final int DEFAULT_LOCATION = KeyEvent.KEY_LOCATION_STANDARD;
    public KeyInfo() {
        vKey = DEFAULT_VKEY;
        keyLocation = DEFAULT_LOCATION;
        keyChars = new StringBuffer();
    }
    public void setKeyChars(char ch) {
        keyChars.setLength(0);
        keyChars.append(ch);
    }
    public void setKeyChars(StringBuffer sb) {
        keyChars.setLength(0);
        keyChars.append(sb);
    }
}

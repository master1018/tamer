public class Bug7041232 {
    public static void main(String[] args) {
        String UnicodeChars;
        StringBuffer sb = new StringBuffer();
        for (int i = 0x2000; i < 0x2100; i++) {
            sb.append((char)i);
        }
        UnicodeChars = sb.toString();
        Bidi bidi = new Bidi(UnicodeChars, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        bidi.createLineBidi(0, UnicodeChars.length());
    }
}

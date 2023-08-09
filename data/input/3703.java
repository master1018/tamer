public class WDefaultFontCharset extends AWTCharset
{
    static {
       initIDs();
    }
    private String fontName;
    public WDefaultFontCharset(String name){
        super("WDefaultFontCharset", Charset.forName("windows-1252"));
        fontName = name;
    }
    public CharsetEncoder newEncoder() {
        return new Encoder();
    }
    private class Encoder extends AWTCharset.Encoder {
        public boolean canEncode(char c){
            return canConvert(c);
        }
    }
    public synchronized native boolean canConvert(char ch);
    private static native void initIDs();
}

public class XFontPeer extends PlatformFont {
    private String xfsname;
    static {
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }
    private static native void initIDs();
    public XFontPeer(String name, int style){
        super(name, style);
    }
    protected char getMissingGlyphCharacter() {
        return '\u274F';
    }
}

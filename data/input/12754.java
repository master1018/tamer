public class MFontPeer extends PlatformFont {
    private String xfsname;
    private String converter;
    static {
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }
    private static native void initIDs();
    public MFontPeer(String name, int style){
        super(name, style);
        if (fontConfig != null) {
            xfsname = ((MFontConfiguration) fontConfig).getMotifFontSet(familyName, style);
        }
    }
    protected char getMissingGlyphCharacter() {
        return '\u274F';
    }
}

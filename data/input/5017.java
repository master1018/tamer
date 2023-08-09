public class WFontPeer extends PlatformFont {
    private String textComponentFontName;
    public WFontPeer(String name, int style){
        super(name, style);
        if (fontConfig != null) {
            textComponentFontName = ((WFontConfiguration) fontConfig).getTextComponentFontName(familyName, style);
        }
    }
    protected char getMissingGlyphCharacter() {
        return '\u2751';
    }
    static {
        initIDs();
    }
    private static native void initIDs();
}

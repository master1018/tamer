public class NativeGlyphMapper extends CharToGlyphMapper {
    NativeFont font;
    XMap xmapper;
    int numGlyphs;
    NativeGlyphMapper(NativeFont f) {
        font = f;
        xmapper = XMap.getXMapper(font.encoding);
        numGlyphs = f.getNumGlyphs();
        missingGlyph = 0;
    }
    public int getNumGlyphs() {
        return numGlyphs;
    }
    public int charToGlyph(char unicode) {
        if (unicode >= xmapper.convertedGlyphs.length) {
            return 0;
        } else {
            return xmapper.convertedGlyphs[unicode];
        }
    }
    public int charToGlyph(int unicode) {
        if (unicode >= xmapper.convertedGlyphs.length) {
            return 0;
        } else {
            return xmapper.convertedGlyphs[unicode];
        }
    }
    public void charsToGlyphs(int count, char[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            char code = unicodes[i];
            if (code >= xmapper.convertedGlyphs.length) {
                glyphs[i] = 0;
            } else {
                glyphs[i] = xmapper.convertedGlyphs[code];
            }
        }
    }
    public boolean charsToGlyphsNS(int count, char[] unicodes, int[] glyphs) {
        charsToGlyphs(count, unicodes, glyphs);
        return false;
    }
    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            char code = (char)unicodes[i];
            if (code >= xmapper.convertedGlyphs.length) {
                glyphs[i] = 0;
            } else {
                glyphs[i] = xmapper.convertedGlyphs[code];
            }
        }
    }
}

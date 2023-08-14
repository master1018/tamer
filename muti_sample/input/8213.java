public final class Type1GlyphMapper extends CharToGlyphMapper {
    Type1Font font;
    FontScaler scaler;
    public Type1GlyphMapper(Type1Font font) {
        this.font = font;
        initMapper();
    }
    private void initMapper() {
        scaler = font.getScaler();
        try {
          missingGlyph = scaler.getMissingGlyphCode();
        } catch (FontScalerException fe) {
            scaler = FontScaler.getNullScaler();
            try {
                missingGlyph = scaler.getMissingGlyphCode();
            } catch (FontScalerException e) { 
                missingGlyph = 0;
            }
        }
    }
    public int getNumGlyphs() {
        try {
            return scaler.getNumGlyphs();
        } catch (FontScalerException e) {
            scaler = FontScaler.getNullScaler();
            return getNumGlyphs();
        }
    }
    public int getMissingGlyphCode() {
        return missingGlyph;
    }
    public boolean canDisplay(char ch) {
        try {
            return scaler.getGlyphCode(ch) != missingGlyph;
        } catch(FontScalerException e) {
            scaler = FontScaler.getNullScaler();
            return canDisplay(ch);
        }
    }
    public int charToGlyph(char ch) {
        try {
            return scaler.getGlyphCode(ch);
        } catch (FontScalerException e) {
            scaler = FontScaler.getNullScaler();
            return charToGlyph(ch);
        }
    }
    public int charToGlyph(int ch) {
        if (ch < 0 || ch > 0xffff) {
            return missingGlyph;
        } else {
            try {
                return scaler.getGlyphCode((char)ch);
            } catch (FontScalerException e) {
                scaler = FontScaler.getNullScaler();
                return charToGlyph(ch);
            }
        }
    }
    public void charsToGlyphs(int count, char[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            int code = unicodes[i]; 
            if (code >= HI_SURROGATE_START &&
                code <= HI_SURROGATE_END && i < count - 1) {
                char low = unicodes[i + 1];
                if (low >= LO_SURROGATE_START &&
                    low <= LO_SURROGATE_END) {
                    code = (code - HI_SURROGATE_START) *
                        0x400 + low - LO_SURROGATE_START + 0x10000;
                    glyphs[i + 1] = 0xFFFF; 
                }
            }
            glyphs[i] = charToGlyph(code);
            if (code >= 0x10000) {
                i += 1; 
            }
        }
    }
    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            glyphs[i] = charToGlyph(unicodes[i]);
        }
    }
    public boolean charsToGlyphsNS(int count, char[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            int code = unicodes[i]; 
            if (code >= HI_SURROGATE_START &&
                code <= HI_SURROGATE_END && i < count - 1) {
                char low = unicodes[i + 1];
                if (low >= LO_SURROGATE_START &&
                    low <= LO_SURROGATE_END) {
                    code = (code - HI_SURROGATE_START) *
                        0x400 + low - LO_SURROGATE_START + 0x10000;
                    glyphs[i + 1] = INVISIBLE_GLYPH_ID;
                }
            }
            glyphs[i] = charToGlyph(code);
            if (code < FontUtilities.MIN_LAYOUT_CHARCODE) {
                continue;
            }
            else if (FontUtilities.isComplexCharCode(code)) {
                return true;
            }
            else if (code >= 0x10000) {
                i += 1; 
                continue;
            }
        }
        return false;
    }
}

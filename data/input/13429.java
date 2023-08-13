public final class CompositeGlyphMapper extends CharToGlyphMapper {
    public static final int SLOTMASK =  0xff000000;
    public static final int GLYPHMASK = 0x00ffffff;
    public static final int NBLOCKS = 216;
    public static final int BLOCKSZ = 256;
    public static final int MAXUNICODE = NBLOCKS*BLOCKSZ;
    CompositeFont font;
    CharToGlyphMapper slotMappers[];
    int[][] glyphMaps;
    private boolean hasExcludes;
    public CompositeGlyphMapper(CompositeFont compFont) {
        font = compFont;
        initMapper();
        hasExcludes = compFont.exclusionRanges != null &&
                      compFont.maxIndices != null;
    }
    public final int compositeGlyphCode(int slot, int glyphCode) {
        return (slot << 24 | (glyphCode & GLYPHMASK));
    }
    private final void initMapper() {
        if (missingGlyph == CharToGlyphMapper.UNINITIALIZED_GLYPH) {
            if (glyphMaps == null) {
                glyphMaps = new int[NBLOCKS][];
            }
            slotMappers = new CharToGlyphMapper[font.numSlots];
            missingGlyph = font.getSlotFont(0).getMissingGlyphCode();
            missingGlyph = compositeGlyphCode(0, missingGlyph);
        }
    }
    private int getCachedGlyphCode(int unicode) {
        if (unicode >= MAXUNICODE) {
            return UNINITIALIZED_GLYPH; 
        }
        int[] gmap;
        if ((gmap = glyphMaps[unicode >> 8]) == null) {
            return UNINITIALIZED_GLYPH;
        }
        return gmap[unicode & 0xff];
    }
    private void setCachedGlyphCode(int unicode, int glyphCode) {
        if (unicode >= MAXUNICODE) {
            return;     
        }
        int index0 = unicode >> 8;
        if (glyphMaps[index0] == null) {
            glyphMaps[index0] = new int[BLOCKSZ];
            for (int i=0;i<BLOCKSZ;i++) {
                glyphMaps[index0][i] = UNINITIALIZED_GLYPH;
            }
        }
        glyphMaps[index0][unicode & 0xff] = glyphCode;
    }
    private final CharToGlyphMapper getSlotMapper(int slot) {
        CharToGlyphMapper mapper = slotMappers[slot];
        if (mapper == null) {
            mapper = font.getSlotFont(slot).getMapper();
            slotMappers[slot] = mapper;
        }
        return mapper;
    }
    private final int convertToGlyph(int unicode) {
        for (int slot = 0; slot < font.numSlots; slot++) {
            if (!hasExcludes || !font.isExcludedChar(slot, unicode)) {
                CharToGlyphMapper mapper = getSlotMapper(slot);
                int glyphCode = mapper.charToGlyph(unicode);
                if (glyphCode != mapper.getMissingGlyphCode()) {
                    glyphCode = compositeGlyphCode(slot, glyphCode);
                    setCachedGlyphCode(unicode, glyphCode);
                    return glyphCode;
                }
            }
        }
        return missingGlyph;
    }
    public int getNumGlyphs() {
        int numGlyphs = 0;
        for (int slot=0; slot<1 ; slot++) {
           CharToGlyphMapper mapper = slotMappers[slot];
           if (mapper == null) {
               mapper = font.getSlotFont(slot).getMapper();
               slotMappers[slot] = mapper;
           }
           numGlyphs += mapper.getNumGlyphs();
        }
        return numGlyphs;
    }
    public int charToGlyph(int unicode) {
        int glyphCode = getCachedGlyphCode(unicode);
        if (glyphCode == UNINITIALIZED_GLYPH) {
            glyphCode = convertToGlyph(unicode);
        }
        return glyphCode;
    }
    public int charToGlyph(int unicode, int prefSlot) {
        if (prefSlot >= 0) {
            CharToGlyphMapper mapper = getSlotMapper(prefSlot);
            int glyphCode = mapper.charToGlyph(unicode);
            if (glyphCode != mapper.getMissingGlyphCode()) {
                return compositeGlyphCode(prefSlot, glyphCode);
            }
        }
        return charToGlyph(unicode);
    }
    public int charToGlyph(char unicode) {
        int glyphCode  = getCachedGlyphCode(unicode);
        if (glyphCode == UNINITIALIZED_GLYPH) {
            glyphCode = convertToGlyph(unicode);
        }
        return glyphCode;
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
            int gc = glyphs[i] = getCachedGlyphCode(code);
            if (gc == UNINITIALIZED_GLYPH) {
                glyphs[i] = convertToGlyph(code);
            }
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
                    int gc = glyphs[i] = getCachedGlyphCode(code);
                    if (gc == UNINITIALIZED_GLYPH) {
                        glyphs[i] = convertToGlyph(code);
                    }
                    i += 1; 
                    glyphs[i] = INVISIBLE_GLYPH_ID;
                    continue;
                }
            }
            int gc = glyphs[i] = getCachedGlyphCode(code);
            if (gc == UNINITIALIZED_GLYPH) {
                glyphs[i] = convertToGlyph(code);
            }
        }
    }
    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
        for (int i=0; i<count; i++) {
            int code = unicodes[i];
            glyphs[i] = getCachedGlyphCode(code);
            if (glyphs[i] == UNINITIALIZED_GLYPH) {
                glyphs[i] = convertToGlyph(code);
            }
        }
    }
}

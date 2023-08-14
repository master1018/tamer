public final class CompositeStrike extends FontStrike {
    static final int SLOTMASK = 0xffffff;
    private CompositeFont compFont;
    private PhysicalStrike[] strikes;
    int numGlyphs = 0;
    CompositeStrike(CompositeFont font2D, FontStrikeDesc desc) {
        this.compFont = font2D;
        this.desc = desc;
        this.disposer = new FontStrikeDisposer(compFont, desc);
        if (desc.style != compFont.style) {
            algoStyle = true;
            if ((desc.style & Font.BOLD) == Font.BOLD &&
                ((compFont.style & Font.BOLD) == 0)) {
                boldness = 1.33f;
            }
            if ((desc.style & Font.ITALIC) == Font.ITALIC &&
                (compFont.style & Font.ITALIC) == 0) {
                italic = 0.7f;
            }
        }
        strikes = new PhysicalStrike[compFont.numSlots];
    }
    PhysicalStrike getStrikeForGlyph(int glyphCode) {
        return getStrikeForSlot(glyphCode >>> 24);
    }
    PhysicalStrike getStrikeForSlot(int slot) {
        PhysicalStrike strike = strikes[slot];
        if (strike == null) {
            strike =
                (PhysicalStrike)(compFont.getSlotFont(slot).getStrike(desc));
            strikes[slot] = strike;
        }
        return strike;
    }
    public int getNumGlyphs() {
        return compFont.getNumGlyphs();
    }
    StrikeMetrics getFontMetrics() {
        if (strikeMetrics == null) {
            StrikeMetrics compMetrics = new StrikeMetrics();
            for (int s=0; s<compFont.numMetricsSlots; s++) {
                compMetrics.merge(getStrikeForSlot(s).getFontMetrics());
            }
            strikeMetrics = compMetrics;
        }
        return strikeMetrics;
    }
    void getGlyphImagePtrs(int[] glyphCodes, long[] images, int  len) {
        PhysicalStrike strike = getStrikeForSlot(0);
        int numptrs = strike.getSlot0GlyphImagePtrs(glyphCodes, images, len);
        if (numptrs == len) {
            return;
        }
        for (int i=numptrs; i< len; i++) {
            strike = getStrikeForGlyph(glyphCodes[i]);
            images[i] = strike.getGlyphImagePtr(glyphCodes[i] & SLOTMASK);
        }
    }
    long getGlyphImagePtr(int glyphCode) {
        PhysicalStrike strike = getStrikeForGlyph(glyphCode);
        return strike.getGlyphImagePtr(glyphCode & SLOTMASK);
    }
    void getGlyphImageBounds(int glyphCode, Point2D.Float pt, Rectangle result) {
        PhysicalStrike strike = getStrikeForGlyph(glyphCode);
        strike.getGlyphImageBounds(glyphCode & SLOTMASK, pt, result);
    }
    Point2D.Float getGlyphMetrics(int glyphCode) {
        PhysicalStrike strike = getStrikeForGlyph(glyphCode);
        return strike.getGlyphMetrics(glyphCode & SLOTMASK);
    }
    Point2D.Float getCharMetrics(char ch) {
        return getGlyphMetrics(compFont.getMapper().charToGlyph(ch));
    }
    float getGlyphAdvance(int glyphCode) {
        PhysicalStrike strike = getStrikeForGlyph(glyphCode);
        return strike.getGlyphAdvance(glyphCode & SLOTMASK);
    }
    float getCodePointAdvance(int cp) {
        return getGlyphAdvance(compFont.getMapper().charToGlyph(cp));
    }
    Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {
        PhysicalStrike strike = getStrikeForGlyph(glyphCode);
        return strike.getGlyphOutlineBounds(glyphCode & SLOTMASK);
    }
    GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
        PhysicalStrike strike = getStrikeForGlyph(glyphCode);
        GeneralPath path = strike.getGlyphOutline(glyphCode & SLOTMASK, x, y);
        if (path == null) {
            return new GeneralPath();
        } else {
            return path;
        }
    }
    GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
        GeneralPath path = null;
        GeneralPath gp;
        int glyphIndex = 0;
        int[] tmpGlyphs;
        while (glyphIndex < glyphs.length) {
            int start = glyphIndex;
            int slot = glyphs[glyphIndex] >>> 24;
            while (glyphIndex < glyphs.length &&
                   (glyphs[glyphIndex+1] >>> 24) == slot) {
                glyphIndex++;
            }
            int tmpLen = glyphIndex-start+1;
            tmpGlyphs = new int[tmpLen];
            for (int i=0;i<tmpLen;i++) {
                tmpGlyphs[i] = glyphs[i] & SLOTMASK;
            }
            gp = getStrikeForSlot(slot).getGlyphVectorOutline(tmpGlyphs, x, y);
            if (path == null) {
                path = gp;
            } else if (gp != null) {
                path.append(gp, false);
            }
        }
        if (path == null) {
            return new GeneralPath();
        } else {
            return path;
        }
    }
}

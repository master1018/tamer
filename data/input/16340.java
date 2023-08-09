class NullFontScaler extends FontScaler {
    NullFontScaler() {}
    public NullFontScaler(Font2D font, int indexInCollection,
        boolean supportsCJK, int filesize) {}
    StrikeMetrics getFontMetrics(long pScalerContext) {
        return new StrikeMetrics(0xf0,0xf0,0xf0,0xf0,0xf0,0xf0,
        0xf0,0xf0,0xf0,0xf0);
    }
    float getGlyphAdvance(long pScalerContext, int glyphCode) {
        return 0.0f;
    }
    void getGlyphMetrics(long pScalerContext, int glyphCode,
        Point2D.Float metrics) {
        metrics.x = 0;
        metrics.y = 0;
    }
    Rectangle2D.Float getGlyphOutlineBounds(long pContext, int glyphCode) {
        return new Rectangle2D.Float(0, 0, 0, 0);
    }
    GeneralPath getGlyphOutline(long pScalerContext, int glyphCode,
        float x, float y) {
        return new GeneralPath();
    }
    GeneralPath getGlyphVectorOutline(long pScalerContext, int[] glyphs,
        int numGlyphs, float x, float y) {
        return new GeneralPath();
    }
    long getLayoutTableCache() {return 0L;}
    long createScalerContext(double[] matrix, int aa,
        int fm, float boldness, float italic, boolean disableHinting) {
        return getNullScalerContext();
    }
    void invalidateScalerContext(long ppScalerContext) {
    }
    int getNumGlyphs() throws FontScalerException {
        return 1;
    }
    int getMissingGlyphCode() throws FontScalerException {
        return 0;
    }
    int getGlyphCode(char charCode) throws FontScalerException {
        return 0;
    }
    long getUnitsPerEm() {
        return 2048;
    }
    Point2D.Float getGlyphPoint(long pScalerContext,
                                int glyphCode, int ptNumber) {
        return null;
    }
    static native long getNullScalerContext();
    native long getGlyphImage(long pScalerContext, int glyphCode);
}

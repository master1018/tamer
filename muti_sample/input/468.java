public abstract class FontStrike {
    protected FontStrikeDisposer disposer;
    protected FontStrikeDesc desc;
    protected StrikeMetrics strikeMetrics;
    protected boolean algoStyle = false;
    protected float boldness = 1f;
    protected float italic = 0f;
    public abstract int getNumGlyphs();
    abstract StrikeMetrics getFontMetrics();
    abstract void getGlyphImagePtrs(int[] glyphCodes, long[] images,int  len);
    abstract long getGlyphImagePtr(int glyphcode);
    abstract void getGlyphImageBounds(int glyphcode,
                                      Point2D.Float pt,
                                      Rectangle result);
    abstract Point2D.Float getGlyphMetrics(int glyphcode);
    abstract Point2D.Float getCharMetrics(char ch);
    abstract float getGlyphAdvance(int glyphCode);
    abstract float getCodePointAdvance(int cp);
    abstract Rectangle2D.Float getGlyphOutlineBounds(int glyphCode);
    abstract GeneralPath
        getGlyphOutline(int glyphCode, float x, float y);
    abstract GeneralPath
        getGlyphVectorOutline(int[] glyphs, float x, float y);
}

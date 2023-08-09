public abstract class PhysicalFont extends Font2D {
    protected String platName;
    protected Object nativeNames;
    public boolean equals(Object o) {
        return (o != null && o.getClass() == this.getClass() &&
                ((Font2D)o).fullName.equals(this.fullName));
    }
    public int hashCode() {
        return fullName.hashCode();
    }
    PhysicalFont(String platname, Object nativeNames)
        throws FontFormatException {
        handle = new Font2DHandle(this);
        this.platName = platname;
        this.nativeNames = nativeNames;
    }
    protected PhysicalFont() {
        handle = new Font2DHandle(this);
    }
    Point2D.Float getGlyphPoint(long pScalerContext,
                             int glyphCode, int ptNumber) {
        return new Point2D.Float();
    }
    abstract StrikeMetrics getFontMetrics(long pScalerContext);
    abstract float getGlyphAdvance(long pScalerContext, int glyphCode);
    abstract void getGlyphMetrics(long pScalerContext, int glyphCode,
                                  Point2D.Float metrics);
    abstract long getGlyphImage(long pScalerContext, int glyphCode);
    abstract Rectangle2D.Float getGlyphOutlineBounds(long pScalerContext,
                                                     int glyphCode);
    abstract GeneralPath getGlyphOutline(long pScalerContext, int glyphCode,
                                         float x, float y);
    abstract GeneralPath getGlyphVectorOutline(long pScalerContext,
                                               int[] glyphs, int numGlyphs,
                                               float x, float y);
}

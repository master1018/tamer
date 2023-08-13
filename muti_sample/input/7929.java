public abstract class PhysicalStrike extends FontStrike {
    static final long INTMASK = 0xffffffffL;
    static boolean longAddresses;
    static {
        switch (StrikeCache.nativeAddressSize) {
        case 8: longAddresses = true; break;
        case 4: longAddresses = false; break;
        default: throw new RuntimeException("Unexpected address size");
        }
    }
    private PhysicalFont physicalFont;
    protected CharToGlyphMapper mapper;
    protected long pScalerContext;
    protected long[] longGlyphImages;
    protected int[] intGlyphImages;
    ConcurrentHashMap<Integer, Point2D.Float> glyphPointMapCache;
    protected boolean getImageWithAdvance;
    protected static final int complexTX =
        AffineTransform.TYPE_FLIP |
        AffineTransform.TYPE_GENERAL_SCALE |
        AffineTransform.TYPE_GENERAL_ROTATION |
        AffineTransform.TYPE_GENERAL_TRANSFORM |
        AffineTransform.TYPE_QUADRANT_ROTATION;
    PhysicalStrike(PhysicalFont physicalFont, FontStrikeDesc desc) {
        this.physicalFont = physicalFont;
        this.desc = desc;
    }
    protected PhysicalStrike() {
    }
    public int getNumGlyphs() {
        return physicalFont.getNumGlyphs();
    }
    StrikeMetrics getFontMetrics() {
        if (strikeMetrics == null) {
            strikeMetrics =
                physicalFont.getFontMetrics(pScalerContext);
        }
        return strikeMetrics;
    }
    float getCodePointAdvance(int cp) {
        return getGlyphAdvance(physicalFont.getMapper().charToGlyph(cp));
    }
   Point2D.Float getCharMetrics(char ch) {
        return getGlyphMetrics(physicalFont.getMapper().charToGlyph(ch));
    }
    int getSlot0GlyphImagePtrs(int[] glyphCodes, long[] images, int  len) {
        return 0;
    }
    Point2D.Float getGlyphPoint(int glyphCode, int ptNumber) {
        Point2D.Float gp = null;
        Integer ptKey = Integer.valueOf(glyphCode<<16|ptNumber);
        if (glyphPointMapCache == null) {
            synchronized (this) {
                if (glyphPointMapCache == null) {
                    glyphPointMapCache =
                        new ConcurrentHashMap<Integer, Point2D.Float>();
                }
            }
        } else {
            gp = glyphPointMapCache.get(ptKey);
        }
        if (gp == null) {
            gp = (physicalFont.getGlyphPoint(pScalerContext, glyphCode, ptNumber));
            adjustPoint(gp);
            glyphPointMapCache.put(ptKey, gp);
        }
        return gp;
    }
    protected void adjustPoint(Point2D.Float pt) {
    }
}

public class StandardGlyphVector extends GlyphVector {
    private Font font;
    private FontRenderContext frc;
    private int[] glyphs; 
    private int[] userGlyphs; 
    private float[] positions; 
    private int[] charIndices;  
    private int flags; 
    private static final int UNINITIALIZED_FLAGS = -1;
    private GlyphTransformInfo gti; 
    private AffineTransform ftx;   
    private AffineTransform dtx;   
    private AffineTransform invdtx; 
    private AffineTransform frctx; 
    private Font2D font2D;         
    private SoftReference fsref;   
    public StandardGlyphVector(Font font, String str, FontRenderContext frc) {
        init(font, str.toCharArray(), 0, str.length(), frc, UNINITIALIZED_FLAGS);
    }
    public StandardGlyphVector(Font font, char[] text, FontRenderContext frc) {
        init(font, text, 0, text.length, frc, UNINITIALIZED_FLAGS);
    }
    public StandardGlyphVector(Font font, char[] text, int start, int count,
                               FontRenderContext frc) {
        init(font, text, start, count, frc, UNINITIALIZED_FLAGS);
    }
    private float getTracking(Font font) {
        if (font.hasLayoutAttributes()) {
            AttributeValues values = ((AttributeMap)font.getAttributes()).getValues();
            return values.getTracking();
        }
        return 0;
    }
    public StandardGlyphVector(Font font, FontRenderContext frc, int[] glyphs, float[] positions,
                               int[] indices, int flags) {
        initGlyphVector(font, frc, glyphs, positions, indices, flags);
        float track = getTracking(font);
        if (track != 0) {
            track *= font.getSize2D();
            Point2D.Float trackPt = new Point2D.Float(track, 0); 
            if (font.isTransformed()) {
                AffineTransform at = font.getTransform();
                at.deltaTransform(trackPt, trackPt);
            }
            Font2D f2d = FontUtilities.getFont2D(font);
            FontStrike strike = f2d.getStrike(font, frc);
            float[] deltas = { trackPt.x, trackPt.y };
            for (int j = 0; j < deltas.length; ++j) {
                float inc = deltas[j];
                if (inc != 0) {
                    float delta = 0;
                    for (int i = j, n = 0; n < glyphs.length; i += 2) {
                        if (strike.getGlyphAdvance(glyphs[n++]) != 0) { 
                            positions[i] += delta;
                            delta += inc;
                        }
                    }
                    positions[positions.length-2+j] += delta;
                }
            }
        }
    }
    public void initGlyphVector(Font font, FontRenderContext frc, int[] glyphs, float[] positions,
                                int[] indices, int flags) {
        this.font = font;
        this.frc = frc;
        this.glyphs = glyphs;
        this.userGlyphs = glyphs; 
        this.positions = positions;
        this.charIndices = indices;
        this.flags = flags;
        initFontData();
    }
    public StandardGlyphVector(Font font, CharacterIterator iter, FontRenderContext frc) {
        int offset = iter.getBeginIndex();
        char[] text = new char [iter.getEndIndex() - offset];
        for(char c = iter.first();
            c != CharacterIterator.DONE;
            c = iter.next()) {
            text[iter.getIndex() - offset] = c;
        }
        init(font, text, 0, text.length, frc, UNINITIALIZED_FLAGS);
    }
    public StandardGlyphVector(Font font, int[] glyphs, FontRenderContext frc) {
        this.font = font;
        this.frc = frc;
        this.flags = UNINITIALIZED_FLAGS;
        initFontData();
        this.userGlyphs = glyphs;
        this.glyphs = getValidatedGlyphs(this.userGlyphs);
    }
    public static StandardGlyphVector getStandardGV(GlyphVector gv,
                                                    FontInfo info) {
        if (info.aaHint == SunHints.INTVAL_TEXT_ANTIALIAS_ON) {
            Object aaHint = gv.getFontRenderContext().getAntiAliasingHint();
            if (aaHint != VALUE_TEXT_ANTIALIAS_ON &&
                aaHint != VALUE_TEXT_ANTIALIAS_GASP) {
                FontRenderContext frc = gv.getFontRenderContext();
                frc = new FontRenderContext(frc.getTransform(),
                                            VALUE_TEXT_ANTIALIAS_ON,
                                            frc.getFractionalMetricsHint());
                return new StandardGlyphVector(gv, frc);
            }
        }
        if (gv instanceof StandardGlyphVector) {
            return (StandardGlyphVector)gv;
        }
        return new StandardGlyphVector(gv, gv.getFontRenderContext());
    }
    public Font getFont() {
        return this.font;
    }
    public FontRenderContext getFontRenderContext() {
        return this.frc;
    }
    public void performDefaultLayout() {
        positions = null;
        if (getTracking(font) == 0) {
            clearFlags(FLAG_HAS_POSITION_ADJUSTMENTS);
        }
    }
    public int getNumGlyphs() {
        return glyphs.length;
    }
    public int getGlyphCode(int glyphIndex) {
        return userGlyphs[glyphIndex];
    }
    public int[] getGlyphCodes(int start, int count, int[] result) {
        if (count < 0) {
            throw new IllegalArgumentException("count = " + count);
        }
        if (start < 0) {
            throw new IndexOutOfBoundsException("start = " + start);
        }
        if (start > glyphs.length - count) { 
            throw new IndexOutOfBoundsException("start + count = " + (start + count));
        }
        if (result == null) {
            result = new int[count];
        }
        for (int i = 0; i < count; ++i) {
            result[i] = userGlyphs[i + start];
        }
        return result;
    }
    public int getGlyphCharIndex(int ix) {
        if (ix < 0 && ix >= glyphs.length) {
            throw new IndexOutOfBoundsException("" + ix);
        }
        if (charIndices == null) {
            if ((getLayoutFlags() & FLAG_RUN_RTL) != 0) {
                return glyphs.length - 1 - ix;
            }
            return ix;
        }
        return charIndices[ix];
    }
    public int[] getGlyphCharIndices(int start, int count, int[] result) {
        if (start < 0 || count < 0 || (count > glyphs.length - start)) {
            throw new IndexOutOfBoundsException("" + start + ", " + count);
        }
        if (result == null) {
            result = new int[count];
        }
        if (charIndices == null) {
            if ((getLayoutFlags() & FLAG_RUN_RTL) != 0) {
                for (int i = 0, n = glyphs.length - 1 - start;
                     i < count; ++i, --n) {
                         result[i] = n;
                     }
            } else {
                for (int i = 0, n = start; i < count; ++i, ++n) {
                    result[i] = n;
                }
            }
        } else {
            for (int i = 0; i < count; ++i) {
                result[i] = charIndices[i + start];
            }
        }
        return result;
    }
    public Rectangle2D getLogicalBounds() {
        setFRCTX();
        initPositions();
        LineMetrics lm = font.getLineMetrics("", frc);
        float minX, minY, maxX, maxY;
        minX = 0;
        minY = -lm.getAscent();
        maxX = 0;
        maxY = lm.getDescent() + lm.getLeading();
        if (glyphs.length > 0) {
            maxX = positions[positions.length - 2];
        }
        return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
    }
    public Rectangle2D getVisualBounds() {
        Rectangle2D result = null;
        for (int i = 0; i < glyphs.length; ++i) {
            Rectangle2D glyphVB = getGlyphVisualBounds(i).getBounds2D();
            if (!glyphVB.isEmpty()) {
                if (result == null) {
                    result = glyphVB;
                } else {
                    Rectangle2D.union(result, glyphVB, result);
                }
            }
        }
        if (result == null) {
            result = new Rectangle2D.Float(0, 0, 0, 0);
        }
        return result;
    }
    public Rectangle getPixelBounds(FontRenderContext renderFRC, float x, float y) {
      return getGlyphsPixelBounds(renderFRC, x, y, 0, glyphs.length);
    }
    public Shape getOutline() {
        return getGlyphsOutline(0, glyphs.length, 0, 0);
    }
    public Shape getOutline(float x, float y) {
        return getGlyphsOutline(0, glyphs.length, x, y);
    }
    public Shape getGlyphOutline(int ix) {
        return getGlyphsOutline(ix, 1, 0, 0);
    }
    public Shape getGlyphOutline(int ix, float x, float y) {
        return getGlyphsOutline(ix, 1, x, y);
    }
    public Point2D getGlyphPosition(int ix) {
        initPositions();
        ix *= 2;
        return new Point2D.Float(positions[ix], positions[ix + 1]);
    }
    public void setGlyphPosition(int ix, Point2D pos) {
        initPositions();
        int ix2 = ix << 1;
        positions[ix2] = (float)pos.getX();
        positions[ix2 + 1] = (float)pos.getY();
        clearCaches(ix);
        addFlags(FLAG_HAS_POSITION_ADJUSTMENTS);
    }
    public AffineTransform getGlyphTransform(int ix) {
        if (ix < 0 || ix >= glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        if (gti != null) {
            return gti.getGlyphTransform(ix);
        }
        return null; 
    }
    public void setGlyphTransform(int ix, AffineTransform newTX) {
        if (ix < 0 || ix >= glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        if (gti == null) {
            if (newTX == null || newTX.isIdentity()) {
                return;
            }
            gti = new GlyphTransformInfo(this);
        }
        gti.setGlyphTransform(ix, newTX); 
        if (gti.transformCount() == 0) {
            gti = null;
        }
    }
    public int getLayoutFlags() {
        if (flags == UNINITIALIZED_FLAGS) {
            flags = 0;
            if (charIndices != null && glyphs.length > 1) {
                boolean ltr = true;
                boolean rtl = true;
                int rtlix = charIndices.length; 
                for (int i = 0; i < charIndices.length && (ltr || rtl); ++i) {
                    int cx = charIndices[i];
                    ltr = ltr && (cx == i);
                    rtl = rtl && (cx == --rtlix);
                }
                if (rtl) flags |= FLAG_RUN_RTL;
                if (!rtl && !ltr) flags |= FLAG_COMPLEX_GLYPHS;
            }
        }
        return flags;
    }
    public float[] getGlyphPositions(int start, int count, float[] result) {
        if (count < 0) {
            throw new IllegalArgumentException("count = " + count);
        }
        if (start < 0) {
            throw new IndexOutOfBoundsException("start = " + start);
        }
        if (start > glyphs.length + 1 - count) { 
            throw new IndexOutOfBoundsException("start + count = " + (start + count));
        }
        return internalGetGlyphPositions(start, count, 0, result);
    }
    public Shape getGlyphLogicalBounds(int ix) {
        if (ix < 0 || ix >= glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        Shape[] lbcache;
        if (lbcacheRef == null || (lbcache = (Shape[])lbcacheRef.get()) == null) {
            lbcache = new Shape[glyphs.length];
            lbcacheRef = new SoftReference(lbcache);
        }
        Shape result = lbcache[ix];
        if (result == null) {
            setFRCTX();
            initPositions();
            ADL adl = new ADL();
            GlyphStrike gs = getGlyphStrike(ix);
            gs.getADL(adl);
            Point2D.Float adv = gs.strike.getGlyphMetrics(glyphs[ix]);
            float wx = adv.x;
            float wy = adv.y;
            float hx = adl.descentX + adl.leadingX + adl.ascentX;
            float hy = adl.descentY + adl.leadingY + adl.ascentY;
            float x = positions[ix*2] + gs.dx - adl.ascentX;
            float y = positions[ix*2+1] + gs.dy - adl.ascentY;
            GeneralPath gp = new GeneralPath();
            gp.moveTo(x, y);
            gp.lineTo(x + wx, y + wy);
            gp.lineTo(x + wx + hx, y + wy + hy);
            gp.lineTo(x + hx, y + hy);
            gp.closePath();
            result = new DelegatingShape(gp);
            lbcache[ix] = result;
        }
        return result;
    }
    private SoftReference lbcacheRef;
    public Shape getGlyphVisualBounds(int ix) {
        if (ix < 0 || ix >= glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        Shape[] vbcache;
        if (vbcacheRef == null || (vbcache = (Shape[])vbcacheRef.get()) == null) {
            vbcache = new Shape[glyphs.length];
            vbcacheRef = new SoftReference(vbcache);
        }
        Shape result = vbcache[ix];
        if (result == null) {
            result = new DelegatingShape(getGlyphOutlineBounds(ix));
            vbcache[ix] = result;
        }
        return result;
    }
    private SoftReference vbcacheRef;
    public Rectangle getGlyphPixelBounds(int index, FontRenderContext renderFRC, float x, float y) {
      return getGlyphsPixelBounds(renderFRC, x, y, index, 1);
    }
    public GlyphMetrics getGlyphMetrics(int ix) {
        if (ix < 0 || ix >= glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        Rectangle2D vb = getGlyphVisualBounds(ix).getBounds2D();
        Point2D pt = getGlyphPosition(ix);
        vb.setRect(vb.getMinX() - pt.getX(),
                   vb.getMinY() - pt.getY(),
                   vb.getWidth(),
                   vb.getHeight());
        Point2D.Float adv =
            getGlyphStrike(ix).strike.getGlyphMetrics(glyphs[ix]);
        GlyphMetrics gm = new GlyphMetrics(true, adv.x, adv.y,
                                           vb,
                                           GlyphMetrics.STANDARD);
        return gm;
    }
    public GlyphJustificationInfo getGlyphJustificationInfo(int ix) {
        if (ix < 0 || ix >= glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        return null;
    }
    public boolean equals(GlyphVector rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null) {
            return false;
        }
        try {
            StandardGlyphVector other = (StandardGlyphVector)rhs;
            if (glyphs.length != other.glyphs.length) {
                return false;
            }
            for (int i = 0; i < glyphs.length; ++i) {
                if (glyphs[i] != other.glyphs[i]) {
                    return false;
                }
            }
            if (!font.equals(other.font)) {
                return false;
            }
            if (!frc.equals(other.frc)) {
                return false;
            }
            if ((other.positions == null) != (positions == null)) {
                if (positions == null) {
                    initPositions();
                } else {
                    other.initPositions();
                }
            }
            if (positions != null) {
                for (int i = 0; i < positions.length; ++i) {
                    if (positions[i] != other.positions[i]) {
                        return false;
                    }
                }
            }
            if (gti == null) {
                return other.gti == null;
            } else {
                return gti.equals(other.gti);
            }
        }
        catch (ClassCastException e) {
            return false;
        }
    }
    public int hashCode() {
        return font.hashCode() ^ glyphs.length;
    }
    public boolean equals(Object rhs) {
        try {
            return equals((GlyphVector)rhs);
        }
        catch (ClassCastException e) {
            return false;
        }
    }
    public StandardGlyphVector copy() {
        return (StandardGlyphVector)clone();
    }
    public Object clone() {
        try {
            StandardGlyphVector result = (StandardGlyphVector)super.clone();
            result.clearCaches();
            if (positions != null) {
                result.positions = (float[])positions.clone();
            }
            if (gti != null) {
                result.gti = new GlyphTransformInfo(result, gti);
            }
            return result;
        }
        catch (CloneNotSupportedException e) {
        }
        return this;
    }
    public void setGlyphPositions(float[] srcPositions, int srcStart,
                                  int start, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count = " + count);
        }
        initPositions();
        for (int i = start * 2, e = i + count * 2, p = srcStart; i < e; ++i, ++p) {
            positions[i] = srcPositions[p];
        }
        clearCaches();
        addFlags(FLAG_HAS_POSITION_ADJUSTMENTS);
    }
    public void setGlyphPositions(float[] srcPositions) {
        int requiredLength = glyphs.length * 2 + 2;
        if (srcPositions.length != requiredLength) {
            throw new IllegalArgumentException("srcPositions.length != " + requiredLength);
        }
        positions = (float[])srcPositions.clone();
        clearCaches();
        addFlags(FLAG_HAS_POSITION_ADJUSTMENTS);
    }
    public float[] getGlyphPositions(float[] result) {
        return internalGetGlyphPositions(0, glyphs.length + 1, 0, result);
    }
    public AffineTransform[] getGlyphTransforms(int start, int count, AffineTransform[] result) {
        if (start < 0 || count < 0 || start + count > glyphs.length) {
            throw new IllegalArgumentException("start: " + start + " count: " + count);
        }
        if (gti == null) {
            return null;
        }
        if (result == null) {
            result = new AffineTransform[count];
        }
        for (int i = 0; i < count; ++i, ++start) {
            result[i] = gti.getGlyphTransform(start);
        }
        return result;
    }
    public AffineTransform[] getGlyphTransforms() {
        return getGlyphTransforms(0, glyphs.length, null);
    }
    public void setGlyphTransforms(AffineTransform[] srcTransforms, int srcStart, int start, int count) {
        for (int i = start, e = start + count; i < e; ++i) {
            setGlyphTransform(i, srcTransforms[srcStart + i]);
        }
    }
    public void setGlyphTransforms(AffineTransform[] srcTransforms) {
        setGlyphTransforms(srcTransforms, 0, 0, glyphs.length);
    }
    public float[] getGlyphInfo() {
        setFRCTX();
        initPositions();
        float[] result = new float[glyphs.length * 8];
        for (int i = 0, n = 0; i < glyphs.length; ++i, n += 8) {
            float x = positions[i*2];
            float y = positions[i*2+1];
            result[n] = x;
            result[n+1] = y;
            int glyphID = glyphs[i];
            GlyphStrike s = getGlyphStrike(i);
            Point2D.Float adv = s.strike.getGlyphMetrics(glyphID);
            result[n+2] = adv.x;
            result[n+3] = adv.y;
            Rectangle2D vb = getGlyphVisualBounds(i).getBounds2D();
            result[n+4] = (float)(vb.getMinX());
            result[n+5] = (float)(vb.getMinY());
            result[n+6] = (float)(vb.getWidth());
            result[n+7] = (float)(vb.getHeight());
        }
        return result;
    }
    public void pixellate(FontRenderContext renderFRC, Point2D loc, Point pxResult) {
        if (renderFRC == null) {
            renderFRC = frc;
        }
        AffineTransform at = renderFRC.getTransform();
        at.transform(loc, loc);
        pxResult.x = (int)loc.getX(); 
        pxResult.y = (int)loc.getY();
        loc.setLocation(pxResult.x, pxResult.y);
        try {
            at.inverseTransform(loc, loc);
        }
        catch (NoninvertibleTransformException e) {
            throw new IllegalArgumentException("must be able to invert frc transform");
        }
    }
    boolean needsPositions(double[] devTX) {
        return gti != null ||
            (getLayoutFlags() & FLAG_HAS_POSITION_ADJUSTMENTS) != 0 ||
            !matchTX(devTX, frctx);
    }
    Object setupGlyphImages(long[] images, float[] positions, double[] devTX) {
        initPositions(); 
        setRenderTransform(devTX); 
        if (gti != null) {
            return gti.setupGlyphImages(images, positions, dtx);
        }
        GlyphStrike gs = getDefaultStrike();
        gs.strike.getGlyphImagePtrs(glyphs, images, glyphs.length);
        if (positions != null) {
            if (dtx.isIdentity()) {
                System.arraycopy(this.positions, 0, positions, 0, glyphs.length * 2);
            } else {
                dtx.transform(this.positions, 0, positions, 0, glyphs.length);
            }
        }
        return gs;
    }
    private static boolean matchTX(double[] lhs, AffineTransform rhs) {
        return
            lhs[0] == rhs.getScaleX() &&
            lhs[1] == rhs.getShearY() &&
            lhs[2] == rhs.getShearX() &&
            lhs[3] == rhs.getScaleY();
    }
    private static AffineTransform getNonTranslateTX(AffineTransform tx) {
        if (tx.getTranslateX() != 0 || tx.getTranslateY() != 0) {
            tx = new AffineTransform(tx.getScaleX(), tx.getShearY(),
                                     tx.getShearX(), tx.getScaleY(),
                                     0, 0);
        }
        return tx;
    }
    private static boolean equalNonTranslateTX(AffineTransform lhs, AffineTransform rhs) {
        return lhs.getScaleX() == rhs.getScaleX() &&
            lhs.getShearY() == rhs.getShearY() &&
            lhs.getShearX() == rhs.getShearX() &&
            lhs.getScaleY() == rhs.getScaleY();
    }
    private void setRenderTransform(double[] devTX) {
        assert(devTX.length == 4);
        if (!matchTX(devTX, dtx)) {
            resetDTX(new AffineTransform(devTX)); 
        }
    }
    private final void setDTX(AffineTransform tx) {
        if (!equalNonTranslateTX(dtx, tx)) {
            resetDTX(getNonTranslateTX(tx));
        }
    }
    private final void setFRCTX() {
        if (!equalNonTranslateTX(frctx, dtx)) {
            resetDTX(getNonTranslateTX(frctx));
        }
    }
    private final void resetDTX(AffineTransform at) {
        fsref = null;
        dtx = at;
        invdtx = null;
        if (!dtx.isIdentity()) {
            try {
                invdtx = dtx.createInverse();
            }
            catch (NoninvertibleTransformException e) {
            }
        }
        if (gti != null) {
            gti.strikesRef = null;
        }
    }
    private StandardGlyphVector(GlyphVector gv, FontRenderContext frc) {
        this.font = gv.getFont();
        this.frc = frc;
        initFontData();
        int nGlyphs = gv.getNumGlyphs();
        this.userGlyphs = gv.getGlyphCodes(0, nGlyphs, null);
        if (gv instanceof StandardGlyphVector) {
            this.glyphs = userGlyphs;
        } else {
            this.glyphs = getValidatedGlyphs(this.userGlyphs);
        }
        this.flags = gv.getLayoutFlags() & FLAG_MASK;
        if ((flags & FLAG_HAS_POSITION_ADJUSTMENTS) != 0) {
            this.positions = gv.getGlyphPositions(0, nGlyphs + 1, null);
        }
        if ((flags & FLAG_COMPLEX_GLYPHS) != 0) {
            this.charIndices = gv.getGlyphCharIndices(0, nGlyphs, null);
        }
        if ((flags & FLAG_HAS_TRANSFORMS) != 0) {
            AffineTransform[] txs = new AffineTransform[nGlyphs]; 
            for (int i = 0; i < nGlyphs; ++i) {
                txs[i] = gv.getGlyphTransform(i); 
            }
            setGlyphTransforms(txs);
        }
    }
    int[] getValidatedGlyphs(int[] oglyphs) {
        int len = oglyphs.length;
        int[] vglyphs = new int[len];
        for (int i=0; i<len; i++) {
            if (oglyphs[i] == 0xFFFE || oglyphs[i] == 0xFFFF) {
                vglyphs[i] = oglyphs[i];
            } else {
                vglyphs[i] = font2D.getValidatedGlyphCode(oglyphs[i]);
            }
        }
        return vglyphs;
    }
    private void init(Font font, char[] text, int start, int count,
                      FontRenderContext frc, int flags) {
        if (start < 0 || count < 0 || start + count > text.length) {
            throw new ArrayIndexOutOfBoundsException("start or count out of bounds");
        }
        this.font = font;
        this.frc = frc;
        this.flags = flags;
        if (getTracking(font) != 0) {
            addFlags(FLAG_HAS_POSITION_ADJUSTMENTS);
        }
        if (start != 0) {
            char[] temp = new char[count];
            System.arraycopy(text, start, temp, 0, count);
            text = temp;
        }
        initFontData(); 
        glyphs = new int[count]; 
        userGlyphs = glyphs;
        font2D.getMapper().charsToGlyphs(count, text, glyphs);
    }
    private void initFontData() {
        font2D = FontUtilities.getFont2D(font);
        float s = font.getSize2D();
        if (font.isTransformed()) {
            ftx = font.getTransform();
            if (ftx.getTranslateX() != 0 || ftx.getTranslateY() != 0) {
                addFlags(FLAG_HAS_POSITION_ADJUSTMENTS);
            }
            ftx.setTransform(ftx.getScaleX(), ftx.getShearY(), ftx.getShearX(), ftx.getScaleY(), 0, 0);
            ftx.scale(s, s);
        } else {
            ftx = AffineTransform.getScaleInstance(s, s);
        }
        frctx = frc.getTransform();
        resetDTX(getNonTranslateTX(frctx));
    }
    private float[] internalGetGlyphPositions(int start, int count, int offset, float[] result) {
        if (result == null) {
            result = new float[offset + count * 2];
        }
        initPositions();
        for (int i = offset, e = offset + count * 2, p = start * 2; i < e; ++i, ++p) {
            result[i] = positions[p];
        }
        return result;
    }
    private Rectangle2D getGlyphOutlineBounds(int ix) {
        setFRCTX();
        initPositions();
        return getGlyphStrike(ix).getGlyphOutlineBounds(glyphs[ix], positions[ix*2], positions[ix*2+1]);
    }
    private Shape getGlyphsOutline(int start, int count, float x, float y) {
        setFRCTX();
        initPositions();
        GeneralPath result = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        for (int i = start, e = start + count, n = start * 2; i < e; ++i, n += 2) {
            float px = x + positions[n];
            float py = y + positions[n+1];
            getGlyphStrike(i).appendGlyphOutline(glyphs[i], result, px, py);
        }
        return result;
    }
    private Rectangle getGlyphsPixelBounds(FontRenderContext frc, float x, float y, int start, int count) {
        initPositions(); 
        AffineTransform tx = null;
        if (frc == null || frc.equals(this.frc)) {
            tx = frctx;
        } else {
            tx = frc.getTransform();
        }
        setDTX(tx); 
        if (gti != null) {
            return gti.getGlyphsPixelBounds(tx, x, y, start, count);
        }
        FontStrike fs = getDefaultStrike().strike;
        Rectangle result = null;
        Rectangle r = new Rectangle();
        Point2D.Float pt = new Point.Float();
        int n = start * 2;
        while (--count >= 0) {
            pt.x = x + positions[n++];
            pt.y = y + positions[n++];
            tx.transform(pt, pt);
            fs.getGlyphImageBounds(glyphs[start++], pt, r);
            if (!r.isEmpty()) {
                if (result == null) {
                    result = new Rectangle(r);
                } else {
                    result.add(r);
                }
            }
        }
        return result != null ? result : r;
    }
    private void clearCaches(int ix) {
        if (lbcacheRef != null) {
            Shape[] lbcache = (Shape[])lbcacheRef.get();
            if (lbcache != null) {
                lbcache[ix] = null;
            }
        }
        if (vbcacheRef != null) {
            Shape[] vbcache = (Shape[])vbcacheRef.get();
            if (vbcache != null) {
                vbcache[ix] = null;
            }
        }
    }
    private void clearCaches() {
        lbcacheRef = null;
        vbcacheRef = null;
    }
    public static final int FLAG_USES_VERTICAL_BASELINE = 128;
    public static final int FLAG_USES_VERTICAL_METRICS = 256;
    public static final int FLAG_USES_ALTERNATE_ORIENTATION = 512;
    private void initPositions() {
        if (positions == null) {
            setFRCTX();
            positions = new float[glyphs.length * 2 + 2];
            Point2D.Float trackPt = null;
            float track = getTracking(font);
            if (track != 0) {
                track *= font.getSize2D();
                trackPt = new Point2D.Float(track, 0); 
            }
            Point2D.Float pt = new Point2D.Float(0, 0);
            if (font.isTransformed()) {
                AffineTransform at = font.getTransform();
                at.transform(pt, pt);
                positions[0] = pt.x;
                positions[1] = pt.y;
                if (trackPt != null) {
                    at.deltaTransform(trackPt, trackPt);
                }
            }
            for (int i = 0, n = 2; i < glyphs.length; ++i, n += 2) {
                getGlyphStrike(i).addDefaultGlyphAdvance(glyphs[i], pt);
                if (trackPt != null) {
                    pt.x += trackPt.x;
                    pt.y += trackPt.y;
                }
                positions[n] = pt.x;
                positions[n+1] = pt.y;
            }
        }
    }
    private void addFlags(int newflags) {
        flags = getLayoutFlags() | newflags;
    }
    private void clearFlags(int clearedFlags) {
        flags = getLayoutFlags() & ~clearedFlags;
    }
    private GlyphStrike getGlyphStrike(int ix) {
        if (gti == null) {
            return getDefaultStrike();
        } else {
            return gti.getStrike(ix);
        }
    }
    private GlyphStrike getDefaultStrike() {
        GlyphStrike gs = null;
        if (fsref != null) {
            gs = (GlyphStrike)fsref.get();
        }
        if (gs == null) {
            gs = GlyphStrike.create(this, dtx, null);
            fsref = new SoftReference(gs);
        }
        return gs;
    }
    static final class GlyphTransformInfo {
        StandardGlyphVector sgv;  
        int[] indices;            
        double[] transforms;      
        SoftReference strikesRef; 
        boolean haveAllStrikes;   
        GlyphTransformInfo(StandardGlyphVector sgv) {
            this.sgv = sgv;
        }
        GlyphTransformInfo(StandardGlyphVector sgv, GlyphTransformInfo rhs) {
            this.sgv = sgv;
            this.indices = rhs.indices == null ? null : (int[])rhs.indices.clone();
            this.transforms = rhs.transforms == null ? null : (double[])rhs.transforms.clone();
            this.strikesRef = null; 
        }
        public boolean equals(GlyphTransformInfo rhs) {
            if (rhs == null) {
                return false;
            }
            if (rhs == this) {
                return true;
            }
            if (this.indices.length != rhs.indices.length) {
                return false;
            }
            if (this.transforms.length != rhs.transforms.length) {
                return false;
            }
            for (int i = 0; i < this.indices.length; ++i) {
                int tix = this.indices[i];
                int rix = rhs.indices[i];
                if ((tix == 0) != (rix == 0)) {
                    return false;
                }
                if (tix != 0) {
                    tix *= 6;
                    rix *= 6;
                    for (int j = 6; j > 0; --j) {
                        if (this.indices[--tix] != rhs.indices[--rix]) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
            double[] temp = new double[6];
            boolean isIdentity = true;
            if (newTX == null || newTX.isIdentity()) {
                temp[0] = temp[3] = 1.0;
            }
            else {
                isIdentity = false;
                newTX.getMatrix(temp);
            }
            if (indices == null) {
                if (isIdentity) { 
                    return;
                }
                indices = new int[sgv.glyphs.length];
                indices[glyphIndex] = 1;
                transforms = temp;
            } else {
                boolean addSlot = false; 
                int newIndex = -1;
                if (isIdentity) {
                    newIndex = 0; 
                } else {
                    addSlot = true; 
                    int i;
                loop:
                    for (i = 0; i < transforms.length; i += 6) {
                        for (int j = 0; j < 6; ++j) {
                            if (transforms[i + j] != temp[j]) {
                                continue loop;
                            }
                        }
                        addSlot = false;
                        break;
                    }
                    newIndex = i / 6 + 1; 
                }
                int oldIndex = indices[glyphIndex];
                if (newIndex != oldIndex) {
                    boolean removeSlot = false;
                    if (oldIndex != 0) {
                        removeSlot = true;
                        for (int i = 0; i < indices.length; ++i) {
                            if (indices[i] == oldIndex && i != glyphIndex) {
                                removeSlot = false;
                                break;
                            }
                        }
                    }
                    if (removeSlot && addSlot) { 
                        newIndex = oldIndex;
                        System.arraycopy(temp, 0, transforms, (newIndex - 1) * 6, 6);
                    } else if (removeSlot) {
                        if (transforms.length == 6) { 
                            indices = null;
                            transforms = null;
                            sgv.clearCaches(glyphIndex);
                            sgv.clearFlags(FLAG_HAS_TRANSFORMS);
                            strikesRef = null;
                            return;
                        }
                        double[] ttemp = new double[transforms.length - 6];
                        System.arraycopy(transforms, 0, ttemp, 0, (oldIndex - 1) * 6);
                        System.arraycopy(transforms, oldIndex * 6, ttemp, (oldIndex - 1) * 6,
                                         transforms.length - oldIndex * 6);
                        transforms = ttemp;
                        for (int i = 0; i < indices.length; ++i) {
                            if (indices[i] > oldIndex) { 
                                indices[i] -= 1;
                            }
                        }
                        if (newIndex > oldIndex) { 
                            --newIndex;
                        }
                    } else if (addSlot) {
                        double[] ttemp = new double[transforms.length + 6];
                        System.arraycopy(transforms, 0, ttemp, 0, transforms.length);
                        System.arraycopy(temp, 0, ttemp, transforms.length, 6);
                        transforms = ttemp;
                    }
                    indices[glyphIndex] = newIndex;
                }
            }
            sgv.clearCaches(glyphIndex);
            sgv.addFlags(FLAG_HAS_TRANSFORMS);
            strikesRef = null;
        }
        AffineTransform getGlyphTransform(int ix) {
            int index = indices[ix];
            if (index == 0) {
                return null;
            }
            int x = (index - 1) * 6;
            return new AffineTransform(transforms[x + 0],
                                       transforms[x + 1],
                                       transforms[x + 2],
                                       transforms[x + 3],
                                       transforms[x + 4],
                                       transforms[x + 5]);
        }
        int transformCount() {
            if (transforms == null) {
                return 0;
            }
            return transforms.length / 6;
        }
        Object setupGlyphImages(long[] images, float[] positions, AffineTransform tx) {
            int len = sgv.glyphs.length;
            GlyphStrike[] sl = getAllStrikes();
            for (int i = 0; i < len; ++i) {
                GlyphStrike gs = sl[indices[i]];
                int glyphID = sgv.glyphs[i];
                images[i] = gs.strike.getGlyphImagePtr(glyphID);
                gs.getGlyphPosition(glyphID, i*2, sgv.positions, positions);
            }
            tx.transform(positions, 0, positions, 0, len);
            return sl;
        }
        Rectangle getGlyphsPixelBounds(AffineTransform tx, float x, float y, int start, int count) {
            Rectangle result = null;
            Rectangle r = new Rectangle();
            Point2D.Float pt = new Point.Float();
            int n = start * 2;
            while (--count >= 0) {
                GlyphStrike gs = getStrike(start);
                pt.x = x + sgv.positions[n++] + gs.dx;
                pt.y = y + sgv.positions[n++] + gs.dy;
                tx.transform(pt, pt);
                gs.strike.getGlyphImageBounds(sgv.glyphs[start++], pt, r);
                if (!r.isEmpty()) {
                    if (result == null) {
                        result = new Rectangle(r);
                    } else {
                        result.add(r);
                    }
                }
            }
            return result != null ? result : r;
        }
        GlyphStrike getStrike(int glyphIndex) {
            if (indices != null) {
                GlyphStrike[] strikes = getStrikeArray();
                return getStrikeAtIndex(strikes, indices[glyphIndex]);
            }
            return sgv.getDefaultStrike();
        }
        private GlyphStrike[] getAllStrikes() {
            if (indices == null) {
                return null;
            }
            GlyphStrike[] strikes = getStrikeArray();
            if (!haveAllStrikes) {
                for (int i = 0; i < strikes.length; ++i) {
                    getStrikeAtIndex(strikes, i);
                }
                haveAllStrikes = true;
            }
            return strikes;
        }
        private GlyphStrike[] getStrikeArray() {
            GlyphStrike[] strikes = null;
            if (strikesRef != null) {
                strikes = (GlyphStrike[])strikesRef.get();
            }
            if (strikes == null) {
                haveAllStrikes = false;
                strikes = new GlyphStrike[transformCount() + 1];
                strikesRef = new SoftReference(strikes);
            }
            return strikes;
        }
        private GlyphStrike getStrikeAtIndex(GlyphStrike[] strikes, int strikeIndex) {
            GlyphStrike strike = strikes[strikeIndex];
            if (strike == null) {
                if (strikeIndex == 0) {
                    strike = sgv.getDefaultStrike();
                } else {
                    int ix = (strikeIndex - 1) * 6;
                    AffineTransform gtx = new AffineTransform(transforms[ix],
                                                              transforms[ix+1],
                                                              transforms[ix+2],
                                                              transforms[ix+3],
                                                              transforms[ix+4],
                                                              transforms[ix+5]);
                    strike = GlyphStrike.create(sgv, sgv.dtx, gtx);
                }
                strikes[strikeIndex] = strike;
            }
            return strike;
        }
    }
    public static final class GlyphStrike {
        StandardGlyphVector sgv;
        FontStrike strike; 
        float dx;
        float dy;
        static GlyphStrike create(StandardGlyphVector sgv, AffineTransform dtx, AffineTransform gtx) {
            float dx = 0;
            float dy = 0;
            AffineTransform tx = sgv.ftx;
            if (!dtx.isIdentity() || gtx != null) {
                tx = new AffineTransform(sgv.ftx);
                if (gtx != null) {
                    tx.preConcatenate(gtx);
                    dx = (float)tx.getTranslateX(); 
                    dy = (float)tx.getTranslateY();
                }
                if (!dtx.isIdentity()) {
                    tx.preConcatenate(dtx);
                }
            }
            int ptSize = 1; 
            Object aaHint = sgv.frc.getAntiAliasingHint();
            if (aaHint == VALUE_TEXT_ANTIALIAS_GASP) {
                if (!tx.isIdentity() &&
                    (tx.getType() & ~AffineTransform.TYPE_TRANSLATION) != 0) {
                    double shearx = tx.getShearX();
                    if (shearx != 0) {
                        double scaley = tx.getScaleY();
                        ptSize =
                            (int)Math.sqrt(shearx * shearx + scaley * scaley);
                    } else {
                        ptSize = (int)(Math.abs(tx.getScaleY()));
                    }
                }
            }
            int aa = FontStrikeDesc.getAAHintIntVal(aaHint,sgv.font2D, ptSize);
            int fm = FontStrikeDesc.getFMHintIntVal
                (sgv.frc.getFractionalMetricsHint());
            FontStrikeDesc desc = new FontStrikeDesc(dtx,
                                                     tx,
                                                     sgv.font.getStyle(),
                                                     aa, fm);
            FontStrike strike = sgv.font2D.getStrike(desc);  
            return new GlyphStrike(sgv, strike, dx, dy);
        }
        private GlyphStrike(StandardGlyphVector sgv, FontStrike strike, float dx, float dy) {
            this.sgv = sgv;
            this.strike = strike;
            this.dx = dx;
            this.dy = dy;
        }
        void getADL(ADL result) {
            StrikeMetrics sm = strike.getFontMetrics();
            Point2D.Float delta = null;
            if (sgv.font.isTransformed()) {
                delta = new Point2D.Float();
                delta.x = (float)sgv.font.getTransform().getTranslateX();
                delta.y = (float)sgv.font.getTransform().getTranslateY();
            }
            result.ascentX = -sm.ascentX;
            result.ascentY = -sm.ascentY;
            result.descentX = sm.descentX;
            result.descentY = sm.descentY;
            result.leadingX = sm.leadingX;
            result.leadingY = sm.leadingY;
        }
        void getGlyphPosition(int glyphID, int ix, float[] positions, float[] result) {
            result[ix] = positions[ix] + dx;
            ++ix;
            result[ix] = positions[ix] + dy;
        }
        void addDefaultGlyphAdvance(int glyphID, Point2D.Float result) {
            Point2D.Float adv = strike.getGlyphMetrics(glyphID);
            result.x += adv.x + dx;
            result.y += adv.y + dy;
        }
        Rectangle2D getGlyphOutlineBounds(int glyphID, float x, float y) {
            Rectangle2D result = null;
            if (sgv.invdtx == null) {
                result = new Rectangle2D.Float();
                result.setRect(strike.getGlyphOutlineBounds(glyphID)); 
            } else {
                GeneralPath gp = strike.getGlyphOutline(glyphID, 0, 0);
                gp.transform(sgv.invdtx);
                result = gp.getBounds2D();
            }
            if (!result.isEmpty()) {
                result.setRect(result.getMinX() + x + dx,
                               result.getMinY() + y + dy,
                               result.getWidth(), result.getHeight());
            }
            return result;
        }
        void appendGlyphOutline(int glyphID, GeneralPath result, float x, float y) {
            GeneralPath gp = null;
            if (sgv.invdtx == null) {
                gp = strike.getGlyphOutline(glyphID, x + dx, y + dy);
            } else {
                gp = strike.getGlyphOutline(glyphID, 0, 0);
                gp.transform(sgv.invdtx);
                gp.transform(AffineTransform.getTranslateInstance(x + dx, y + dy));
            }
            PathIterator iterator = gp.getPathIterator(null);
            result.append(iterator, false);
        }
    }
    public String toString() {
        return appendString(null).toString();
    }
    StringBuffer appendString(StringBuffer buf) {
        if (buf == null) {
            buf = new StringBuffer();
        }
        try {
            buf.append("SGV{font: ");
            buf.append(font.toString());
            buf.append(", frc: ");
            buf.append(frc.toString());
            buf.append(", glyphs: (");
            buf.append(glyphs.length);
            buf.append(")[");
            for (int i = 0; i < glyphs.length; ++i) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(Integer.toHexString(glyphs[i]));
            }
            buf.append("]");
            if (positions != null) {
                buf.append(", positions: (");
                buf.append(positions.length);
                buf.append(")[");
                for (int i = 0; i < positions.length; i += 2) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append(positions[i]);
                    buf.append("@");
                    buf.append(positions[i+1]);
                }
                buf.append("]");
            }
            if (charIndices != null) {
                buf.append(", indices: (");
                buf.append(charIndices.length);
                buf.append(")[");
                for (int i = 0; i < charIndices.length; ++i) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append(charIndices[i]);
                }
                buf.append("]");
            }
            buf.append(", flags:");
            if (getLayoutFlags() == 0) {
                buf.append(" default");
            } else {
                if ((flags & FLAG_HAS_TRANSFORMS) != 0) {
                    buf.append(" tx");
                }
                if ((flags & FLAG_HAS_POSITION_ADJUSTMENTS) != 0) {
                    buf.append(" pos");
                }
                if ((flags & FLAG_RUN_RTL) != 0) {
                    buf.append(" rtl");
                }
                if ((flags & FLAG_COMPLEX_GLYPHS) != 0) {
                    buf.append(" complex");
                }
            }
        }
        catch(Exception e) {
            buf.append(" " + e.getMessage());
        }
        buf.append("}");
        return buf;
    }
    static class ADL {
        public float ascentX;
        public float ascentY;
        public float descentX;
        public float descentY;
        public float leadingX;
        public float leadingY;
        public String toString() {
            return toStringBuffer(null).toString();
        }
        protected StringBuffer toStringBuffer(StringBuffer result) {
            if (result == null) {
                result = new StringBuffer();
            }
            result.append("ax: ");
            result.append(ascentX);
            result.append(" ay: ");
            result.append(ascentY);
            result.append(" dx: ");
            result.append(descentX);
            result.append(" dy: ");
            result.append(descentY);
            result.append(" lx: ");
            result.append(leadingX);
            result.append(" ly: ");
            result.append(leadingY);
            return result;
        }
    }
}

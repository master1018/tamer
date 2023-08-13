public final class GlyphList {
    private static final int MINGRAYLENGTH = 1024;
    private static final int MAXGRAYLENGTH = 8192;
    private static final int DEFAULT_LENGTH = 32;
    int glyphindex;
    int metrics[];
    byte graybits[];
    Object strikelist; 
    int len = 0;
    int maxLen = 0;
    int maxPosLen = 0;
    int glyphData[];
    char chData[];
    long images[];
    float positions[];
    float x, y;
    float gposx, gposy;
    boolean usePositions;
    boolean lcdRGBOrder;
    boolean lcdSubPixPos;
    private static GlyphList reusableGL = new GlyphList();
    private static boolean inUse;
    void ensureCapacity(int len) {
        if (len < 0) {
          len = 0;
        }
        if (usePositions && len > maxPosLen) {
            positions = new float[len * 2 + 2];
            maxPosLen = len;
        }
        if (maxLen == 0 || len > maxLen) {
            glyphData = new int[len];
            chData = new char[len];
            images = new long[len];
            maxLen = len;
        }
    }
    private GlyphList() {
    }
    public static GlyphList getInstance() {
        if (inUse) {
            return new GlyphList();
        } else {
            synchronized(GlyphList.class) {
                if (inUse) {
                    return new GlyphList();
                } else {
                    inUse = true;
                    return reusableGL;
                }
            }
        }
    }
    public boolean setFromString(FontInfo info, String str, float x, float y) {
        this.x = x;
        this.y = y;
        this.strikelist = info.fontStrike;
        this.lcdRGBOrder = info.lcdRGBOrder;
        this.lcdSubPixPos = info.lcdSubPixPos;
        len = str.length();
        ensureCapacity(len);
        str.getChars(0, len, chData, 0);
        return mapChars(info, len);
    }
    public boolean setFromChars(FontInfo info, char[] chars, int off, int alen,
                                float x, float y) {
        this.x = x;
        this.y = y;
        this.strikelist = info.fontStrike;
        this.lcdRGBOrder = info.lcdRGBOrder;
        this.lcdSubPixPos = info.lcdSubPixPos;
        len = alen;
        if (alen < 0) {
            len = 0;
        } else {
            len = alen;
        }
        ensureCapacity(len);
        System.arraycopy(chars, off, chData, 0, len);
        return mapChars(info, len);
    }
    private final boolean mapChars(FontInfo info, int len) {
        if (info.font2D.getMapper().charsToGlyphsNS(len, chData, glyphData)) {
            return false;
        }
        info.fontStrike.getGlyphImagePtrs(glyphData, images, len);
        glyphindex = -1;
        return true;
    }
    public void setFromGlyphVector(FontInfo info, GlyphVector gv,
                                   float x, float y) {
        this.x = x;
        this.y = y;
        this.lcdRGBOrder = info.lcdRGBOrder;
        this.lcdSubPixPos = info.lcdSubPixPos;
        StandardGlyphVector sgv = StandardGlyphVector.getStandardGV(gv, info);
        usePositions = sgv.needsPositions(info.devTx);
        len = sgv.getNumGlyphs();
        ensureCapacity(len);
        strikelist = sgv.setupGlyphImages(images,
                                          usePositions ? positions : null,
                                          info.devTx);
        glyphindex = -1;
    }
    public int[] getBounds() {
        if (glyphindex >= 0) {
            throw new InternalError("calling getBounds after setGlyphIndex");
        }
        if (metrics == null) {
            metrics = new int[5];
        }
        gposx = x + 0.5f;
        gposy = y + 0.5f;
        fillBounds(metrics);
        return metrics;
    }
    public void setGlyphIndex(int i) {
        glyphindex = i;
        float gx =
            StrikeCache.unsafe.getFloat(images[i]+StrikeCache.topLeftXOffset);
        float gy =
            StrikeCache.unsafe.getFloat(images[i]+StrikeCache.topLeftYOffset);
        if (usePositions) {
            metrics[0] = (int)Math.floor(positions[(i<<1)]   + gposx + gx);
            metrics[1] = (int)Math.floor(positions[(i<<1)+1] + gposy + gy);
        } else {
            metrics[0] = (int)Math.floor(gposx + gx);
            metrics[1] = (int)Math.floor(gposy + gy);
            gposx += StrikeCache.unsafe.getFloat
                (images[i]+StrikeCache.xAdvanceOffset);
            gposy += StrikeCache.unsafe.getFloat
                (images[i]+StrikeCache.yAdvanceOffset);
        }
        metrics[2] =
            StrikeCache.unsafe.getChar(images[i]+StrikeCache.widthOffset);
        metrics[3] =
            StrikeCache.unsafe.getChar(images[i]+StrikeCache.heightOffset);
        metrics[4] =
            StrikeCache.unsafe.getChar(images[i]+StrikeCache.rowBytesOffset);
    }
    public int[] getMetrics() {
        return metrics;
    }
    public byte[] getGrayBits() {
        int len = metrics[4] * metrics[3];
        if (graybits == null) {
            graybits = new byte[Math.max(len, MINGRAYLENGTH)];
        } else {
            if (len > graybits.length) {
                graybits = new byte[len];
            }
        }
        long pixelDataAddress;
        if (StrikeCache.nativeAddressSize == 4) {
            pixelDataAddress = 0xffffffff &
                StrikeCache.unsafe.getInt(images[glyphindex] +
                                          StrikeCache.pixelDataOffset);
        } else {
            pixelDataAddress =
            StrikeCache.unsafe.getLong(images[glyphindex] +
                                       StrikeCache.pixelDataOffset);
        }
        if (pixelDataAddress == 0L) {
            return graybits;
        }
        for (int i=0; i<len; i++) {
            graybits[i] = StrikeCache.unsafe.getByte(pixelDataAddress+i);
        }
        return graybits;
    }
    public long[] getImages() {
        return images;
    }
    public boolean usePositions() {
        return usePositions;
    }
    public float[] getPositions() {
        return positions;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public Object getStrike() {
        return strikelist;
    }
    public boolean isSubPixPos() {
        return lcdSubPixPos;
    }
    public boolean isRGBOrder() {
        return lcdRGBOrder;
    }
    public void dispose() {
        if (this == reusableGL) {
            if (graybits != null && graybits.length > MAXGRAYLENGTH) {
                graybits = null;
            }
            usePositions = false;
            strikelist = null; 
            inUse = false;
        }
    }
    public int getNumGlyphs() {
        return len;
    }
    private void fillBounds(int[] bounds) {
        int xOffset = StrikeCache.topLeftXOffset;
        int yOffset = StrikeCache.topLeftYOffset;
        int wOffset = StrikeCache.widthOffset;
        int hOffset = StrikeCache.heightOffset;
        int xAdvOffset = StrikeCache.xAdvanceOffset;
        int yAdvOffset = StrikeCache.yAdvanceOffset;
        if (len == 0) {
            bounds[0] = bounds[1] = bounds[2] = bounds[3] = 0;
            return;
        }
        float bx0, by0, bx1, by1;
        bx0 = by0 = Float.POSITIVE_INFINITY;
        bx1 = by1 = Float.NEGATIVE_INFINITY;
        int posIndex = 0;
        float glx = x + 0.5f;
        float gly = y + 0.5f;
        char gw, gh;
        float gx, gy, gx0, gy0, gx1, gy1;
        for (int i=0; i<len; i++) {
            gx = StrikeCache.unsafe.getFloat(images[i]+xOffset);
            gy = StrikeCache.unsafe.getFloat(images[i]+yOffset);
            gw = StrikeCache.unsafe.getChar(images[i]+wOffset);
            gh = StrikeCache.unsafe.getChar(images[i]+hOffset);
            if (usePositions) {
                gx0 = positions[posIndex++] + gx + glx;
                gy0 = positions[posIndex++] + gy + gly;
            } else {
                gx0 = glx + gx;
                gy0 = gly + gy;
                glx += StrikeCache.unsafe.getFloat(images[i]+xAdvOffset);
                gly += StrikeCache.unsafe.getFloat(images[i]+yAdvOffset);
            }
            gx1 = gx0 + gw;
            gy1 = gy0 + gh;
            if (bx0 > gx0) bx0 = gx0;
            if (by0 > gy0) by0 = gy0;
            if (bx1 < gx1) bx1 = gx1;
            if (by1 < gy1) by1 = gy1;
        }
        bounds[0] = (int)Math.floor(bx0);
        bounds[1] = (int)Math.floor(by0);
        bounds[2] = (int)Math.floor(bx1);
        bounds[3] = (int)Math.floor(by1);
    }
}

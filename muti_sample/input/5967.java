public abstract class PathGraphics extends ProxyGraphics2D {
    private Printable mPainter;
    private PageFormat mPageFormat;
    private int mPageIndex;
    private boolean mCanRedraw;
    protected boolean printingGlyphVector;
    protected PathGraphics(Graphics2D graphics, PrinterJob printerJob,
                           Printable painter, PageFormat pageFormat,
                           int pageIndex, boolean canRedraw) {
        super(graphics, printerJob);
        mPainter = painter;
        mPageFormat = pageFormat;
        mPageIndex = pageIndex;
        mCanRedraw = canRedraw;
    }
    protected Printable getPrintable() {
        return mPainter;
    }
    protected PageFormat getPageFormat() {
        return mPageFormat;
    }
    protected int getPageIndex() {
        return mPageIndex;
    }
    public boolean canDoRedraws() {
        return mCanRedraw;
    }
    public abstract void redrawRegion(Rectangle2D region,
                                      double scaleX, double scaleY,
                                      Shape clip,
                                      AffineTransform devTransform)
                    throws PrinterException ;
    public void drawLine(int x1, int y1, int x2, int y2) {
        Paint paint = getPaint();
        try {
            AffineTransform deviceTransform = getTransform();
            if (getClip() != null) {
                deviceClip(getClip().getPathIterator(deviceTransform));
            }
            deviceDrawLine(x1, y1, x2, y2, (Color) paint);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected a Color instance");
        }
    }
    public void drawRect(int x, int y, int width, int height) {
        Paint paint = getPaint();
        try {
            AffineTransform deviceTransform = getTransform();
            if (getClip() != null) {
                deviceClip(getClip().getPathIterator(deviceTransform));
            }
            deviceFrameRect(x, y, width, height, (Color) paint);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected a Color instance");
        }
    }
    public void fillRect(int x, int y, int width, int height){
        Paint paint = getPaint();
        try {
            AffineTransform deviceTransform = getTransform();
            if (getClip() != null) {
                deviceClip(getClip().getPathIterator(deviceTransform));
            }
            deviceFillRect(x, y, width, height, (Color) paint);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected a Color instance");
        }
    }
    public void clearRect(int x, int y, int width, int height) {
        fill(new Rectangle2D.Float(x, y, width, height), getBackground());
    }
    public void drawRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        draw(new RoundRectangle2D.Float(x, y,
                                        width, height,
                                        arcWidth, arcHeight));
    }
    public void fillRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        fill(new RoundRectangle2D.Float(x, y,
                                        width, height,
                                        arcWidth, arcHeight));
    }
    public void drawOval(int x, int y, int width, int height) {
        draw(new Ellipse2D.Float(x, y, width, height));
    }
    public void fillOval(int x, int y, int width, int height){
        fill(new Ellipse2D.Float(x, y, width, height));
    }
    public void drawArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle) {
        draw(new Arc2D.Float(x, y, width, height,
                             startAngle, arcAngle,
                             Arc2D.OPEN));
    }
    public void fillArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle) {
        fill(new Arc2D.Float(x, y, width, height,
                             startAngle, arcAngle,
                             Arc2D.PIE));
    }
    public void drawPolyline(int xPoints[], int yPoints[],
                             int nPoints) {
        float fromX;
        float fromY;
        float toX;
        float toY;
        if (nPoints > 0) {
            fromX = xPoints[0];
            fromY = yPoints[0];
            for(int i = 1; i < nPoints; i++) {
                toX = xPoints[i];
                toY = yPoints[i];
                draw(new Line2D.Float(fromX, fromY, toX, toY));
                fromX = toX;
                fromY = toY;
            }
        }
    }
    public void drawPolygon(int xPoints[], int yPoints[],
                                     int nPoints) {
        draw(new Polygon(xPoints, yPoints, nPoints));
    }
    public void drawPolygon(Polygon p) {
        draw(p);
    }
    public void fillPolygon(int xPoints[], int yPoints[],
                            int nPoints) {
        fill(new Polygon(xPoints, yPoints, nPoints));
    }
    public void fillPolygon(Polygon p) {
        fill(p);
    }
    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }
    public void drawString(String str, float x, float y) {
        if (str.length() == 0) {
            return;
        }
        TextLayout layout =
            new TextLayout(str, getFont(), getFontRenderContext());
        layout.draw(this, x, y);
    }
    protected void drawString(String str, float x, float y,
                              Font font, FontRenderContext frc, float w) {
        TextLayout layout =
            new TextLayout(str, font, frc);
        Shape textShape =
            layout.getOutline(AffineTransform.getTranslateInstance(x, y));
        fill(textShape);
    }
    public void drawString(AttributedCharacterIterator iterator,
                           int x, int y) {
        drawString(iterator, (float) x, (float) y);
    }
    public void drawString(AttributedCharacterIterator iterator,
                           float x, float y) {
        if (iterator == null) {
            throw
                new NullPointerException("attributedcharacteriterator is null");
        }
        TextLayout layout =
            new TextLayout(iterator, getFontRenderContext());
        layout.draw(this, x, y);
    }
    public void drawGlyphVector(GlyphVector g,
                                float x,
                                float y) {
        if (printingGlyphVector) {
            assert !printingGlyphVector; 
            fill(g.getOutline(x, y));
            return;
        }
        try {
            printingGlyphVector = true;
            if (RasterPrinterJob.shapeTextProp ||
                !printedSimpleGlyphVector(g, x, y)) {
                fill(g.getOutline(x, y));
            }
        } finally {
            printingGlyphVector = false;
        }
    }
    protected static SoftReference<Hashtable<Font2DHandle,Object>>
        fontMapRef = new SoftReference<Hashtable<Font2DHandle,Object>>(null);
    protected int platformFontCount(Font font, String str) {
        return 0;
    }
    protected boolean printGlyphVector(GlyphVector gv, float x, float y) {
        return false;
    }
    boolean printedSimpleGlyphVector(GlyphVector g, float x, float y) {
        int flags = g.getLayoutFlags();
        if (flags != 0 && flags != GlyphVector.FLAG_HAS_POSITION_ADJUSTMENTS) {
            return printGlyphVector(g, x, y);
        }
        Font font = g.getFont();
        Font2D font2D = FontUtilities.getFont2D(font);
        if (font2D.handle.font2D != font2D) {
            return false;
        }
        Hashtable<Font2DHandle,Object> fontMap;
        synchronized (PathGraphics.class) {
            fontMap = fontMapRef.get();
            if (fontMap == null) {
                fontMap = new Hashtable<Font2DHandle,Object>();
                fontMapRef =
                    new SoftReference<Hashtable<Font2DHandle,Object>>(fontMap);
            }
        }
        int numGlyphs = g.getNumGlyphs();
        int[] glyphCodes = g.getGlyphCodes(0, numGlyphs, null);
        char[] glyphToCharMap = null;
        char[][] mapArray = null;
        CompositeFont cf = null;
        synchronized (fontMap) {
            if (font2D instanceof CompositeFont) {
                cf = (CompositeFont)font2D;
                int numSlots = cf.getNumSlots();
                mapArray = (char[][])fontMap.get(font2D.handle);
                if (mapArray == null) {
                    mapArray = new char[numSlots][];
                    fontMap.put(font2D.handle, mapArray);
                }
                for (int i=0; i<numGlyphs;i++) {
                    int slot = glyphCodes[i] >>> 24;
                    if (slot >= numSlots) { 
                        return false;
                    }
                    if (mapArray[slot] == null) {
                        Font2D slotFont = cf.getSlotFont(slot);
                        char[] map = (char[])fontMap.get(slotFont.handle);
                        if (map == null) {
                            map = getGlyphToCharMapForFont(slotFont);
                        }
                        mapArray[slot] = map;
                    }
                }
            } else {
                glyphToCharMap = (char[])fontMap.get(font2D.handle);
                if (glyphToCharMap == null) {
                    glyphToCharMap = getGlyphToCharMapForFont(font2D);
                    fontMap.put(font2D.handle, glyphToCharMap);
                }
            }
        }
        char[] chars = new char[numGlyphs];
        if (cf != null) {
            for (int i=0; i<numGlyphs; i++) {
                int gc = glyphCodes[i];
                char[] map = mapArray[gc >>> 24];
                gc = gc & 0xffffff;
                if (map == null) {
                    return false;
                }
                char ch;
                if (gc == CharToGlyphMapper.INVISIBLE_GLYPH_ID) {
                    ch = '\n';
                } else if (gc < 0 || gc >= map.length) {
                    return false;
                } else {
                    ch = map[gc];
                }
                if (ch != CharToGlyphMapper.INVISIBLE_GLYPH_ID) {
                    chars[i] = ch;
                } else {
                    return false;
                }
            }
        } else {
            for (int i=0; i<numGlyphs; i++) {
                int gc = glyphCodes[i];
                char ch;
                if (gc == CharToGlyphMapper.INVISIBLE_GLYPH_ID) {
                    ch = '\n';
                } else if (gc < 0 || gc >= glyphToCharMap.length) {
                    return false;
                } else {
                    ch = glyphToCharMap[gc];
                }
                if (ch != CharToGlyphMapper.INVISIBLE_GLYPH_ID) {
                    chars[i] = ch;
                } else {
                    return false;
                }
            }
        }
        FontRenderContext gvFrc = g.getFontRenderContext();
        GlyphVector gv2 = font.createGlyphVector(gvFrc, chars);
        if (gv2.getNumGlyphs() != numGlyphs) {
            return printGlyphVector(g, x, y);
        }
        int[] glyphCodes2 = gv2.getGlyphCodes(0, numGlyphs, null);
        for (int i=0; i<numGlyphs; i++) {
            if (glyphCodes[i] != glyphCodes2[i]) {
                return printGlyphVector(g, x, y);
            }
        }
        FontRenderContext g2dFrc = getFontRenderContext();
        boolean compatibleFRC = gvFrc.equals(g2dFrc);
        if (!compatibleFRC &&
            gvFrc.usesFractionalMetrics() == g2dFrc.usesFractionalMetrics()) {
            AffineTransform gvAT = gvFrc.getTransform();
            AffineTransform g2dAT = getTransform();
            double[] gvMatrix = new double[4];
            double[] g2dMatrix = new double[4];
            gvAT.getMatrix(gvMatrix);
            g2dAT.getMatrix(g2dMatrix);
            compatibleFRC = true;
            for (int i=0;i<4;i++) {
                if (gvMatrix[i] != g2dMatrix[i]) {
                    compatibleFRC = false;
                    break;
                }
            }
        }
        String str = new String(chars, 0, numGlyphs);
        int numFonts = platformFontCount(font, str);
        if (numFonts == 0) {
            return false;
        }
        float[] positions = g.getGlyphPositions(0, numGlyphs, null);
        boolean noPositionAdjustments =
            ((flags & GlyphVector.FLAG_HAS_POSITION_ADJUSTMENTS) == 0) ||
            samePositions(gv2, glyphCodes2, glyphCodes, positions);
        Point2D gvAdvancePt = g.getGlyphPosition(numGlyphs);
        float gvAdvanceX = (float)gvAdvancePt.getX();
        boolean layoutAffectsAdvance = false;
        if (font.hasLayoutAttributes() && printingGlyphVector &&
            noPositionAdjustments) {
            Map<TextAttribute, ?> map = font.getAttributes();
            Object o = map.get(TextAttribute.TRACKING);
            boolean tracking = o != null && (o instanceof Number) &&
                (((Number)o).floatValue() != 0f);
            if (tracking) {
                noPositionAdjustments = false;
            } else {
                Rectangle2D bounds = font.getStringBounds(str, gvFrc);
                float strAdvanceX = (float)bounds.getWidth();
                if (Math.abs(strAdvanceX - gvAdvanceX) > 0.00001) {
                    layoutAffectsAdvance = true;
                }
            }
        }
        if (compatibleFRC && noPositionAdjustments && !layoutAffectsAdvance) {
            drawString(str, x, y, font, gvFrc, 0f);
            return true;
        }
        if (numFonts == 1 && canDrawStringToWidth() && noPositionAdjustments) {
            drawString(str, x, y, font, gvFrc, gvAdvanceX);
            return true;
        }
        if (FontUtilities.isComplexText(chars, 0, chars.length)) {
            return printGlyphVector(g, x, y);
        }
        if (numGlyphs > 10 && printGlyphVector(g, x, y)) {
            return true;
        }
        for (int i=0; i<numGlyphs; i++) {
            String s = new String(chars, i, 1);
            drawString(s, x+positions[i*2], y+positions[i*2+1],
                       font, gvFrc, 0f);
        }
        return true;
    }
    private boolean samePositions(GlyphVector gv, int[] gvcodes,
                                  int[] origCodes, float[] origPositions) {
        int numGlyphs = gv.getNumGlyphs();
        float[] gvpos = gv.getGlyphPositions(0, numGlyphs, null);
        if (numGlyphs != gvcodes.length ||  
            origCodes.length != gvcodes.length ||
            origPositions.length != gvpos.length) {
            return false;
        }
        for (int i=0; i<numGlyphs; i++) {
            if (gvcodes[i] != origCodes[i] || gvpos[i] != origPositions[i]) {
                return false;
            }
        }
        return true;
    }
    protected boolean canDrawStringToWidth() {
        return false;
    }
    private static char[] getGlyphToCharMapForFont(Font2D font2D) {
        int numGlyphs = font2D.getNumGlyphs();
        int missingGlyph = font2D.getMissingGlyphCode();
        char[] glyphToCharMap = new char[numGlyphs];
        int glyph;
        for (int i=0;i<numGlyphs; i++) {
            glyphToCharMap[i] = CharToGlyphMapper.INVISIBLE_GLYPH_ID;
        }
        for (char c=0; c<0xFFFF; c++) {
           if (c >= CharToGlyphMapper.HI_SURROGATE_START &&
               c <= CharToGlyphMapper.LO_SURROGATE_END) {
                continue;
            }
            glyph = font2D.charToGlyph(c);
            if (glyph != missingGlyph && glyph < numGlyphs &&
                (glyphToCharMap[glyph] ==
                 CharToGlyphMapper.INVISIBLE_GLYPH_ID)) {
                glyphToCharMap[glyph] = c;
            }
        }
        return glyphToCharMap;
    }
    public void draw(Shape s) {
        fill(getStroke().createStrokedShape(s));
    }
    public void fill(Shape s) {
        Paint paint = getPaint();
        try {
            fill(s, (Color) paint);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected a Color instance");
        }
    }
    public void fill(Shape s, Color color) {
        AffineTransform deviceTransform = getTransform();
        if (getClip() != null) {
            deviceClip(getClip().getPathIterator(deviceTransform));
        }
        deviceFill(s.getPathIterator(deviceTransform), color);
    }
    protected abstract void deviceFill(PathIterator pathIter, Color color);
    protected abstract void deviceClip(PathIterator pathIter);
    protected abstract void deviceFrameRect(int x, int y,
                                            int width, int height,
                                            Color color);
    protected abstract void deviceDrawLine(int xBegin, int yBegin,
                                           int xEnd, int yEnd, Color color);
    protected abstract void deviceFillRect(int x, int y,
                                           int width, int height, Color color);
    protected BufferedImage getBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        } else if (img instanceof ToolkitImage) {
            return ((ToolkitImage)img).getBufferedImage();
        } else if (img instanceof VolatileImage) {
            return ((VolatileImage)img).getSnapshot();
        } else {
            return null;
        }
    }
    protected boolean hasTransparentPixels(BufferedImage bufferedImage) {
        ColorModel colorModel = bufferedImage.getColorModel();
        boolean hasTransparency = colorModel == null
            ? true
            : colorModel.getTransparency() != ColorModel.OPAQUE;
        if (hasTransparency && bufferedImage != null) {
            if (bufferedImage.getType()==BufferedImage.TYPE_INT_ARGB ||
                bufferedImage.getType()==BufferedImage.TYPE_INT_ARGB_PRE) {
                DataBuffer db =  bufferedImage.getRaster().getDataBuffer();
                SampleModel sm = bufferedImage.getRaster().getSampleModel();
                if (db instanceof DataBufferInt &&
                    sm instanceof SinglePixelPackedSampleModel) {
                    SinglePixelPackedSampleModel psm =
                        (SinglePixelPackedSampleModel)sm;
                    int[] int_data =
                        SunWritableRaster.stealData((DataBufferInt) db, 0);
                    int x = bufferedImage.getMinX();
                    int y = bufferedImage.getMinY();
                    int w = bufferedImage.getWidth();
                    int h = bufferedImage.getHeight();
                    int stride = psm.getScanlineStride();
                    boolean hastranspixel = false;
                    for (int j = y; j < y+h; j++) {
                        int yoff = j * stride;
                        for (int i = x; i < x+w; i++) {
                            if ((int_data[yoff+i] & 0xff000000)!=0xff000000 ) {
                                hastranspixel = true;
                                break;
                            }
                        }
                        if (hastranspixel) {
                            break;
                        }
                    }
                    if (hastranspixel == false) {
                        hasTransparency = false;
                    }
                }
            }
        }
        return hasTransparency;
    }
    protected boolean isBitmaskTransparency(BufferedImage bufferedImage) {
        ColorModel colorModel = bufferedImage.getColorModel();
        return (colorModel != null &&
                colorModel.getTransparency() == ColorModel.BITMASK);
    }
    protected boolean drawBitmaskImage(BufferedImage bufferedImage,
                                       AffineTransform xform,
                                       Color bgcolor,
                                       int srcX, int srcY,
                                       int srcWidth, int srcHeight) {
        ColorModel colorModel = bufferedImage.getColorModel();
        IndexColorModel icm;
        int [] pixels;
        if (!(colorModel instanceof IndexColorModel)) {
            return false;
        } else {
            icm = (IndexColorModel)colorModel;
        }
        if (colorModel.getTransparency() != ColorModel.BITMASK) {
            return false;
        }
        if (bgcolor != null && bgcolor.getAlpha() < 128) {
            return false;
        }
        if ((xform.getType()
             & ~( AffineTransform.TYPE_UNIFORM_SCALE
                  | AffineTransform.TYPE_TRANSLATION
                  | AffineTransform.TYPE_QUADRANT_ROTATION
                  )) != 0) {
            return false;
        }
        if ((getTransform().getType()
             & ~( AffineTransform.TYPE_UNIFORM_SCALE
                  | AffineTransform.TYPE_TRANSLATION
                  | AffineTransform.TYPE_QUADRANT_ROTATION
                  )) != 0) {
            return false;
        }
        BufferedImage subImage = null;
        Raster raster = bufferedImage.getRaster();
        int transpixel = icm.getTransparentPixel();
        byte[] alphas = new byte[icm.getMapSize()];
        icm.getAlphas(alphas);
        if (transpixel >= 0) {
            alphas[transpixel] = 0;
        }
        int rw = raster.getWidth();
        int rh = raster.getHeight();
        if (srcX > rw || srcY > rh) {
            return false;
        }
        int right, bottom, wid, hgt;
        if (srcX+srcWidth > rw) {
            right = rw;
            wid = right - srcX;
        } else {
            right = srcX+srcWidth;
            wid = srcWidth;
        }
        if (srcY+srcHeight > rh) {
            bottom = rh;
            hgt = bottom - srcY;
        } else {
            bottom = srcY+srcHeight;
            hgt = srcHeight;
        }
        pixels = new int[wid];
        for (int j=srcY; j<bottom; j++) {
            int startx = -1;
            raster.getPixels(srcX, j, wid, 1, pixels);
            for (int i=srcX; i<right; i++) {
                if (alphas[pixels[i-srcX]] == 0) {
                    if (startx >=0) {
                        subImage = bufferedImage.getSubimage(startx, j,
                                                             i-startx, 1);
                        xform.translate(startx, j);
                        drawImageToPlatform(subImage, xform, bgcolor,
                                      0, 0, i-startx, 1, true);
                        xform.translate(-startx, -j);
                        startx = -1;
                    }
                } else if (startx < 0) {
                    startx = i;
                }
            }
            if (startx >= 0) {
                subImage = bufferedImage.getSubimage(startx, j,
                                                     right - startx, 1);
                xform.translate(startx, j);
                drawImageToPlatform(subImage, xform, bgcolor,
                              0, 0, right - startx, 1, true);
                xform.translate(-startx, -j);
            }
        }
        return true;
    }
    protected abstract boolean
        drawImageToPlatform(Image img, AffineTransform xform,
                            Color bgcolor,
                            int srcX, int srcY,
                            int srcWidth, int srcHeight,
                            boolean handlingTransparency);
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        return drawImage(img, x, y, null, observer);
    }
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             ImageObserver observer) {
        return drawImage(img, x, y, width, height, null, observer);
    }
    public boolean drawImage(Image img, int x, int y,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        boolean result;
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);
        if (srcWidth < 0 || srcHeight < 0) {
            result = false;
        } else {
            result = drawImage(img, x, y, srcWidth, srcHeight, bgcolor, observer);
        }
        return result;
    }
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        boolean result;
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);
        if (srcWidth < 0 || srcHeight < 0) {
            result = false;
        } else {
            result = drawImage(img,
                         x, y, x + width, y + height,
                         0, 0, srcWidth, srcHeight,
                         observer);
        }
        return result;
    }
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {
        return drawImage(img,
                         dx1, dy1, dx2, dy2,
                         sx1, sy1, sx2, sy2,
                         null, observer);
    }
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        int imgWidth = img.getWidth(null);
        int imgHeight = img.getHeight(null);
        if (imgWidth < 0 || imgHeight < 0) {
            return true;
        }
        int srcWidth = sx2 - sx1;
        int srcHeight = sy2 - sy1;
        float scalex = (float) (dx2 - dx1) / srcWidth;
        float scaley = (float) (dy2 - dy1) / srcHeight;
        AffineTransform xForm
            = new AffineTransform(scalex,
                                  0,
                                  0,
                                  scaley,
                                  dx1 - (sx1 * scalex),
                                  dy1 - (sy1 * scaley));
        int tmp=0;
        if (sx2 < sx1) {
            tmp = sx1;
            sx1 = sx2;
            sx2 = tmp;
        }
        if (sy2 < sy1) {
            tmp = sy1;
            sy1 = sy2;
            sy2 = tmp;
        }
        if (sx1 < 0) {
            sx1 = 0;
        } else if (sx1 > imgWidth) { 
            sx1 = imgWidth;
        }
        if (sx2 < 0) { 
            sx2 = 0;
        } else if (sx2 > imgWidth) {
            sx2 = imgWidth;
        }
        if (sy1 < 0) {
            sy1 = 0;
        } else if (sy1 > imgHeight) { 
            sy1 = imgHeight;
        }
        if (sy2 < 0) {  
            sy2 = 0;
        } else if (sy2 > imgHeight) {
            sy2 = imgHeight;
        }
        srcWidth =  sx2 - sx1;
        srcHeight = sy2 - sy1;
        if (srcWidth <= 0 || srcHeight <= 0) {
            return true;
        }
        return drawImageToPlatform(img, xForm, bgcolor,
                                   sx1, sy1, srcWidth, srcHeight, false);
    }
    public boolean drawImage(Image img,
                             AffineTransform xform,
                             ImageObserver obs) {
        if (img == null) {
            return true;
        }
        boolean result;
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);
        if (srcWidth < 0 || srcHeight < 0) {
            result = false;
        } else {
            result = drawImageToPlatform(img, xform, null,
                                         0, 0, srcWidth, srcHeight, false);
        }
        return result;
    }
    public void drawImage(BufferedImage img,
                          BufferedImageOp op,
                          int x,
                          int y) {
        if (img == null) {
            return;
        }
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);
        if (op != null) {
            img = op.filter(img, null);
        }
        if (srcWidth <= 0 || srcHeight <= 0) {
            return;
        } else {
            AffineTransform xform = new AffineTransform(1f,0f,0f,1f,x,y);
            drawImageToPlatform(img, xform, null,
                                0, 0, srcWidth, srcHeight, false);
        }
    }
    public void drawRenderedImage(RenderedImage img,
                                  AffineTransform xform) {
        if (img == null) {
            return;
        }
        BufferedImage bufferedImage = null;
        int srcWidth = img.getWidth();
        int srcHeight = img.getHeight();
        if (srcWidth <= 0 || srcHeight <= 0) {
            return;
        }
        if (img instanceof BufferedImage) {
            bufferedImage = (BufferedImage) img;
        } else {
            bufferedImage = new BufferedImage(srcWidth, srcHeight,
                                              BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = bufferedImage.createGraphics();
            imageGraphics.drawRenderedImage(img, xform);
        }
        drawImageToPlatform(bufferedImage, xform, null,
                            0, 0, srcWidth, srcHeight, false);
    }
}

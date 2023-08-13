class WPathGraphics extends PathGraphics {
    private static final int DEFAULT_USER_RES = 72;
    private static final float MIN_DEVICE_LINEWIDTH = 1.2f;
    private static final float MAX_THINLINE_INCHES = 0.014f;
    private static boolean useGDITextLayout = true;
    private static boolean preferGDITextLayout = false;
    static {
        String textLayoutStr =
            (String)java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction(
                         "sun.java2d.print.enableGDITextLayout"));
        if (textLayoutStr != null) {
            useGDITextLayout = Boolean.getBoolean(textLayoutStr);
            if (!useGDITextLayout) {
                if (textLayoutStr.equalsIgnoreCase("prefer")) {
                    useGDITextLayout = true;
                    preferGDITextLayout = true;
                }
            }
        }
    }
    WPathGraphics(Graphics2D graphics, PrinterJob printerJob,
                  Printable painter, PageFormat pageFormat, int pageIndex,
                  boolean canRedraw) {
        super(graphics, printerJob, painter, pageFormat, pageIndex, canRedraw);
    }
    @Override
    public Graphics create() {
        return new WPathGraphics((Graphics2D) getDelegate().create(),
                                 getPrinterJob(),
                                 getPrintable(),
                                 getPageFormat(),
                                 getPageIndex(),
                                 canDoRedraws());
    }
    @Override
    public void draw(Shape s) {
        Stroke stroke = getStroke();
        if (stroke instanceof BasicStroke) {
            BasicStroke lineStroke;
            BasicStroke minLineStroke = null;
            float deviceLineWidth;
            float lineWidth;
            AffineTransform deviceTransform;
            Point2D.Float penSize;
            lineStroke = (BasicStroke) stroke;
            lineWidth = lineStroke.getLineWidth();
            penSize = new Point2D.Float(lineWidth, lineWidth);
            deviceTransform = getTransform();
            deviceTransform.deltaTransform(penSize, penSize);
            deviceLineWidth = Math.min(Math.abs(penSize.x),
                                       Math.abs(penSize.y));
            if (deviceLineWidth < MIN_DEVICE_LINEWIDTH) {
                Point2D.Float minPenSize = new Point2D.Float(
                                                MIN_DEVICE_LINEWIDTH,
                                                MIN_DEVICE_LINEWIDTH);
                try {
                    AffineTransform inverse;
                    float minLineWidth;
                    inverse = deviceTransform.createInverse();
                    inverse.deltaTransform(minPenSize, minPenSize);
                    minLineWidth = Math.max(Math.abs(minPenSize.x),
                                            Math.abs(minPenSize.y));
                    minLineStroke = new BasicStroke(minLineWidth,
                                                    lineStroke.getEndCap(),
                                                    lineStroke.getLineJoin(),
                                                    lineStroke.getMiterLimit(),
                                                    lineStroke.getDashArray(),
                                                    lineStroke.getDashPhase());
                    setStroke(minLineStroke);
                } catch (NoninvertibleTransformException e) {
                }
            }
            super.draw(s);
            if (minLineStroke != null) {
                setStroke(lineStroke);
            }
        } else {
            super.draw(s);
        }
    }
    @Override
    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }
    @Override
     public void drawString(String str, float x, float y) {
         drawString(str, x, y, getFont(), getFontRenderContext(), 0f);
     }
    @Override
    protected int platformFontCount(Font font, String str) {
        AffineTransform deviceTransform = getTransform();
        AffineTransform fontTransform = new AffineTransform(deviceTransform);
        fontTransform.concatenate(getFont().getTransform());
        int transformType = fontTransform.getType();
        boolean directToGDI = ((transformType !=
                               AffineTransform.TYPE_GENERAL_TRANSFORM)
                               && ((transformType & AffineTransform.TYPE_FLIP)
                                   == 0));
        if (!directToGDI) {
            return 0;
        }
        Font2D font2D = FontUtilities.getFont2D(font);
        if (font2D instanceof CompositeFont ||
            font2D instanceof TrueTypeFont) {
            return 1;
        } else {
            return 0;
        }
    }
    private static boolean isXP() {
        String osVersion = System.getProperty("os.version");
        if (osVersion != null) {
            Float version = Float.valueOf(osVersion);
            return (version.floatValue() >= 5.1f);
        } else {
            return false;
        }
    }
    private boolean strNeedsTextLayout(String str, Font font) {
        char[] chars = str.toCharArray();
        boolean isComplex = FontUtilities.isComplexText(chars, 0, chars.length);
        if (!isComplex) {
            return false;
        } else if (!useGDITextLayout) {
            return true;
        } else {
            if (preferGDITextLayout ||
                (isXP() && FontUtilities.textLayoutIsCompatible(font))) {
                return false;
            } else {
                return true;
            }
        }
    }
    private int getAngle(Point2D.Double pt) {
        double angle = Math.toDegrees(Math.atan2(pt.y, pt.x));
        if (angle < 0.0) {
            angle+= 360.0;
        }
        if (angle != 0.0) {
            angle = 360.0 - angle;
        }
        return (int)Math.round(angle * 10.0);
    }
    private float getAwScale(double scaleFactorX, double scaleFactorY) {
        float awScale = (float)(scaleFactorX/scaleFactorY);
        if (awScale > 0.999f && awScale < 1.001f) {
            awScale = 1.0f;
        }
        return awScale;
    }
    @Override
    public void drawString(String str, float x, float y,
                           Font font, FontRenderContext frc, float targetW) {
        if (str.length() == 0) {
            return;
        }
        if (WPrinterJob.shapeTextProp) {
            super.drawString(str, x, y, font, frc, targetW);
            return;
        }
        boolean layoutNeeded = strNeedsTextLayout(str, font);
        if ((font.hasLayoutAttributes() || layoutNeeded)
            && !printingGlyphVector) {
            TextLayout layout = new TextLayout(str, font, frc);
            layout.draw(this, x, y);
            return;
        } else if (layoutNeeded) {
            super.drawString(str, x, y, font, frc, targetW);
            return;
        }
        AffineTransform deviceTransform = getTransform();
        AffineTransform fontTransform = new AffineTransform(deviceTransform);
        fontTransform.concatenate(font.getTransform());
        int transformType = fontTransform.getType();
        boolean directToGDI = ((transformType !=
                               AffineTransform.TYPE_GENERAL_TRANSFORM)
                               && ((transformType & AffineTransform.TYPE_FLIP)
                                   == 0));
        WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
        try {
            wPrinterJob.setTextColor((Color)getPaint());
        } catch (ClassCastException e) { 
            directToGDI = false;
        }
        if (!directToGDI) {
            super.drawString(str, x, y, font, frc, targetW);
            return;
        }
        Point2D.Float userpos = new Point2D.Float(x, y);
        Point2D.Float devpos = new Point2D.Float();
        if (font.isTransformed()) {
            AffineTransform fontTx = font.getTransform();
            float translateX = (float)(fontTx.getTranslateX());
            float translateY = (float)(fontTx.getTranslateY());
            if (Math.abs(translateX) < 0.00001) translateX = 0f;
            if (Math.abs(translateY) < 0.00001) translateY = 0f;
            userpos.x += translateX; userpos.y += translateY;
        }
        deviceTransform.transform(userpos, devpos);
        if (getClip() != null) {
            deviceClip(getClip().getPathIterator(deviceTransform));
        }
        float fontSize = font.getSize2D();
        Point2D.Double pty = new Point2D.Double(0.0, 1.0);
        fontTransform.deltaTransform(pty, pty);
        double scaleFactorY = Math.sqrt(pty.x*pty.x+pty.y*pty.y);
        float scaledFontSizeY = (float)(fontSize * scaleFactorY);
        Point2D.Double ptx = new Point2D.Double(1.0, 0.0);
        fontTransform.deltaTransform(ptx, ptx);
        double scaleFactorX = Math.sqrt(ptx.x*ptx.x+ptx.y*ptx.y);
        float scaledFontSizeX = (float)(fontSize * scaleFactorX);
        float awScale = getAwScale(scaleFactorX, scaleFactorY);
        int iangle = getAngle(ptx);
        Font2D font2D = FontUtilities.getFont2D(font);
        if (font2D instanceof TrueTypeFont) {
            textOut(str, font, (TrueTypeFont)font2D, frc,
                    scaledFontSizeY, iangle, awScale,
                    deviceTransform, scaleFactorX,
                    x, y, devpos.x, devpos.y, targetW);
        } else if (font2D instanceof CompositeFont) {
            CompositeFont compFont = (CompositeFont)font2D;
            float userx = x, usery = y;
            float devx = devpos.x, devy = devpos.y;
            char[] chars = str.toCharArray();
            int len = chars.length;
            int[] glyphs = new int[len];
            compFont.getMapper().charsToGlyphs(len, chars, glyphs);
            int startChar = 0, endChar = 0, slot = 0;
            while (endChar < len) {
                startChar = endChar;
                slot = glyphs[startChar] >>> 24;
                while (endChar < len && ((glyphs[endChar] >>> 24) == slot)) {
                    endChar++;
                }
                String substr = new String(chars, startChar,endChar-startChar);
                PhysicalFont slotFont = compFont.getSlotFont(slot);
                textOut(substr, font, slotFont, frc,
                        scaledFontSizeY, iangle, awScale,
                        deviceTransform, scaleFactorX,
                        userx, usery, devx, devy, 0f);
                Rectangle2D bds = font.getStringBounds(substr, frc);
                float xAdvance = (float)bds.getWidth();
                userx += xAdvance;
                userpos.x += xAdvance;
                deviceTransform.transform(userpos, devpos);
            }
        } else {
            super.drawString(str, x, y, font, frc, targetW);
        }
    }
    @Override
    protected boolean printGlyphVector(GlyphVector gv, float x, float y) {
        if ((gv.getLayoutFlags() & GlyphVector.FLAG_HAS_TRANSFORMS) != 0) {
            return false;
        }
        AffineTransform deviceTransform = getTransform();
        AffineTransform fontTransform = new AffineTransform(deviceTransform);
        Font font = gv.getFont();
        fontTransform.concatenate(font.getTransform());
        int transformType = fontTransform.getType();
        boolean directToGDI =
            ((transformType != AffineTransform.TYPE_GENERAL_TRANSFORM) &&
             ((transformType & AffineTransform.TYPE_FLIP) == 0));
        WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
        try {
            wPrinterJob.setTextColor((Color)getPaint());
        } catch (ClassCastException e) { 
            directToGDI = false;
        }
        if (WPrinterJob.shapeTextProp || !directToGDI) {
            return false;
        }
        Point2D.Float userpos = new Point2D.Float(x, y);
        Point2D.Float devpos = new Point2D.Float();
        if (font.isTransformed()) {
            AffineTransform fontTx = font.getTransform();
            float translateX = (float)(fontTx.getTranslateX());
            float translateY = (float)(fontTx.getTranslateY());
            if (Math.abs(translateX) < 0.00001) translateX = 0f;
            if (Math.abs(translateY) < 0.00001) translateY = 0f;
            userpos.x += translateX; userpos.y += translateY;
        }
        deviceTransform.transform(userpos, devpos);
        if (getClip() != null) {
            deviceClip(getClip().getPathIterator(deviceTransform));
        }
        float fontSize = font.getSize2D();
        Point2D.Double pty = new Point2D.Double(0.0, 1.0);
        fontTransform.deltaTransform(pty, pty);
        double scaleFactorY = Math.sqrt(pty.x*pty.x+pty.y*pty.y);
        float scaledFontSizeY = (float)(fontSize * scaleFactorY);
        Point2D.Double pt = new Point2D.Double(1.0, 0.0);
        fontTransform.deltaTransform(pt, pt);
        double scaleFactorX = Math.sqrt(pt.x*pt.x+pt.y*pt.y);
        float scaledFontSizeX = (float)(fontSize * scaleFactorX);
        float awScale = getAwScale(scaleFactorX, scaleFactorY);
        int iangle = getAngle(pt);
        int numGlyphs = gv.getNumGlyphs();
        int[] glyphCodes = gv.getGlyphCodes(0, numGlyphs, null);
        float[] glyphPos = gv.getGlyphPositions(0, numGlyphs, null);
        int invisibleGlyphCnt = 0;
        for (int gc=0; gc<numGlyphs; gc++) {
            if ((glyphCodes[gc] & 0xffff) >=
                CharToGlyphMapper.INVISIBLE_GLYPHS) {
                invisibleGlyphCnt++;
            }
        }
        if (invisibleGlyphCnt > 0) {
            int visibleGlyphCnt = numGlyphs - invisibleGlyphCnt;
            int[] visibleGlyphCodes = new int[visibleGlyphCnt];
            float[] visiblePositions = new float[visibleGlyphCnt*2];
            int index = 0;
            for (int i=0; i<numGlyphs; i++) {
                if ((glyphCodes[i] & 0xffff)
                    < CharToGlyphMapper.INVISIBLE_GLYPHS) {
                    visibleGlyphCodes[index] = glyphCodes[i];
                    visiblePositions[index*2]   = glyphPos[i*2];
                    visiblePositions[index*2+1] = glyphPos[i*2+1];
                    index++;
                }
            }
            numGlyphs = visibleGlyphCnt;
            glyphCodes = visibleGlyphCodes;
            glyphPos = visiblePositions;
        }
        AffineTransform advanceTransform =
            new AffineTransform(deviceTransform);
        advanceTransform.rotate(iangle*Math.PI/1800.0);
        float[] glyphAdvPos = new float[glyphPos.length];
        advanceTransform.transform(glyphPos, 0,         
                                   glyphAdvPos, 0,      
                                   glyphPos.length/2);  
        Font2D font2D = FontUtilities.getFont2D(font);
        if (font2D instanceof TrueTypeFont) {
            String family = font2D.getFamilyName(null);
            int style = font.getStyle() | font2D.getStyle();
            if (!wPrinterJob.setFont(family, scaledFontSizeY, style,
                                     iangle, awScale)) {
                return false;
            }
            wPrinterJob.glyphsOut(glyphCodes, devpos.x, devpos.y, glyphAdvPos);
        } else if (font2D instanceof CompositeFont) {
            CompositeFont compFont = (CompositeFont)font2D;
            float userx = x, usery = y;
            float devx = devpos.x, devy = devpos.y;
            int start = 0, end = 0, slot = 0;
            while (end < numGlyphs) {
                start = end;
                slot = glyphCodes[start] >>> 24;
                while (end < numGlyphs && ((glyphCodes[end] >>> 24) == slot)) {
                    end++;
                }
                PhysicalFont slotFont = compFont.getSlotFont(slot);
                if (!(slotFont instanceof TrueTypeFont)) {
                    return false;
                }
                String family = slotFont.getFamilyName(null);
                int style = font.getStyle() | slotFont.getStyle();
                if (!wPrinterJob.setFont(family, scaledFontSizeY, style,
                                         iangle, awScale)) {
                    return false;
                }
                int[] glyphs = Arrays.copyOfRange(glyphCodes, start, end);
                float[] posns = Arrays.copyOfRange(glyphAdvPos,
                                                   start*2, end*2);
                if (start != 0) {
                    Point2D.Float p =
                        new Point2D.Float(x+glyphPos[start*2],
                                          y+glyphPos[start*2+1]);
                    deviceTransform.transform(p, p);
                    devx = p.x;
                    devy = p.y;
                }
                wPrinterJob.glyphsOut(glyphs, devx, devy, posns);
            }
        } else {
            return false;
        }
        return true;
    }
    private void textOut(String str,
                          Font font, PhysicalFont font2D,
                          FontRenderContext frc,
                          float deviceSize, int rotation, float awScale,
                          AffineTransform deviceTransform,
                          double scaleFactorX,
                          float userx, float usery,
                          float devx, float devy, float targetW) {
         String family = font2D.getFamilyName(null);
         int style = font.getStyle() | font2D.getStyle();
         WPrinterJob wPrinterJob = (WPrinterJob)getPrinterJob();
         boolean setFont = wPrinterJob.setFont(family, deviceSize, style,
                                               rotation, awScale);
         if (!setFont) {
             super.drawString(str, userx, usery, font, frc, targetW);
             return;
         }
         float[] glyphPos = null;
         if (!okGDIMetrics(str, font, frc, scaleFactorX)) {
             str = wPrinterJob.removeControlChars(str);
             char[] chars = str.toCharArray();
             int len = chars.length;
             GlyphVector gv = null;
             if (!FontUtilities.isComplexText(chars, 0, len)) {
                 gv = font.createGlyphVector(frc, str);
             }
             if (gv == null) {
                 super.drawString(str, userx, usery, font, frc, targetW);
                 return;
             }
             glyphPos = gv.getGlyphPositions(0, len, null);
             Point2D gvAdvPt = gv.getGlyphPosition(gv.getNumGlyphs());
             AffineTransform advanceTransform =
               new AffineTransform(deviceTransform);
             advanceTransform.rotate(rotation*Math.PI/1800.0);
             float[] glyphAdvPos = new float[glyphPos.length];
             advanceTransform.transform(glyphPos, 0,         
                                        glyphAdvPos, 0,      
                                        glyphPos.length/2);  
             glyphPos = glyphAdvPos;
         }
         wPrinterJob.textOut(str, devx, devy, glyphPos);
     }
     private boolean okGDIMetrics(String str, Font font,
                                  FontRenderContext frc, double scaleX) {
         Rectangle2D bds = font.getStringBounds(str, frc);
         double jdkAdvance = bds.getWidth();
         jdkAdvance = Math.round(jdkAdvance*scaleX);
         int gdiAdvance = ((WPrinterJob)getPrinterJob()).getGDIAdvance(str);
         if (jdkAdvance > 0 && gdiAdvance > 0) {
             double diff = Math.abs(gdiAdvance-jdkAdvance);
             double ratio = gdiAdvance/jdkAdvance;
             if (ratio < 1) {
                 ratio = 1/ratio;
             }
             return diff <= 1 || ratio < 1.002;
         }
         return true;
     }
    protected boolean drawImageToPlatform(Image image, AffineTransform xform,
                                          Color bgcolor,
                                          int srcX, int srcY,
                                          int srcWidth, int srcHeight,
                                          boolean handlingTransparency) {
        BufferedImage img = getBufferedImage(image);
        if (img == null) {
            return true;
        }
        WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
        AffineTransform fullTransform = getTransform();
        if (xform == null) {
            xform = new AffineTransform();
        }
        fullTransform.concatenate(xform);
        double[] fullMatrix = new double[6];
        fullTransform.getMatrix(fullMatrix);
        Point2D.Float unitVectorX = new Point2D.Float(1, 0);
        Point2D.Float unitVectorY = new Point2D.Float(0, 1);
        fullTransform.deltaTransform(unitVectorX, unitVectorX);
        fullTransform.deltaTransform(unitVectorY, unitVectorY);
        Point2D.Float origin = new Point2D.Float(0, 0);
        double scaleX = unitVectorX.distance(origin);
        double scaleY = unitVectorY.distance(origin);
        double devResX = wPrinterJob.getXRes();
        double devResY = wPrinterJob.getYRes();
        double devScaleX = devResX / DEFAULT_USER_RES;
        double devScaleY = devResY / DEFAULT_USER_RES;
        int transformType = fullTransform.getType();
        boolean clampScale = ((transformType &
                               (AffineTransform.TYPE_GENERAL_ROTATION |
                                AffineTransform.TYPE_GENERAL_TRANSFORM)) != 0);
        if (clampScale) {
            if (scaleX > devScaleX) scaleX = devScaleX;
            if (scaleY > devScaleY) scaleY = devScaleY;
        }
        if (scaleX != 0 && scaleY != 0) {
            AffineTransform rotTransform = new AffineTransform(
                                        fullMatrix[0] / scaleX,  
                                        fullMatrix[1] / scaleY,  
                                        fullMatrix[2] / scaleX,  
                                        fullMatrix[3] / scaleY,  
                                        fullMatrix[4] / scaleX,  
                                        fullMatrix[5] / scaleY); 
            Rectangle2D.Float srcRect = new Rectangle2D.Float(srcX, srcY,
                                                              srcWidth,
                                                              srcHeight);
            Shape rotShape = rotTransform.createTransformedShape(srcRect);
            Rectangle2D rotBounds = rotShape.getBounds2D();
            rotBounds.setRect(rotBounds.getX(), rotBounds.getY(),
                              rotBounds.getWidth()+0.001,
                              rotBounds.getHeight()+0.001);
            int boundsWidth = (int) rotBounds.getWidth();
            int boundsHeight = (int) rotBounds.getHeight();
            if (boundsWidth > 0 && boundsHeight > 0) {
                boolean drawOpaque = true;
                if (!handlingTransparency && hasTransparentPixels(img)) {
                    drawOpaque = false;
                    if (isBitmaskTransparency(img)) {
                        if (bgcolor == null) {
                            if (drawBitmaskImage(img, xform, bgcolor,
                                                 srcX, srcY,
                                                 srcWidth, srcHeight)) {
                                return true;
                            }
                        } else if (bgcolor.getTransparency()
                                   == Transparency.OPAQUE) {
                            drawOpaque = true;
                        }
                    }
                    if (!canDoRedraws()) {
                        drawOpaque = true;
                    }
                } else {
                    bgcolor = null;
                }
                if ((srcX+srcWidth > img.getWidth(null) ||
                     srcY+srcHeight > img.getHeight(null))
                    && canDoRedraws()) {
                    drawOpaque = false;
                }
                if (drawOpaque == false) {
                    fullTransform.getMatrix(fullMatrix);
                    AffineTransform tx =
                        new AffineTransform(
                                            fullMatrix[0] / devScaleX,  
                                            fullMatrix[1] / devScaleY,  
                                            fullMatrix[2] / devScaleX,  
                                            fullMatrix[3] / devScaleY,  
                                            fullMatrix[4] / devScaleX,  
                                            fullMatrix[5] / devScaleY); 
                    Rectangle2D.Float rect =
                        new Rectangle2D.Float(srcX, srcY, srcWidth, srcHeight);
                    Shape shape = fullTransform.createTransformedShape(rect);
                    Rectangle2D region = shape.getBounds2D();
                    region.setRect(region.getX(), region.getY(),
                                   region.getWidth()+0.001,
                                   region.getHeight()+0.001);
                    int w = (int)region.getWidth();
                    int h = (int)region.getHeight();
                    int nbytes = w * h * 3;
                    int maxBytes = 8 * 1024 * 1024;
                    double origDpi = (devResX < devResY) ? devResX : devResY;
                    int dpi = (int)origDpi;
                    double scaleFactor = 1;
                    double maxSFX = w/(double)boundsWidth;
                    double maxSFY = h/(double)boundsHeight;
                    double maxSF = (maxSFX > maxSFY) ? maxSFY : maxSFX;
                    int minDpi = (int)(dpi/maxSF);
                    if (minDpi < DEFAULT_USER_RES) minDpi = DEFAULT_USER_RES;
                    while (nbytes > maxBytes && dpi > minDpi) {
                        scaleFactor *= 2;
                        dpi /= 2;
                        nbytes /= 4;
                    }
                    if (dpi < minDpi) {
                        scaleFactor = (origDpi / minDpi);
                    }
                    region.setRect(region.getX()/scaleFactor,
                                   region.getY()/scaleFactor,
                                   region.getWidth()/scaleFactor,
                                   region.getHeight()/scaleFactor);
                    wPrinterJob.saveState(getTransform(), getClip(),
                                          region, scaleFactor, scaleFactor);
                    return true;
                } else {
                    int dibType = BufferedImage.TYPE_3BYTE_BGR;
                    IndexColorModel icm = null;
                    ColorModel cm = img.getColorModel();
                    int imgType = img.getType();
                    if (cm instanceof IndexColorModel &&
                        cm.getPixelSize() <= 8 &&
                        (imgType == BufferedImage.TYPE_BYTE_BINARY ||
                         imgType == BufferedImage.TYPE_BYTE_INDEXED)) {
                        icm = (IndexColorModel)cm;
                        dibType = imgType;
                        if (imgType == BufferedImage.TYPE_BYTE_BINARY &&
                            cm.getPixelSize() == 2) {
                            int[] rgbs = new int[16];
                            icm.getRGBs(rgbs);
                            boolean transparent =
                                icm.getTransparency() != Transparency.OPAQUE;
                            int transpixel = icm.getTransparentPixel();
                            icm = new IndexColorModel(4, 16,
                                                      rgbs, 0,
                                                      transparent, transpixel,
                                                      DataBuffer.TYPE_BYTE);
                        }
                    }
                    int iw = (int)rotBounds.getWidth();
                    int ih = (int)rotBounds.getHeight();
                    BufferedImage deepImage = null;
                    boolean newImage = true;
                    if (newImage) {
                        if (icm == null) {
                            deepImage = new BufferedImage(iw, ih, dibType);
                        } else {
                            deepImage = new BufferedImage(iw, ih, dibType,icm);
                        }
                        Graphics2D imageGraphics = deepImage.createGraphics();
                        imageGraphics.clipRect(0, 0,
                                               deepImage.getWidth(),
                                               deepImage.getHeight());
                        imageGraphics.translate(-rotBounds.getX(),
                                                -rotBounds.getY());
                        imageGraphics.transform(rotTransform);
                        if (bgcolor == null) {
                            bgcolor = Color.white;
                        }
                        imageGraphics.drawImage(img,
                                                srcX, srcY,
                                                srcX + srcWidth,
                                                srcY + srcHeight,
                                                srcX, srcY,
                                                srcX + srcWidth,
                                                srcY + srcHeight,
                                                bgcolor, null);
                        imageGraphics.dispose();
                    } else {
                        deepImage = img;
                    }
                    Rectangle2D.Float scaledBounds
                            = new Rectangle2D.Float(
                                    (float) (rotBounds.getX() * scaleX),
                                    (float) (rotBounds.getY() * scaleY),
                                    (float) (rotBounds.getWidth() * scaleX),
                                    (float) (rotBounds.getHeight() * scaleY));
                    WritableRaster raster = deepImage.getRaster();
                    byte[] data;
                    if (raster instanceof ByteComponentRaster) {
                        data = ((ByteComponentRaster)raster).getDataStorage();
                    } else if (raster instanceof BytePackedRaster) {
                        data = ((BytePackedRaster)raster).getDataStorage();
                    } else {
                        return false;
                    }
                    int bitsPerPixel = 24;
                    SampleModel sm = deepImage.getSampleModel();
                    if (sm instanceof ComponentSampleModel) {
                        ComponentSampleModel csm = (ComponentSampleModel)sm;
                        bitsPerPixel = csm.getPixelStride() * 8;
                    } else if (sm instanceof MultiPixelPackedSampleModel) {
                        MultiPixelPackedSampleModel mppsm =
                            (MultiPixelPackedSampleModel)sm;
                        bitsPerPixel = mppsm.getPixelBitStride();
                    } else {
                        if (icm != null) {
                            int diw = deepImage.getWidth();
                            int dih = deepImage.getHeight();
                            if (diw > 0 && dih > 0) {
                                bitsPerPixel = data.length*8/diw/dih;
                            }
                        }
                    }
                    Shape holdClip = getClip();
                    clip(xform.createTransformedShape(srcRect));
                    deviceClip(getClip().getPathIterator(getTransform()));
                    wPrinterJob.drawDIBImage
                        (data, scaledBounds.x, scaledBounds.y,
                         (float)Math.rint(scaledBounds.width+0.5),
                         (float)Math.rint(scaledBounds.height+0.5),
                         0f, 0f,
                         deepImage.getWidth(), deepImage.getHeight(),
                         bitsPerPixel, icm);
                    setClip(holdClip);
                }
            }
        }
        return true;
    }
    public void redrawRegion(Rectangle2D region, double scaleX, double scaleY,
                             Shape savedClip, AffineTransform savedTransform)
            throws PrinterException {
        WPrinterJob wPrinterJob = (WPrinterJob)getPrinterJob();
        Printable painter = getPrintable();
        PageFormat pageFormat = getPageFormat();
        int pageIndex = getPageIndex();
        BufferedImage deepImage = new BufferedImage(
                                        (int) region.getWidth(),
                                        (int) region.getHeight(),
                                        BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = deepImage.createGraphics();
        ProxyGraphics2D proxy = new ProxyGraphics2D(g, wPrinterJob);
        proxy.setColor(Color.white);
        proxy.fillRect(0, 0, deepImage.getWidth(), deepImage.getHeight());
        proxy.clipRect(0, 0, deepImage.getWidth(), deepImage.getHeight());
        proxy.translate(-region.getX(), -region.getY());
        float sourceResX = (float)(wPrinterJob.getXRes() / scaleX);
        float sourceResY = (float)(wPrinterJob.getYRes() / scaleY);
        proxy.scale(sourceResX / DEFAULT_USER_RES,
                    sourceResY / DEFAULT_USER_RES);
        proxy.translate(
            -wPrinterJob.getPhysicalPrintableX(pageFormat.getPaper())
               / wPrinterJob.getXRes() * DEFAULT_USER_RES,
            -wPrinterJob.getPhysicalPrintableY(pageFormat.getPaper())
               / wPrinterJob.getYRes() * DEFAULT_USER_RES);
        proxy.transform(new AffineTransform(getPageFormat().getMatrix()));
        proxy.setPaint(Color.black);
        painter.print(proxy, pageFormat, pageIndex);
        g.dispose();
        deviceClip(savedClip.getPathIterator(savedTransform));
        Rectangle2D.Float scaledBounds
                = new Rectangle2D.Float(
                        (float) (region.getX() * scaleX),
                        (float) (region.getY() * scaleY),
                        (float) (region.getWidth() * scaleX),
                        (float) (region.getHeight() * scaleY));
       ByteComponentRaster tile
                = (ByteComponentRaster)deepImage.getRaster();
        wPrinterJob.drawImage3ByteBGR(tile.getDataStorage(),
                    scaledBounds.x, scaledBounds.y,
                    scaledBounds.width,
                    scaledBounds.height,
                    0f, 0f,
                    deepImage.getWidth(), deepImage.getHeight());
    }
    protected void deviceFill(PathIterator pathIter, Color color) {
        WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
        convertToWPath(pathIter);
        wPrinterJob.selectSolidBrush(color);
        wPrinterJob.fillPath();
    }
    protected void deviceClip(PathIterator pathIter) {
        WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
        convertToWPath(pathIter);
        wPrinterJob.selectClipPath();
    }
     protected void deviceFrameRect(int x, int y, int width, int height,
                                     Color color) {
        AffineTransform deviceTransform = getTransform();
        int transformType = deviceTransform.getType();
        boolean usePath = ((transformType &
                           (AffineTransform.TYPE_GENERAL_ROTATION |
                            AffineTransform.TYPE_GENERAL_TRANSFORM)) != 0);
        if (usePath) {
            draw(new Rectangle2D.Float(x, y, width, height));
            return;
        }
        Stroke stroke = getStroke();
        if (stroke instanceof BasicStroke) {
            BasicStroke lineStroke = (BasicStroke) stroke;
            int endCap = lineStroke.getEndCap();
            int lineJoin = lineStroke.getLineJoin();
            if ((endCap == BasicStroke.CAP_SQUARE) &&
                (lineJoin == BasicStroke.JOIN_MITER) &&
                (lineStroke.getMiterLimit() ==10.0f)) {
                float lineWidth = lineStroke.getLineWidth();
                Point2D.Float penSize = new Point2D.Float(lineWidth,
                                                          lineWidth);
                deviceTransform.deltaTransform(penSize, penSize);
                float deviceLineWidth = Math.min(Math.abs(penSize.x),
                                                 Math.abs(penSize.y));
                Point2D.Float ul_pos = new Point2D.Float(x, y);
                deviceTransform.transform(ul_pos, ul_pos);
                Point2D.Float lr_pos = new Point2D.Float(x + width,
                                                         y + height);
                deviceTransform.transform(lr_pos, lr_pos);
                float w = (float) (lr_pos.getX() - ul_pos.getX());
                float h = (float)(lr_pos.getY() - ul_pos.getY());
                WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
                if (wPrinterJob.selectStylePen(endCap, lineJoin,
                                           deviceLineWidth, color) == true)  {
                    wPrinterJob.frameRect((float)ul_pos.getX(),
                                          (float)ul_pos.getY(), w, h);
                }
                else {
                    double lowerRes = Math.min(wPrinterJob.getXRes(),
                                               wPrinterJob.getYRes());
                    if ((deviceLineWidth/lowerRes) < MAX_THINLINE_INCHES) {
                        wPrinterJob.selectPen(deviceLineWidth, color);
                        wPrinterJob.frameRect((float)ul_pos.getX(),
                                              (float)ul_pos.getY(), w, h);
                    }
                    else {
                        draw(new Rectangle2D.Float(x, y, width, height));
                    }
                }
            }
            else {
                draw(new Rectangle2D.Float(x, y, width, height));
            }
        }
     }
    protected void deviceFillRect(int x, int y, int width, int height,
                                  Color color) {
        AffineTransform deviceTransform = getTransform();
        int transformType = deviceTransform.getType();
        boolean usePath =  ((transformType &
                               (AffineTransform.TYPE_GENERAL_ROTATION |
                                AffineTransform.TYPE_GENERAL_TRANSFORM)) != 0);
        if (usePath) {
            fill(new Rectangle2D.Float(x, y, width, height));
            return;
        }
        Point2D.Float tlc_pos = new Point2D.Float(x, y);
        deviceTransform.transform(tlc_pos, tlc_pos);
        Point2D.Float brc_pos = new Point2D.Float(x+width, y+height);
        deviceTransform.transform(brc_pos, brc_pos);
        float deviceWidth = (float) (brc_pos.getX() - tlc_pos.getX());
        float deviceHeight = (float)(brc_pos.getY() - tlc_pos.getY());
        WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
        wPrinterJob.fillRect((float)tlc_pos.getX(), (float)tlc_pos.getY(),
                             deviceWidth, deviceHeight, color);
    }
    protected void deviceDrawLine(int xBegin, int yBegin, int xEnd, int yEnd,
                                  Color color) {
        Stroke stroke = getStroke();
        if (stroke instanceof BasicStroke) {
            BasicStroke lineStroke = (BasicStroke) stroke;
            if (lineStroke.getDashArray() != null) {
                draw(new Line2D.Float(xBegin, yBegin, xEnd, yEnd));
                return;
            }
            float lineWidth = lineStroke.getLineWidth();
            Point2D.Float penSize = new Point2D.Float(lineWidth, lineWidth);
            AffineTransform deviceTransform = getTransform();
            deviceTransform.deltaTransform(penSize, penSize);
            float deviceLineWidth = Math.min(Math.abs(penSize.x),
                                             Math.abs(penSize.y));
            Point2D.Float begin_pos = new Point2D.Float(xBegin, yBegin);
            deviceTransform.transform(begin_pos, begin_pos);
            Point2D.Float end_pos = new Point2D.Float(xEnd, yEnd);
            deviceTransform.transform(end_pos, end_pos);
            int endCap = lineStroke.getEndCap();
            int lineJoin = lineStroke.getLineJoin();
            if ((end_pos.getX() == begin_pos.getX())
                && (end_pos.getY() == begin_pos.getY())) {
                endCap = BasicStroke.CAP_ROUND;
            }
            WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
            if (wPrinterJob.selectStylePen(endCap, lineJoin,
                                           deviceLineWidth, color)) {
                wPrinterJob.moveTo((float)begin_pos.getX(),
                                   (float)begin_pos.getY());
                wPrinterJob.lineTo((float)end_pos.getX(),
                                   (float)end_pos.getY());
            }
            else {
                double lowerRes = Math.min(wPrinterJob.getXRes(),
                                           wPrinterJob.getYRes());
                if ((endCap == BasicStroke.CAP_ROUND) ||
                 (((xBegin == xEnd) || (yBegin == yEnd)) &&
                 (deviceLineWidth/lowerRes < MAX_THINLINE_INCHES))) {
                    wPrinterJob.selectPen(deviceLineWidth, color);
                    wPrinterJob.moveTo((float)begin_pos.getX(),
                                       (float)begin_pos.getY());
                    wPrinterJob.lineTo((float)end_pos.getX(),
                                       (float)end_pos.getY());
                }
                else {
                    draw(new Line2D.Float(xBegin, yBegin, xEnd, yEnd));
                }
            }
        }
    }
    private void convertToWPath(PathIterator pathIter) {
        float[] segment = new float[6];
        int segmentType;
        WPrinterJob wPrinterJob = (WPrinterJob) getPrinterJob();
        int polyFillRule;
        if (pathIter.getWindingRule() == PathIterator.WIND_EVEN_ODD) {
            polyFillRule = WPrinterJob.POLYFILL_ALTERNATE;
        } else {
            polyFillRule = WPrinterJob.POLYFILL_WINDING;
        }
        wPrinterJob.setPolyFillMode(polyFillRule);
        wPrinterJob.beginPath();
        while (pathIter.isDone() == false) {
            segmentType = pathIter.currentSegment(segment);
            switch (segmentType) {
             case PathIterator.SEG_MOVETO:
                wPrinterJob.moveTo(segment[0], segment[1]);
                break;
             case PathIterator.SEG_LINETO:
                wPrinterJob.lineTo(segment[0], segment[1]);
                break;
             case PathIterator.SEG_QUADTO:
                int lastX = wPrinterJob.getPenX();
                int lastY = wPrinterJob.getPenY();
                float c1x = lastX + (segment[0] - lastX) * 2 / 3;
                float c1y = lastY + (segment[1] - lastY) * 2 / 3;
                float c2x = segment[2] - (segment[2] - segment[0]) * 2/ 3;
                float c2y = segment[3] - (segment[3] - segment[1]) * 2/ 3;
                wPrinterJob.polyBezierTo(c1x, c1y,
                                         c2x, c2y,
                                         segment[2], segment[3]);
                break;
             case PathIterator.SEG_CUBICTO:
                wPrinterJob.polyBezierTo(segment[0], segment[1],
                                         segment[2], segment[3],
                                         segment[4], segment[5]);
                break;
             case PathIterator.SEG_CLOSE:
                wPrinterJob.closeFigure();
                break;
            }
            pathIter.next();
        }
        wPrinterJob.endPath();
    }
}

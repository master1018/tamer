public class JavaTextRenderer extends TextRenderer {
    public static final JavaTextRenderer inst = new JavaTextRenderer();
    @Override
    public void drawGlyphVector(Graphics2D g, GlyphVector glyphVector,
            float x, float y) {
        AffineTransform at = g.getTransform();
        Rectangle c = g.getClipBounds();
        if (at != null){
            int atType = at.getType();
            if (atType == AffineTransform.TYPE_TRANSLATION) {
                c.translate((int)Math.round(at.getTranslateX()), (int)Math.round(at.getTranslateY()));
            }
        }
        WritableRaster wr = ((BufferedImageGraphics2D)g).getWritableRaster();
        ColorModel cm = ((BufferedImageGraphics2D)g).getColorModel();
        Rectangle rBounds = wr.getBounds();
        Object color = cm.getDataElements(g.getColor().getRGB(), null);
        drawClipGlyphVector(wr, color, glyphVector, (int)Math.round(x + at.getTranslateX()), (int)Math.round(y + at.getTranslateY()),
        Math.max(c.x,rBounds.x),
        Math.max(c.y,rBounds.y),
        Math.min((int)Math.round(c.getMaxX()), (int)Math.round(rBounds.getMaxX())),
        Math.min((int)Math.round(c.getMaxY()), (int)Math.round(rBounds.getMaxY())));
    }
    @SuppressWarnings("deprecation")
    @Override
    public void drawString(Graphics2D g, String str, float x, float y) {
        AffineTransform at = g.getTransform();
        Rectangle c = g.getClipBounds();
        if (at != null){
            int atType = at.getType();
            if (atType == AffineTransform.TYPE_TRANSLATION) {
                c.translate((int)Math.round(at.getTranslateX()), (int)Math.round(at.getTranslateY()));
            }
        }
        WritableRaster wr = ((BufferedImageGraphics2D)g).getWritableRaster();
        ColorModel cm = ((BufferedImageGraphics2D)g).getColorModel();
        Rectangle rBounds = wr.getBounds();
        Object color = cm.getDataElements(g.getColor().getRGB(), null);
        drawClipString(wr, color, str, (FontPeerImpl) (g.getFont().getPeer()),
                (int)Math.round(x + at.getTranslateX()), (int)Math.round(y + at.getTranslateY()),
                Math.max(c.x,rBounds.x),
                Math.max(c.y,rBounds.y),
                Math.min((int)Math.round(c.getMaxX()), (int)Math.round(rBounds.getMaxX())),
                Math.min((int)Math.round(c.getMaxY()), (int)Math.round(rBounds.getMaxY())));
    }
    public void drawClipGlyphVector(WritableRaster raster, Object color,
            GlyphVector glyphVector, int x, int y,
            int cMinX, int cMinY, int cMaxX, int cMaxY) {
        int xSrcSurf, ySrcSurf; 
        int xDstSurf, yDstSurf; 
        int clWidth, clHeight;
        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Glyph gl = ((CommonGlyphVector) glyphVector).vector[i];
            if (gl.getPointWidth() == 0) {
                continue;
            }
            byte[] data = gl.getBitmap();
            if (data != null) {
                Point2D pos = glyphVector.getGlyphPosition(i);
                xSrcSurf = 0;
                ySrcSurf = 0;
                xDstSurf = x + (int)pos.getX() + (int) gl.getGlyphPointMetrics().getLSB();
                yDstSurf = y - gl.bmp_top  + (int) pos.getY();
                int textWidth = gl.bmp_width;
                int textHeight = gl.getPointHeight();
                if ((xDstSurf > cMaxX) || (yDstSurf > cMaxY) || (xDstSurf + textWidth < cMinX)
                        || (yDstSurf + textHeight < cMinY)) {
                } else {
                    if (xDstSurf >= cMinX) {
                        clWidth = Math.min(textWidth, cMaxX - xDstSurf);
                    } else {
                        xSrcSurf += cMinX - xDstSurf;
                        clWidth = Math.min(cMaxX - cMinX, textWidth - (cMinX - xDstSurf));
                        xDstSurf = cMinX;
                    }
                    if (yDstSurf >= cMinY) {
                        clHeight = Math.min(textHeight, cMaxY - yDstSurf);
                    } else {
                        ySrcSurf += cMinY - yDstSurf;
                        clHeight = Math.min(cMaxY - cMinY, textHeight - (cMinY - yDstSurf));
                        yDstSurf = cMinY;
                    }
                    for (int h=0; h<clHeight; h++){
                        for (int w=0; w < clWidth ; w++) {
                            byte currByte = data[(ySrcSurf + h)*gl.bmp_pitch + (xSrcSurf+w)/8];
                            boolean emptyByte = ((currByte & (1 << (7 - ((xSrcSurf+w) % 8)))) != 0);
                            if (emptyByte) {
                                raster.setDataElements(xDstSurf+w, yDstSurf+h, color);
                            } else {
                            }
                        }
                    }
                }
            }
        }
    }
    public void drawClipString(WritableRaster raster, Object color, String str,
            FontPeerImpl font, int x, int y, int cMinX, int cMinY, int cMaxX,
            int cMaxY) {
        int xSrcSurf, ySrcSurf; 
        int xDstSurf, yDstSurf; 
        int clWidth, clHeight;
        char[] chars = str.toCharArray();
        int xBaseLine = x;
        int yBaseLine = y;
        for (char element : chars) {
            Glyph gl = font.getGlyph(element);
            GlyphMetrics pointMetrics = gl.getGlyphPointMetrics();
            if (gl.getWidth() == 0) {
                xBaseLine += pointMetrics.getAdvanceX();
                continue;
            }
            byte[] data = gl.getBitmap();
            if (data == null) {
                xBaseLine += pointMetrics.getAdvanceX();
            } else {
                xSrcSurf = 0;
                ySrcSurf = 0;
                xDstSurf = Math.round(xBaseLine + gl.getGlyphPointMetrics().getLSB());
                yDstSurf = yBaseLine - gl.bmp_top;
                int textWidth = gl.bmp_width;
                int textHeight = gl.getPointHeight();
                if ((xDstSurf > cMaxX) || (yDstSurf > cMaxY) || (xDstSurf + textWidth < cMinX)
                        || (yDstSurf + textHeight < cMinY)) {
                } else {
                    if (xDstSurf >= cMinX) {
                        clWidth = Math.min(textWidth, cMaxX - xDstSurf);
                    } else {
                        xSrcSurf += cMinX - xDstSurf;
                        clWidth = Math.min(cMaxX - cMinX, textWidth - (cMinX - xDstSurf));
                        xDstSurf = cMinX;
                    }
                    if (yDstSurf >= cMinY) {
                        clHeight = Math.min(textHeight, cMaxY - yDstSurf);
                    } else {
                        ySrcSurf += cMinY - yDstSurf;
                        clHeight = Math.min(cMaxY - cMinY, textHeight - (cMinY - yDstSurf));
                        yDstSurf = cMinY;
                    }
                    for (int h=0; h<clHeight; h++){
                        for (int w=0; w < clWidth ; w++) {
                            byte currByte = data[(ySrcSurf + h)*gl.bmp_pitch + (xSrcSurf+w)/8];
                            boolean emptyByte = ((currByte & (1 << (7 - ((xSrcSurf+w) % 8)))) != 0);
                            if (emptyByte) {
                                raster.setDataElements(xDstSurf+w, yDstSurf+h, color);
                            } else {
                            }
                        }
                    }
                }
                xBaseLine += pointMetrics.getAdvanceX();
            }
        }
    }
}
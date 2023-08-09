public class ProxyGraphics2D extends Graphics2D implements PrinterGraphics {
    Graphics2D mGraphics;
    PrinterJob mPrinterJob;
    public ProxyGraphics2D(Graphics2D graphics, PrinterJob printerJob) {
        mGraphics = graphics;
        mPrinterJob = printerJob;
    }
    public Graphics2D getDelegate() {
        return mGraphics;
    }
    public void setDelegate(Graphics2D graphics) {
        mGraphics = graphics;
    }
    public PrinterJob getPrinterJob() {
        return mPrinterJob;
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return ((RasterPrinterJob)mPrinterJob).getPrinterGraphicsConfig();
    }
    public Graphics create() {
        return new ProxyGraphics2D((Graphics2D) mGraphics.create(),
                                   mPrinterJob);
    }
    public void translate(int x, int y) {
        mGraphics.translate(x, y);
    }
    public void translate(double tx, double ty) {
        mGraphics.translate(tx, ty);
    }
    public void rotate(double theta) {
        mGraphics.rotate(theta);
    }
    public void rotate(double theta, double x, double y) {
        mGraphics.rotate(theta, x, y);
    }
    public void scale(double sx, double sy) {
        mGraphics.scale(sx, sy);
    }
    public void shear(double shx, double shy) {
        mGraphics.shear(shx, shy);
    }
    public Color getColor() {
        return mGraphics.getColor();
    }
    public void setColor(Color c) {
        mGraphics.setColor(c);
    }
    public void setPaintMode() {
        mGraphics.setPaintMode();
    }
    public void setXORMode(Color c1) {
        mGraphics.setXORMode(c1);
    }
    public Font getFont() {
        return mGraphics.getFont();
    }
    public void setFont(Font font) {
        mGraphics.setFont(font);
    }
    public FontMetrics getFontMetrics(Font f) {
        return mGraphics.getFontMetrics(f);
    }
    public FontRenderContext getFontRenderContext() {
        return mGraphics.getFontRenderContext();
    }
    public Rectangle getClipBounds() {
        return mGraphics.getClipBounds();
    }
    public void clipRect(int x, int y, int width, int height) {
        mGraphics.clipRect(x, y, width, height);
    }
    public void setClip(int x, int y, int width, int height) {
        mGraphics.setClip(x, y, width, height);
    }
    public Shape getClip() {
        return mGraphics.getClip();
    }
    public void setClip(Shape clip) {
        mGraphics.setClip(clip);
    }
    public void copyArea(int x, int y, int width, int height,
                         int dx, int dy) {
        mGraphics.copyArea(x, y, width, height, dx, dy);
    }
    public void drawLine(int x1, int y1, int x2, int y2) {
        mGraphics.drawLine(x1, y1, x2, y2);
    }
    public void fillRect(int x, int y, int width, int height) {
        mGraphics.fillRect(x, y, width, height);
    }
    public void clearRect(int x, int y, int width, int height) {
        mGraphics.clearRect(x, y, width, height);
    }
    public void drawRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        mGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }
    public void fillRoundRect(int x, int y, int width, int height,
                                       int arcWidth, int arcHeight) {
        mGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }
    public void drawOval(int x, int y, int width, int height) {
        mGraphics.drawOval(x, y, width, height);
    }
    public void fillOval(int x, int y, int width, int height) {
        mGraphics.fillOval(x, y, width, height);
    }
    public void drawArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle) {
        mGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
    }
    public void fillArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
        mGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
    }
    public void drawPolyline(int xPoints[], int yPoints[],
                             int nPoints) {
        mGraphics.drawPolyline(xPoints, yPoints, nPoints);
    }
    public void drawPolygon(int xPoints[], int yPoints[],
                            int nPoints) {
        mGraphics.drawPolygon(xPoints, yPoints, nPoints);
    }
    public void fillPolygon(int xPoints[], int yPoints[],
                            int nPoints) {
        mGraphics.fillPolygon(xPoints, yPoints, nPoints);
    }
    public void drawString(String str, int x, int y) {
        mGraphics.drawString(str, x, y);
    }
    public void drawString(AttributedCharacterIterator iterator,
                                    int x, int y) {
        mGraphics.drawString(iterator, x, y);
    }
    public void drawString(AttributedCharacterIterator iterator,
                                    float x, float y) {
        mGraphics.drawString(iterator, x, y);
    }
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        return mGraphics.drawImage(img, x, y, observer);
    }
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             ImageObserver observer) {
        return mGraphics.drawImage(img, x, y, width, height, observer);
    }
    public boolean drawImage(Image img, int x, int y,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        boolean result;
        if (needToCopyBgColorImage(img)) {
            BufferedImage imageCopy = getBufferedImageCopy(img, bgcolor);
            result = mGraphics.drawImage(imageCopy, x, y, null);
        } else {
            result = mGraphics.drawImage(img, x, y, bgcolor, observer);
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
        if (needToCopyBgColorImage(img)) {
            BufferedImage imageCopy = getBufferedImageCopy(img, bgcolor);
            result = mGraphics.drawImage(imageCopy, x, y, width, height, null);
        } else {
            result = mGraphics.drawImage(img, x, y, width, height,
                                         bgcolor, observer);
        }
        return result;
    }
    public boolean drawImage(Image img,
                                      int dx1, int dy1, int dx2, int dy2,
                                      int sx1, int sy1, int sx2, int sy2,
                                      ImageObserver observer) {
        return mGraphics.drawImage(img, dx1, dy1, dx2, dy2,
                                   sx1, sy1, sx2, sy2,
                                   observer);
    }
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        boolean result;
        if (needToCopyBgColorImage(img)) {
            BufferedImage imageCopy = getBufferedImageCopy(img, bgcolor);
            result = mGraphics.drawImage(imageCopy,
                                         dx1, dy1, dx2, dy2,
                                         sy1, sy1, sx2, sy2,
                                         null);
        } else {
            result = mGraphics.drawImage(img,
                                         dx1, dy1, dx2, dy2,
                                         sy1, sy1, sx2, sy2,
                                         bgcolor,
                                         observer);
        }
        return result;
    }
    private boolean needToCopyBgColorImage(Image img) {
        boolean needToCopy;
        AffineTransform transform = getTransform();
        return (transform.getType()
                & (AffineTransform.TYPE_GENERAL_ROTATION
                   | AffineTransform.TYPE_GENERAL_TRANSFORM)) != 0;
    }
    private BufferedImage getBufferedImageCopy(Image img, Color bgcolor) {
        BufferedImage imageCopy = null;
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        if (width > 0 && height > 0) {
            int imageType;
            if (img instanceof BufferedImage) {
                BufferedImage bufImage = (BufferedImage) img;
                imageType = bufImage.getType();
            } else {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            imageCopy = new BufferedImage(width, height, imageType);
            Graphics g = imageCopy.createGraphics();
            g.drawImage(img, 0, 0, bgcolor, null);
            g.dispose();
        } else {
            imageCopy = null;
        }
        return imageCopy;
    }
    public void drawRenderedImage(RenderedImage img,
                                  AffineTransform xform) {
        mGraphics.drawRenderedImage(img, xform);
    }
    public void drawRenderableImage(RenderableImage img,
                                    AffineTransform xform) {
        if (img == null) {
            return;
        }
        AffineTransform pipeTransform = getTransform();
        AffineTransform concatTransform = new AffineTransform(xform);
        concatTransform.concatenate(pipeTransform);
        AffineTransform reverseTransform;
        RenderContext rc = new RenderContext(concatTransform);
        try {
            reverseTransform = pipeTransform.createInverse();
        } catch (NoninvertibleTransformException nte) {
            rc = new RenderContext(pipeTransform);
            reverseTransform = new AffineTransform();
        }
        RenderedImage rendering = img.createRendering(rc);
        drawRenderedImage(rendering,reverseTransform);
    }
    public void dispose() {
        mGraphics.dispose();
    }
    public void finalize() {
    }
    public void draw(Shape s) {
        mGraphics.draw(s);
    }
    public boolean drawImage(Image img,
                             AffineTransform xform,
                             ImageObserver obs) {
        return mGraphics.drawImage(img, xform, obs);
    }
    public void drawImage(BufferedImage img,
                          BufferedImageOp op,
                          int x,
                          int y) {
        mGraphics.drawImage(img, op, x, y);
    }
    public void drawString(String str,
                           float x,
                           float y) {
        mGraphics.drawString(str, x, y);
    }
    public void drawGlyphVector(GlyphVector g,
                                float x,
                                float y) {
        mGraphics.drawGlyphVector(g, x, y);
    }
    public void fill(Shape s) {
        mGraphics.fill(s);
    }
    public boolean hit(Rectangle rect,
                       Shape s,
                       boolean onStroke) {
        return mGraphics.hit(rect, s, onStroke);
    }
    public void setComposite(Composite comp) {
        mGraphics.setComposite(comp);
    }
    public void setPaint(Paint paint) {
        mGraphics.setPaint(paint);
    }
    public void setStroke(Stroke s) {
        mGraphics.setStroke(s);
    }
    public void setRenderingHint(Key hintCategory, Object hintValue) {
        mGraphics.setRenderingHint(hintCategory, hintValue);
    }
    public Object getRenderingHint(Key hintCategory) {
        return mGraphics.getRenderingHint(hintCategory);
    }
    public void setRenderingHints(Map<?,?> hints) {
        mGraphics.setRenderingHints(hints);
    }
    public void addRenderingHints(Map<?,?> hints) {
        mGraphics.addRenderingHints(hints);
    }
    public RenderingHints getRenderingHints() {
        return mGraphics.getRenderingHints();
    }
    public void transform(AffineTransform Tx) {
        mGraphics.transform(Tx);
    }
    public void setTransform(AffineTransform Tx) {
        mGraphics.setTransform(Tx);
    }
    public AffineTransform getTransform() {
        return mGraphics.getTransform();
    }
    public Paint getPaint() {
        return mGraphics.getPaint();
    }
    public Composite getComposite() {
        return mGraphics.getComposite();
    }
    public void setBackground(Color color) {
        mGraphics.setBackground(color);
    }
    public Color getBackground() {
        return mGraphics.getBackground();
    }
    public Stroke getStroke() {
        return mGraphics.getStroke();
    }
     public void clip(Shape s) {
        mGraphics.clip(s);
     }
}

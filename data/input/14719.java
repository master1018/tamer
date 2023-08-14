public class PeekGraphics extends Graphics2D
                          implements PrinterGraphics,
                                     ImageObserver,
                                     Cloneable {
    Graphics2D mGraphics;
    PrinterJob mPrinterJob;
    private Spans mDrawingArea = new Spans();
    private PeekMetrics mPrintMetrics = new PeekMetrics();
    private boolean mAWTDrawingOnly = false;
    public PeekGraphics(Graphics2D graphics, PrinterJob printerJob) {
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
    public void setAWTDrawingOnly() {
        mAWTDrawingOnly = true;
    }
    public boolean getAWTDrawingOnly() {
        return mAWTDrawingOnly;
    }
    public Spans getDrawingArea() {
        return mDrawingArea;
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return ((RasterPrinterJob)mPrinterJob).getPrinterGraphicsConfig();
    }
    public Graphics create() {
        PeekGraphics newGraphics = null;
        try {
            newGraphics = (PeekGraphics) clone();
            newGraphics.mGraphics = (Graphics2D) mGraphics.create();
        } catch (CloneNotSupportedException e) {
        }
        return newGraphics;
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
    }
    public void drawLine(int x1, int y1, int x2, int y2) {
        addStrokeShape(new Line2D.Float(x1, y1, x2, y2));
        mPrintMetrics.draw(this);
    }
    public void fillRect(int x, int y, int width, int height) {
        addDrawingRect(new Rectangle2D.Float(x, y, width, height));
        mPrintMetrics.fill(this);
    }
    public void clearRect(int x, int y, int width, int height) {
        Rectangle2D.Float rect = new Rectangle2D.Float(x, y, width, height);
        addDrawingRect(rect);
        mPrintMetrics.clear(this);
    }
    public void drawRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        addStrokeShape(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
        mPrintMetrics.draw(this);
    }
    public void fillRoundRect(int x, int y, int width, int height,
                                       int arcWidth, int arcHeight) {
        Rectangle2D.Float rect = new Rectangle2D.Float(x, y,width, height);
        addDrawingRect(rect);
        mPrintMetrics.fill(this);
    }
    public void drawOval(int x, int y, int width, int height) {
        addStrokeShape(new Rectangle2D.Float(x, y,  width, height));
        mPrintMetrics.draw(this);
    }
    public void fillOval(int x, int y, int width, int height) {
        Rectangle2D.Float rect = new Rectangle2D.Float(x, y, width, height);
        addDrawingRect(rect);
        mPrintMetrics.fill(this);
    }
    public void drawArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle) {
        addStrokeShape(new Rectangle2D.Float(x, y,  width, height));
        mPrintMetrics.draw(this);
    }
    public void fillArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
        Rectangle2D.Float rect = new Rectangle2D.Float(x, y,width, height);
        addDrawingRect(rect);
        mPrintMetrics.fill(this);
    }
   public void drawPolyline(int xPoints[], int yPoints[],
                             int nPoints) {
        if (nPoints > 0) {
            int x = xPoints[0];
            int y = yPoints[0];
            for (int i = 1; i < nPoints; i++) {
                drawLine(x, y, xPoints[i], yPoints[i]);
                x = xPoints[i];
                y = yPoints[i];
            }
        }
    }
    public void drawPolygon(int xPoints[], int yPoints[],
                            int nPoints) {
        if (nPoints > 0) {
            drawPolyline(xPoints, yPoints, nPoints);
            drawLine(xPoints[nPoints - 1], yPoints[nPoints - 1],
                     xPoints[0], yPoints[0]);
        }
    }
    public void fillPolygon(int xPoints[], int yPoints[],
                            int nPoints) {
        if (nPoints > 0) {
            int minX = xPoints[0];
            int minY = yPoints[0];
            int maxX = xPoints[0];
            int maxY = yPoints[0];
            for (int i = 1; i < nPoints; i++) {
                if (xPoints[i] < minX) {
                    minX = xPoints[i];
                } else if (xPoints[i] > maxX) {
                    maxX = xPoints[i];
                }
                if (yPoints[i] < minY) {
                    minY = yPoints[i];
                } else if (yPoints[i] > maxY) {
                    maxY = yPoints[i];
                }
            }
            addDrawingRect(minX, minY, maxX - minX, maxY - minY);
        }
        mPrintMetrics.fill(this);
    }
    public void drawString(String str, int x, int y) {
        drawString(str, (float)x, (float)y);
    }
    public void drawString(AttributedCharacterIterator iterator,
                                    int x, int y) {
        drawString(iterator,  (float)x, (float)y);
    }
    public void drawString(AttributedCharacterIterator iterator,
                                    float x, float y) {
        if (iterator == null) {
            throw new
                NullPointerException("AttributedCharacterIterator is null");
        }
        TextLayout layout = new TextLayout(iterator, getFontRenderContext());
        layout.draw(this, x, y);
    }
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        ImageWaiter dim = new ImageWaiter(img);
        addDrawingRect(x, y, dim.getWidth(), dim.getHeight());
        mPrintMetrics.drawImage(this, img);
        return mGraphics.drawImage(img, x, y, observer);
    }
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        addDrawingRect(x, y, width, height);
        mPrintMetrics.drawImage(this, img);
        return mGraphics.drawImage(img, x, y, width, height, observer);
    }
   public boolean drawImage(Image img, int x, int y,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        ImageWaiter dim = new ImageWaiter(img);
        addDrawingRect(x, y, dim.getWidth(), dim.getHeight());
        mPrintMetrics.drawImage(this, img);
        return mGraphics.drawImage(img, x, y, bgcolor, observer);
    }
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        addDrawingRect(x, y, width, height);
        mPrintMetrics.drawImage(this, img);
        return mGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
    }
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        int width = dx2 - dx1;
        int height = dy2 - dy1;
        addDrawingRect(dx1, dy1, width, height);
        mPrintMetrics.drawImage(this, img);
        return mGraphics.drawImage(img, dx1, dy1, dx2, dy2,
                               sx1, sy1, sx2, sy2, observer);
    }
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor,
                             ImageObserver observer) {
        if (img == null) {
            return true;
        }
        int width = dx2 - dx1;
        int height = dy2 - dy1;
        addDrawingRect(dx1, dy1, width, height);
        mPrintMetrics.drawImage(this, img);
        return mGraphics.drawImage(img, dx1, dy1, dx2, dy2,
                               sx1, sy1, sx2, sy2, bgcolor, observer);
    }
    public void drawRenderedImage(RenderedImage img,
                                  AffineTransform xform) {
        if (img == null) {
            return;
        }
        mPrintMetrics.drawImage(this, img);
        mDrawingArea.addInfinite();
    }
    public void drawRenderableImage(RenderableImage img,
                                    AffineTransform xform) {
        if (img == null) {
            return;
        }
        mPrintMetrics.drawImage(this, img);
        mDrawingArea.addInfinite();
    }
    public void dispose() {
        mGraphics.dispose();
    }
    public void finalize() {
    }
    public void draw(Shape s) {
        addStrokeShape(s);
        mPrintMetrics.draw(this);
    }
    public boolean drawImage(Image img,
                             AffineTransform xform,
                             ImageObserver obs) {
        if (img == null) {
            return true;
        }
        mDrawingArea.addInfinite();
        mPrintMetrics.drawImage(this, img);
        return mGraphics.drawImage(img, xform, obs);
    }
    public void drawImage(BufferedImage img,
                          BufferedImageOp op,
                          int x,
                          int y) {
        if (img == null) {
            return;
        }
        mPrintMetrics.drawImage(this, (RenderedImage) img);
        mDrawingArea.addInfinite();
    }
    public void drawString(String str,
                           float x,
                           float y) {
        if (str.length() == 0) {
            return;
        }
        FontRenderContext frc = getFontRenderContext();
        Rectangle2D bbox = getFont().getStringBounds(str, frc);
        addDrawingRect(bbox, x, y);
        mPrintMetrics.drawText(this);
    }
    public void drawGlyphVector(GlyphVector g,
                           float x,
                           float y) {
        Rectangle2D bbox = g.getLogicalBounds();
        addDrawingRect(bbox, x, y);
        mPrintMetrics.drawText(this);
    }
    public void fill(Shape s) {
        addDrawingRect(s.getBounds());
        mPrintMetrics.fill(this);
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
     public boolean hitsDrawingArea(Rectangle rect) {
         return mDrawingArea.intersects((float) rect.getMinY(),
                                        (float) rect.getMaxY());
     }
     public PeekMetrics getMetrics() {
        return mPrintMetrics;
     }
    private void addDrawingRect(Rectangle2D rect, float x, float y) {
        addDrawingRect((float) (rect.getX() + x),
                       (float) (rect.getY() + y),
                       (float) rect.getWidth(),
                       (float) rect.getHeight());
    }
    private void addDrawingRect(float x, float y, float width, float height) {
        Rectangle2D.Float bbox = new Rectangle2D.Float(x, y, width, height);
        addDrawingRect(bbox);
    }
    private void addDrawingRect(Rectangle2D rect) {
        AffineTransform matrix = getTransform();
        Shape transShape = matrix.createTransformedShape(rect);
        Rectangle2D transRect = transShape.getBounds2D();
        mDrawingArea.add((float) transRect.getMinY(),
                         (float) transRect.getMaxY());
    }
    private void addStrokeShape(Shape s) {
        Shape transShape = getStroke().createStrokedShape(s);
        addDrawingRect(transShape.getBounds2D());
    }
    public synchronized boolean imageUpdate(Image img, int infoFlags,
                                            int x, int y,
                                            int width, int height) {
        boolean gotInfo = false;
        if((infoFlags & (WIDTH | HEIGHT)) != 0) {
            gotInfo = true;
            notify();
        }
        return gotInfo;
    }
    private synchronized int getImageWidth(Image img) {
        while (img.getWidth(this) == -1) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        return img.getWidth(this);
    }
    private synchronized int getImageHeight(Image img) {
        while (img.getHeight(this) == -1) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        return img.getHeight(this);
    }
    protected class ImageWaiter implements ImageObserver {
        private int mWidth;
        private int mHeight;
        private boolean badImage = false;
        ImageWaiter(Image img) {
            waitForDimensions(img);
        }
        public int getWidth() {
            return mWidth;
        }
        public int getHeight() {
            return mHeight;
        }
        synchronized private void waitForDimensions(Image img) {
            mHeight = img.getHeight(this);
            mWidth = img.getWidth(this);
            while (!badImage && (mWidth < 0 || mHeight < 0)) {
                try {
                    Thread.sleep(50);
                } catch(InterruptedException e) {
                }
                mHeight = img.getHeight(this);
                mWidth = img.getWidth(this);
            }
            if (badImage) {
                mHeight = 0;
                mWidth = 0;
            }
        }
        synchronized public boolean imageUpdate(Image image, int flags,
                                                int x, int y, int w, int h) {
            boolean dontCallMeAgain = (flags & (HEIGHT | ABORT | ERROR)) != 0;
            badImage = (flags & (ABORT | ERROR)) != 0;
            return dontCallMeAgain;
        }
    }
}

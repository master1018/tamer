public class GCWrapper implements IGraphics {
    private GC mGc;
    private class ColorWrapper implements IColor {
        private final Color mColor;
        public ColorWrapper(Color color) {
            mColor = color;
        }
        public Color getColor() {
            return mColor;
        }
    }
    private final HashMap<Integer, ColorWrapper> mColorMap = new HashMap<Integer, ColorWrapper>();
    private int mFontHeight = 0;
    private final ScaleTransform mHScale;
    private final ScaleTransform mVScale;
    public GCWrapper(ScaleTransform hScale, ScaleTransform vScale) {
        mHScale = hScale;
        mVScale = vScale;
        mGc = null;
    }
    void setGC(GC gc) {
        mGc = gc;
    }
    private GC getGc() {
        return mGc;
    }
    void checkGC() {
        if (mGc == null) {
            throw new RuntimeException("IGraphics used without a valid context.");
        }
    }
    void dispose() {
        for (ColorWrapper c : mColorMap.values()) {
            c.getColor().dispose();
        }
        mColorMap.clear();
    }
    public IColor registerColor(int rgb) {
        checkGC();
        Integer key = Integer.valueOf(rgb);
        ColorWrapper c = mColorMap.get(key);
        if (c == null) {
            c = new ColorWrapper(new Color(getGc().getDevice(),
                    (rgb >> 16) & 0xFF,
                    (rgb >>  8) & 0xFF,
                    (rgb >>  0) & 0xFF));
            mColorMap.put(key, c);
        }
        return c;
    }
    public int getFontHeight() {
        if (mFontHeight < 1) {
            checkGC();
            FontMetrics fm = getGc().getFontMetrics();
            mFontHeight = fm.getHeight();
        }
        return mFontHeight;
    }
    public void setForeground(IColor color) {
        checkGC();
        getGc().setForeground(((ColorWrapper) color).getColor());
    }
    public void setBackground(IColor color) {
        checkGC();
        getGc().setBackground(((ColorWrapper) color).getColor());
    }
    public boolean setAlpha(int alpha) {
        checkGC();
        try {
            getGc().setAlpha(alpha);
            return true;
        } catch (SWTException e) {
            return false;
        }
    }
    public void setLineStyle(LineStyle style) {
        int swtStyle = 0;
        switch (style) {
        case LINE_SOLID:
            swtStyle = SWT.LINE_SOLID;
            break;
        case LINE_DASH:
            swtStyle = SWT.LINE_DASH;
            break;
        case LINE_DOT:
            swtStyle = SWT.LINE_DOT;
            break;
        case LINE_DASHDOT:
            swtStyle = SWT.LINE_DASHDOT;
            break;
        case LINE_DASHDOTDOT:
            swtStyle = SWT.LINE_DASHDOTDOT;
            break;
        }
        if (swtStyle != 0) {
            checkGC();
            getGc().setLineStyle(swtStyle);
        }
    }
    public void setLineWidth(int width) {
        checkGC();
        if (width > 0) {
            getGc().setLineWidth(width);
        }
    }
    public void drawLine(int x1, int y1, int x2, int y2) {
        checkGC();
        x1 = mHScale.translate(x1);
        y1 = mVScale.translate(y1);
        x2 = mHScale.translate(x2);
        y2 = mVScale.translate(y2);
        getGc().drawLine(x1, y1, x2, y2);
    }
    public void drawLine(Point p1, Point p2) {
        drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    public void drawRect(int x1, int y1, int x2, int y2) {
        checkGC();
        int x = mHScale.translate(x1);
        int y = mVScale.translate(y1);
        int w = mHScale.scale(x2 - x1);
        int h = mVScale.scale(y2 - y1);
        getGc().drawRectangle(x, y, w, h);
    }
    public void drawRect(Point p1, Point p2) {
        drawRect(p1.x, p1.y, p2.x, p2.y);
    }
    public void drawRect(Rect r) {
        checkGC();
        int x = mHScale.translate(r.x);
        int y = mVScale.translate(r.y);
        int w = mHScale.scale(r.w);
        int h = mVScale.scale(r.h);
        getGc().drawRectangle(x, y, w, h);
    }
    public void fillRect(int x1, int y1, int x2, int y2) {
        checkGC();
        int x = mHScale.translate(x1);
        int y = mVScale.translate(y1);
        int w = mHScale.scale(x2 - x1);
        int h = mVScale.scale(y2 - y1);
        getGc().fillRectangle(x, y, w, h);
    }
    public void fillRect(Point p1, Point p2) {
        fillRect(p1.x, p1.y, p2.x, p2.y);
    }
    public void fillRect(Rect r) {
        checkGC();
        int x = mHScale.translate(r.x);
        int y = mVScale.translate(r.y);
        int w = mHScale.scale(r.w);
        int h = mVScale.scale(r.h);
        getGc().fillRectangle(x, y, w, h);
    }
    public void drawString(String string, int x, int y) {
        checkGC();
        x = mHScale.translate(x);
        y = mVScale.translate(y);
        getGc().drawString(string, x, y, true );
    }
    public void drawString(String string, Point topLeft) {
        drawString(string, topLeft.x, topLeft.y);
    }
}

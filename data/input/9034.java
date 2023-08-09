public final class ShapeSpanIterator
    implements SpanIterator, PathConsumer2D
{
    long pData;
    static {
        initIDs();
    }
    public static native void initIDs();
    public ShapeSpanIterator(boolean adjust) {
        setNormalize(adjust);
    }
    public void appendPath(PathIterator pi) {
        float coords[] = new float[6];
        setRule(pi.getWindingRule());
        while (!pi.isDone()) {
            addSegment(pi.currentSegment(coords), coords);
            pi.next();
        }
        pathDone();
    }
    public native void appendPoly(int xPoints[], int yPoints[], int nPoints,
                                  int xoff, int yoff);
    private native void setNormalize(boolean adjust);
    public void setOutputAreaXYWH(int x, int y, int w, int h) {
        setOutputAreaXYXY(x, y, Region.dimAdd(x, w), Region.dimAdd(y, h));
    }
    public native void setOutputAreaXYXY(int lox, int loy, int hix, int hiy);
    public void setOutputArea(Rectangle r) {
        setOutputAreaXYWH(r.x, r.y, r.width, r.height);
    }
    public void setOutputArea(Region r) {
        setOutputAreaXYXY(r.lox, r.loy, r.hix, r.hiy);
    }
    public native void setRule(int rule);
    public native void addSegment(int type, float coords[]);
    public native void getPathBox(int pathbox[]);
    public native void intersectClipBox(int lox, int loy, int hix, int hiy);
    public native boolean nextSpan(int spanbox[]);
    public native void skipDownTo(int y);
    public native long getNativeIterator();
    public native void dispose();
    public native void moveTo(float x, float y);
    public native void lineTo(float x, float y);
    public native void quadTo(float x1, float y1,
                              float x2, float y2);
    public native void curveTo(float x1, float y1,
                               float x2, float y2,
                               float x3, float y3);
    public native void closePath();
    public native void pathDone();
    public native long getNativeConsumer();
}

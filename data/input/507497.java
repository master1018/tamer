public class GradientPaint implements Paint {
    Color color1;
    Color color2;
    Point2D point1;
    Point2D point2;
    boolean cyclic;
    public GradientPaint(Point2D point1, Color color1, Point2D point2, Color color2, boolean cyclic) {
        if (point1 == null || point2 == null) {
            throw new NullPointerException(Messages.getString("awt.6D")); 
        }
        if (color1 == null || color2 == null) {
            throw new NullPointerException(Messages.getString("awt.6E")); 
        }
        this.point1 = point1;
        this.point2 = point2;
        this.color1 = color1;
        this.color2 = color2;
        this.cyclic = cyclic;
    }
    public GradientPaint(float x1, float y1, Color color1, float x2, float y2, Color color2,
            boolean cyclic) {
        this(new Point2D.Float(x1, y1), color1, new Point2D.Float(x2, y2), color2, cyclic);
    }
    public GradientPaint(float x1, float y1, Color color1, float x2, float y2, Color color2) {
        this(x1, y1, color1, x2, y2, color2, false);
    }
    public GradientPaint(Point2D point1, Color color1, Point2D point2, Color color2) {
        this(point1, color1, point2, color2, false);
    }
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
            Rectangle2D userBounds, AffineTransform t, RenderingHints hints) {
        return new GradientPaintContext(cm, t, point1, color1, point2, color2, cyclic);
    }
    public Color getColor1() {
        return color1;
    }
    public Color getColor2() {
        return color2;
    }
    public Point2D getPoint1() {
        return point1;
    }
    public Point2D getPoint2() {
        return point2;
    }
    public int getTransparency() {
        int a1 = color1.getAlpha();
        int a2 = color2.getAlpha();
        return (a1 == 0xFF && a2 == 0xFF) ? OPAQUE : TRANSLUCENT;
    }
    public boolean isCyclic() {
        return cyclic;
    }
}

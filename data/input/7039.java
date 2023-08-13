public final class LinearGradientPaint extends MultipleGradientPaint {
    private final Point2D start, end;
    public LinearGradientPaint(float startX, float startY,
                               float endX, float endY,
                               float[] fractions, Color[] colors)
    {
        this(new Point2D.Float(startX, startY),
             new Point2D.Float(endX, endY),
             fractions,
             colors,
             CycleMethod.NO_CYCLE);
    }
    public LinearGradientPaint(float startX, float startY,
                               float endX, float endY,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(new Point2D.Float(startX, startY),
             new Point2D.Float(endX, endY),
             fractions,
             colors,
             cycleMethod);
    }
    public LinearGradientPaint(Point2D start, Point2D end,
                               float[] fractions, Color[] colors)
    {
        this(start, end,
             fractions, colors,
             CycleMethod.NO_CYCLE);
    }
    public LinearGradientPaint(Point2D start, Point2D end,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(start, end,
             fractions, colors,
             cycleMethod,
             ColorSpaceType.SRGB,
             new AffineTransform());
    }
    @ConstructorProperties({ "startPoint", "endPoint", "fractions", "colors", "cycleMethod", "colorSpace", "transform" })
    public LinearGradientPaint(Point2D start, Point2D end,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace,
                               AffineTransform gradientTransform)
    {
        super(fractions, colors, cycleMethod, colorSpace, gradientTransform);
        if (start == null || end == null) {
            throw new NullPointerException("Start and end points must be" +
                                           "non-null");
        }
        if (start.equals(end)) {
            throw new IllegalArgumentException("Start point cannot equal" +
                                               "endpoint");
        }
        this.start = new Point2D.Double(start.getX(), start.getY());
        this.end = new Point2D.Double(end.getX(), end.getY());
    }
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform transform,
                                      RenderingHints hints)
    {
        transform = new AffineTransform(transform);
        transform.concatenate(gradientTransform);
        if ((fractions.length == 2) &&
            (cycleMethod != CycleMethod.REPEAT) &&
            (colorSpace == ColorSpaceType.SRGB))
        {
            boolean cyclic = (cycleMethod != CycleMethod.NO_CYCLE);
            return new GradientPaintContext(cm, start, end,
                                            transform,
                                            colors[0], colors[1],
                                            cyclic);
        } else {
            return new LinearGradientPaintContext(this, cm,
                                                  deviceBounds, userBounds,
                                                  transform, hints,
                                                  start, end,
                                                  fractions, colors,
                                                  cycleMethod, colorSpace);
        }
    }
    public Point2D getStartPoint() {
        return new Point2D.Double(start.getX(), start.getY());
    }
    public Point2D getEndPoint() {
        return new Point2D.Double(end.getX(), end.getY());
    }
}

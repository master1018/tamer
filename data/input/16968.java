public class BufferedPaints {
    static void setPaint(RenderQueue rq, SunGraphics2D sg2d,
                         Paint paint, int ctxflags)
    {
        if (sg2d.paintState <= SunGraphics2D.PAINT_ALPHACOLOR) {
            setColor(rq, sg2d.pixel);
        } else {
            boolean useMask = (ctxflags & BufferedContext.USE_MASK) != 0;
            switch (sg2d.paintState) {
            case SunGraphics2D.PAINT_GRADIENT:
                setGradientPaint(rq, sg2d,
                                 (GradientPaint)paint, useMask);
                break;
            case SunGraphics2D.PAINT_LIN_GRADIENT:
                setLinearGradientPaint(rq, sg2d,
                                       (LinearGradientPaint)paint, useMask);
                break;
            case SunGraphics2D.PAINT_RAD_GRADIENT:
                setRadialGradientPaint(rq, sg2d,
                                       (RadialGradientPaint)paint, useMask);
                break;
            case SunGraphics2D.PAINT_TEXTURE:
                setTexturePaint(rq, sg2d,
                                (TexturePaint)paint, useMask);
                break;
            default:
                break;
            }
        }
    }
    static void resetPaint(RenderQueue rq) {
        rq.ensureCapacity(4);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(RESET_PAINT);
    }
    private static void setColor(RenderQueue rq, int pixel) {
        rq.ensureCapacity(8);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(SET_COLOR);
        buf.putInt(pixel);
    }
    private static void setGradientPaint(RenderQueue rq, AffineTransform at,
                                         Color c1, Color c2,
                                         Point2D pt1, Point2D pt2,
                                         boolean isCyclic, boolean useMask)
    {
        PixelConverter pc = PixelConverter.ArgbPre.instance;
        int pixel1 = pc.rgbToPixel(c1.getRGB(), null);
        int pixel2 = pc.rgbToPixel(c2.getRGB(), null);
        double x = pt1.getX();
        double y = pt1.getY();
        at.translate(x, y);
        x = pt2.getX() - x;
        y = pt2.getY() - y;
        double len = Math.sqrt(x * x + y * y);
        at.rotate(x, y);
        at.scale(2*len, 1);
        at.translate(-0.25, 0);
        double p0, p1, p3;
        try {
            at.invert();
            p0 = at.getScaleX();
            p1 = at.getShearX();
            p3 = at.getTranslateX();
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            p0 = p1 = p3 = 0.0;
        }
        rq.ensureCapacityAndAlignment(44, 12);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(SET_GRADIENT_PAINT);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(isCyclic ? 1 : 0);
        buf.putDouble(p0).putDouble(p1).putDouble(p3);
        buf.putInt(pixel1).putInt(pixel2);
    }
    private static void setGradientPaint(RenderQueue rq,
                                         SunGraphics2D sg2d,
                                         GradientPaint paint,
                                         boolean useMask)
    {
        setGradientPaint(rq, (AffineTransform)sg2d.transform.clone(),
                         paint.getColor1(), paint.getColor2(),
                         paint.getPoint1(), paint.getPoint2(),
                         paint.isCyclic(), useMask);
    }
    private static void setTexturePaint(RenderQueue rq,
                                        SunGraphics2D sg2d,
                                        TexturePaint paint,
                                        boolean useMask)
    {
        BufferedImage bi = paint.getImage();
        SurfaceData dstData = sg2d.surfaceData;
        SurfaceData srcData =
            dstData.getSourceSurfaceData(bi, sg2d.TRANSFORM_ISIDENT,
                                         CompositeType.SrcOver, null);
        boolean filter =
            (sg2d.interpolationType !=
             AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        AffineTransform at = (AffineTransform)sg2d.transform.clone();
        Rectangle2D anchor = paint.getAnchorRect();
        at.translate(anchor.getX(), anchor.getY());
        at.scale(anchor.getWidth(), anchor.getHeight());
        double xp0, xp1, xp3, yp0, yp1, yp3;
        try {
            at.invert();
            xp0 = at.getScaleX();
            xp1 = at.getShearX();
            xp3 = at.getTranslateX();
            yp0 = at.getShearY();
            yp1 = at.getScaleY();
            yp3 = at.getTranslateY();
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            xp0 = xp1 = xp3 = yp0 = yp1 = yp3 = 0.0;
        }
        rq.ensureCapacityAndAlignment(68, 12);
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(SET_TEXTURE_PAINT);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(filter ? 1 : 0);
        buf.putLong(srcData.getNativeOps());
        buf.putDouble(xp0).putDouble(xp1).putDouble(xp3);
        buf.putDouble(yp0).putDouble(yp1).putDouble(yp3);
    }
    public static final int MULTI_MAX_FRACTIONS = 12;
    public static int convertSRGBtoLinearRGB(int color) {
        float input, output;
        input = color / 255.0f;
        if (input <= 0.04045f) {
            output = input / 12.92f;
        } else {
            output = (float)Math.pow((input + 0.055) / 1.055, 2.4);
        }
        return Math.round(output * 255.0f);
    }
    private static int colorToIntArgbPrePixel(Color c, boolean linear) {
        int rgb = c.getRGB();
        if (!linear && ((rgb >> 24) == -1)) {
            return rgb;
        }
        int a = rgb >>> 24;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >>  8) & 0xff;
        int b = (rgb      ) & 0xff;
        if (linear) {
            r = convertSRGBtoLinearRGB(r);
            g = convertSRGBtoLinearRGB(g);
            b = convertSRGBtoLinearRGB(b);
        }
        int a2 = a + (a >> 7);
        r = (r * a2) >> 8;
        g = (g * a2) >> 8;
        b = (b * a2) >> 8;
        return ((a << 24) | (r << 16) | (g << 8) | (b));
    }
    private static int[] convertToIntArgbPrePixels(Color[] colors,
                                                   boolean linear)
    {
        int[] pixels = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            pixels[i] = colorToIntArgbPrePixel(colors[i], linear);
        }
        return pixels;
    }
    private static void setLinearGradientPaint(RenderQueue rq,
                                               SunGraphics2D sg2d,
                                               LinearGradientPaint paint,
                                               boolean useMask)
    {
        boolean linear =
            (paint.getColorSpace() == ColorSpaceType.LINEAR_RGB);
        Color[] colors = paint.getColors();
        int numStops = colors.length;
        Point2D pt1 = paint.getStartPoint();
        Point2D pt2 = paint.getEndPoint();
        AffineTransform at = paint.getTransform();
        at.preConcatenate(sg2d.transform);
        if (!linear && numStops == 2 &&
            paint.getCycleMethod() != CycleMethod.REPEAT)
        {
            boolean isCyclic =
                (paint.getCycleMethod() != CycleMethod.NO_CYCLE);
            setGradientPaint(rq, at,
                             colors[0], colors[1],
                             pt1, pt2,
                             isCyclic, useMask);
            return;
        }
        int cycleMethod = paint.getCycleMethod().ordinal();
        float[] fractions = paint.getFractions();
        int[] pixels = convertToIntArgbPrePixels(colors, linear);
        double x = pt1.getX();
        double y = pt1.getY();
        at.translate(x, y);
        x = pt2.getX() - x;
        y = pt2.getY() - y;
        double len = Math.sqrt(x * x + y * y);
        at.rotate(x, y);
        at.scale(len, 1);
        float p0, p1, p3;
        try {
            at.invert();
            p0 = (float)at.getScaleX();
            p1 = (float)at.getShearX();
            p3 = (float)at.getTranslateX();
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            p0 = p1 = p3 = 0.0f;
        }
        rq.ensureCapacity(20 + 12 + (numStops*4*2));
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(SET_LINEAR_GRADIENT_PAINT);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(linear  ? 1 : 0);
        buf.putInt(cycleMethod);
        buf.putInt(numStops);
        buf.putFloat(p0);
        buf.putFloat(p1);
        buf.putFloat(p3);
        buf.put(fractions);
        buf.put(pixels);
    }
    private static void setRadialGradientPaint(RenderQueue rq,
                                               SunGraphics2D sg2d,
                                               RadialGradientPaint paint,
                                               boolean useMask)
    {
        boolean linear =
            (paint.getColorSpace() == ColorSpaceType.LINEAR_RGB);
        int cycleMethod = paint.getCycleMethod().ordinal();
        float[] fractions = paint.getFractions();
        Color[] colors = paint.getColors();
        int numStops = colors.length;
        int[] pixels = convertToIntArgbPrePixels(colors, linear);
        Point2D center = paint.getCenterPoint();
        Point2D focus = paint.getFocusPoint();
        float radius = paint.getRadius();
        double cx = center.getX();
        double cy = center.getY();
        double fx = focus.getX();
        double fy = focus.getY();
        AffineTransform at = paint.getTransform();
        at.preConcatenate(sg2d.transform);
        focus = at.transform(focus, focus);
        at.translate(cx, cy);
        at.rotate(fx - cx, fy - cy);
        at.scale(radius, radius);
        try {
            at.invert();
        } catch (Exception e) {
            at.setToScale(0.0, 0.0);
        }
        focus = at.transform(focus, focus);
        fx = Math.min(focus.getX(), 0.99);
        rq.ensureCapacity(20 + 28 + (numStops*4*2));
        RenderBuffer buf = rq.getBuffer();
        buf.putInt(SET_RADIAL_GRADIENT_PAINT);
        buf.putInt(useMask ? 1 : 0);
        buf.putInt(linear  ? 1 : 0);
        buf.putInt(numStops);
        buf.putInt(cycleMethod);
        buf.putFloat((float)at.getScaleX());
        buf.putFloat((float)at.getShearX());
        buf.putFloat((float)at.getTranslateX());
        buf.putFloat((float)at.getShearY());
        buf.putFloat((float)at.getScaleY());
        buf.putFloat((float)at.getTranslateY());
        buf.putFloat((float)fx);
        buf.put(fractions);
        buf.put(pixels);
    }
}

abstract class XRPaints {
    static XRCompositeManager xrCompMan;
    static final XRGradient xrGradient = new XRGradient();
    static final XRLinearGradient xrLinearGradient = new XRLinearGradient();
    static final XRRadialGradient xrRadialGradient = new XRRadialGradient();
    static final XRTexture xrTexture = new XRTexture();
    public static void register(XRCompositeManager xrComp) {
        xrCompMan = xrComp;
    }
    private static XRPaints getXRPaint(SunGraphics2D sg2d) {
        switch (sg2d.paintState) {
        case SunGraphics2D.PAINT_GRADIENT:
            return xrGradient;
        case SunGraphics2D.PAINT_LIN_GRADIENT:
            return xrLinearGradient;
        case SunGraphics2D.PAINT_RAD_GRADIENT:
            return xrRadialGradient;
        case SunGraphics2D.PAINT_TEXTURE:
            return xrTexture;
        default:
            return null;
        }
    }
    static boolean isValid(SunGraphics2D sg2d) {
        XRPaints impl = getXRPaint(sg2d);
        return (impl != null && impl.isPaintValid(sg2d));
    }
    static void setPaint(SunGraphics2D sg2d, Paint paint) {
        XRPaints impl = getXRPaint(sg2d);
        if (impl != null) {
            impl.setXRPaint(sg2d, paint);
        }
    }
    abstract boolean isPaintValid(SunGraphics2D sg2d);
    abstract void setXRPaint(SunGraphics2D sg2d, Paint paint);
    private static class XRGradient extends XRPaints {
        private XRGradient() {
        }
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            return true;
        }
        void setXRPaint(SunGraphics2D sg2d, Paint pt) {
            GradientPaint paint = (GradientPaint) pt;
            int[] pixels = convertToIntArgbPixels(new Color[] { paint.getColor1(), paint.getColor2() }, false);
            float fractions[] = new float[2];
            fractions[0] = 0;
            fractions[1] = 1;
            Point2D pt1 = paint.getPoint1();
            Point2D pt2 = paint.getPoint2();
            AffineTransform at = (AffineTransform) sg2d.transform.clone();
            try {
                at.invert();
            } catch (NoninvertibleTransformException ex) {
                at.setToIdentity();
            }
            int repeat = paint.isCyclic() ? XRUtils.RepeatReflect : XRUtils.RepeatPad;
            XRBackend con = xrCompMan.getBackend();
            int gradient = con.createLinearGradient(pt1, pt2, fractions, pixels, repeat, at);
            xrCompMan.setGradientPaint(new XRSurfaceData.XRInternalSurfaceData(con, gradient, at));
        }
    }
    public int getGradientLength(Point2D pt1, Point2D pt2) {
           double xDiff = Math.max(pt1.getX(), pt2.getX()) - Math.min(pt1.getX(), pt2.getX());
           double yDiff = Math.max(pt1.getY(), pt2.getY()) - Math.min(pt1.getY(), pt2.getY());
           return (int) Math.ceil(Math.sqrt(xDiff*xDiff + yDiff*yDiff));
    }
    private static class XRLinearGradient extends XRPaints {
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            return true;
        }
        @Override
        void setXRPaint(SunGraphics2D sg2d, Paint pt) {
            LinearGradientPaint paint = (LinearGradientPaint) pt;
            boolean linear = (paint.getColorSpace() == ColorSpaceType.LINEAR_RGB);
            Color[] colors = paint.getColors();
            Point2D pt1 = paint.getStartPoint();
            Point2D pt2 = paint.getEndPoint();
            AffineTransform at = paint.getTransform();
            at.preConcatenate(sg2d.transform);
            int repeat = XRUtils.getRepeatForCycleMethod(paint.getCycleMethod());
            float[] fractions = paint.getFractions();
            int[] pixels = convertToIntArgbPixels(colors, linear);
            try {
                at.invert();
            } catch (NoninvertibleTransformException ex) {
                ex.printStackTrace();
            }
            XRBackend con = xrCompMan.getBackend();
            int gradient = con.createLinearGradient(pt1, pt2, fractions, pixels, repeat, at);
            xrCompMan.setGradientPaint(new XRSurfaceData.XRInternalSurfaceData(con, gradient, at));
        }
    }
    private static class XRRadialGradient extends XRPaints {
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            RadialGradientPaint grad = (RadialGradientPaint) sg2d.paint;
            return grad.getFocusPoint().equals(grad.getCenterPoint());
        }
        @Override
        void setXRPaint(SunGraphics2D sg2d, Paint pt) {
            RadialGradientPaint paint = (RadialGradientPaint) pt;
            boolean linear = (paint.getColorSpace() == ColorSpaceType.LINEAR_RGB);
            Color[] colors = paint.getColors();
            Point2D center = paint.getCenterPoint();
            Point2D focus = paint.getFocusPoint();
            int repeat = XRUtils.getRepeatForCycleMethod(paint.getCycleMethod());
            float[] fractions = paint.getFractions();
            int[] pixels = convertToIntArgbPixels(colors, linear);
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
            try {
                at.invert();
            } catch (Exception e) {
                at.setToScale(0.0, 0.0);
            }
            focus = at.transform(focus, focus);
            fx = Math.min(focus.getX(), 0.99);
            XRBackend con = xrCompMan.getBackend();
            int gradient = con.createRadialGradient(new Point2D.Float(0, 0), new Point2D.Float(0, 0), 0, radius, fractions, pixels, repeat, at);
            xrCompMan.setGradientPaint(new XRSurfaceData.XRInternalSurfaceData(con, gradient, at));
        }
    }
    private static class XRTexture extends XRPaints {
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            TexturePaint paint = (TexturePaint) sg2d.paint;
            BufferedImage bi = paint.getImage();
            XRSurfaceData dstData = (XRSurfaceData) sg2d.getDestSurface();
            SurfaceData srcData = dstData.getSourceSurfaceData(bi, SunGraphics2D.TRANSFORM_ISIDENT, CompositeType.SrcOver, null);
            if (!(srcData instanceof XRSurfaceData)) {
                srcData = dstData.getSourceSurfaceData(bi, SunGraphics2D.TRANSFORM_ISIDENT, CompositeType.SrcOver, null);
                if (!(srcData instanceof XRSurfaceData)) {
                    return false;
                }
            }
            return true;
        }
        @Override
        void setXRPaint(SunGraphics2D sg2d, Paint pt) {
            TexturePaint paint = (TexturePaint) pt;
            BufferedImage bi = paint.getImage();
            SurfaceData dstData = sg2d.surfaceData;
            SurfaceData srcData = dstData.getSourceSurfaceData(bi, SunGraphics2D.TRANSFORM_ISIDENT, CompositeType.SrcOver, null);
            if (!(srcData instanceof XRSurfaceData)) {
                srcData = dstData.getSourceSurfaceData(paint.getImage(), SunGraphics2D.TRANSFORM_ISIDENT, CompositeType.SrcOver, null);
                if (!(srcData instanceof XRSurfaceData)) {
                    throw new InternalError("Surface not cachable");
                }
            }
            XRSurfaceData x11SrcData = (XRSurfaceData) srcData;
            AffineTransform at = (AffineTransform) sg2d.transform.clone();
            Rectangle2D anchor = paint.getAnchorRect();
            at.translate(anchor.getX(), anchor.getY());
            at.scale(anchor.getWidth() / ((double) bi.getWidth()), anchor.getHeight() / ((double) bi.getHeight()));
            try {
                at.invert();
            } catch (NoninvertibleTransformException ex) {
                at.setToIdentity(); 
            }
            x11SrcData.validateAsSource(at, XRUtils.RepeatNormal, XRUtils.ATransOpToXRQuality(sg2d.interpolationType));
            xrCompMan.setTexturePaint(((XRSurfaceData) srcData));
        }
    }
    public int[] convertToIntArgbPixels(Color[] colors, boolean linear) {
        int[] pixels = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            pixels[i] = colorToIntArgbPixel(colors[i], linear);
        }
        return pixels;
    }
    public int colorToIntArgbPixel(Color c, boolean linear) {
        int rgb = c.getRGB();
        int a = rgb >>> 24;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;
        if (linear) {
            r = BufferedPaints.convertSRGBtoLinearRGB(r);
            g = BufferedPaints.convertSRGBtoLinearRGB(g);
            b = BufferedPaints.convertSRGBtoLinearRGB(b);
        }
        a *= xrCompMan.getExtraAlpha();
        return ((a << 24) | (r << 16) | (g << 8) | (b));
    }
}

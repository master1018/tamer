abstract class OGLPaints {
    private static Map<Integer, OGLPaints> impls =
        new HashMap<Integer, OGLPaints>(4, 1.0f);
    static {
        impls.put(SunGraphics2D.PAINT_GRADIENT, new Gradient());
        impls.put(SunGraphics2D.PAINT_LIN_GRADIENT, new LinearGradient());
        impls.put(SunGraphics2D.PAINT_RAD_GRADIENT, new RadialGradient());
        impls.put(SunGraphics2D.PAINT_TEXTURE, new Texture());
    }
    static boolean isValid(SunGraphics2D sg2d) {
        OGLPaints impl = impls.get(sg2d.paintState);
        return (impl != null && impl.isPaintValid(sg2d));
    }
    abstract boolean isPaintValid(SunGraphics2D sg2d);
    private static class Gradient extends OGLPaints {
        private Gradient() {}
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            return true;
        }
    }
    private static class Texture extends OGLPaints {
        private Texture() {}
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            TexturePaint paint = (TexturePaint)sg2d.paint;
            OGLSurfaceData dstData = (OGLSurfaceData)sg2d.surfaceData;
            BufferedImage bi = paint.getImage();
            if (!dstData.isTexNonPow2Available()) {
                int imgw = bi.getWidth();
                int imgh = bi.getHeight();
                if ((imgw & (imgw - 1)) != 0 || (imgh & (imgh - 1)) != 0) {
                    return false;
                }
            }
            SurfaceData srcData =
                dstData.getSourceSurfaceData(bi, sg2d.TRANSFORM_ISIDENT,
                                             CompositeType.SrcOver, null);
            if (!(srcData instanceof OGLSurfaceData)) {
                srcData =
                    dstData.getSourceSurfaceData(bi, sg2d.TRANSFORM_ISIDENT,
                                                 CompositeType.SrcOver, null);
                if (!(srcData instanceof OGLSurfaceData)) {
                    return false;
                }
            }
            OGLSurfaceData oglData = (OGLSurfaceData)srcData;
            if (oglData.getType() != OGLSurfaceData.TEXTURE) {
                return false;
            }
            return true;
        }
    }
    private static abstract class MultiGradient extends OGLPaints {
        protected MultiGradient() {}
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            MultipleGradientPaint paint = (MultipleGradientPaint)sg2d.paint;
            if (paint.getFractions().length > MULTI_MAX_FRACTIONS) {
                return false;
            }
            OGLSurfaceData dstData = (OGLSurfaceData)sg2d.surfaceData;
            OGLGraphicsConfig gc = dstData.getOGLGraphicsConfig();
            if (!gc.isCapPresent(CAPS_EXT_GRAD_SHADER)) {
                return false;
            }
            return true;
        }
    }
    private static class LinearGradient extends MultiGradient {
        private LinearGradient() {}
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            LinearGradientPaint paint = (LinearGradientPaint)sg2d.paint;
            if (paint.getFractions().length == 2 &&
                paint.getCycleMethod() != CycleMethod.REPEAT &&
                paint.getColorSpace() != ColorSpaceType.LINEAR_RGB)
            {
                return true;
            }
            return super.isPaintValid(sg2d);
        }
    }
    private static class RadialGradient extends MultiGradient {
        private RadialGradient() {}
    }
}

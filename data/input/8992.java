abstract class D3DPaints {
    private static Map<Integer, D3DPaints> impls =
        new HashMap<Integer, D3DPaints>(4, 1.0f);
    static {
        impls.put(SunGraphics2D.PAINT_GRADIENT, new Gradient());
        impls.put(SunGraphics2D.PAINT_LIN_GRADIENT, new LinearGradient());
        impls.put(SunGraphics2D.PAINT_RAD_GRADIENT, new RadialGradient());
        impls.put(SunGraphics2D.PAINT_TEXTURE, new Texture());
    }
    static boolean isValid(SunGraphics2D sg2d) {
        D3DPaints impl = impls.get(sg2d.paintState);
        return (impl != null && impl.isPaintValid(sg2d));
    }
    abstract boolean isPaintValid(SunGraphics2D sg2d);
    private static class Gradient extends D3DPaints {
        private Gradient() {}
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
            D3DGraphicsDevice gd = (D3DGraphicsDevice)
                dstData.getDeviceConfiguration().getDevice();
            return gd.isCapPresent(CAPS_LCD_SHADER);
        }
    }
    private static class Texture extends D3DPaints {
        private Texture() {}
        @Override
        public boolean isPaintValid(SunGraphics2D sg2d) {
            TexturePaint paint = (TexturePaint)sg2d.paint;
            D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
            BufferedImage bi = paint.getImage();
            D3DGraphicsDevice gd =
                (D3DGraphicsDevice)dstData.getDeviceConfiguration().getDevice();
            int imgw = bi.getWidth();
            int imgh = bi.getHeight();
            if (!gd.isCapPresent(CAPS_TEXNONPOW2)) {
                if ((imgw & (imgw - 1)) != 0 || (imgh & (imgh - 1)) != 0) {
                    return false;
                }
            }
            if (!gd.isCapPresent(CAPS_TEXNONSQUARE) && imgw != imgh)
            {
                return false;
            }
            SurfaceData srcData =
                dstData.getSourceSurfaceData(bi, sg2d.TRANSFORM_ISIDENT,
                                             CompositeType.SrcOver, null);
            if (!(srcData instanceof D3DSurfaceData)) {
                srcData =
                    dstData.getSourceSurfaceData(bi, sg2d.TRANSFORM_ISIDENT,
                                                 CompositeType.SrcOver, null);
                if (!(srcData instanceof D3DSurfaceData)) {
                    return false;
                }
            }
            D3DSurfaceData d3dData = (D3DSurfaceData)srcData;
            if (d3dData.getType() != D3DSurfaceData.TEXTURE) {
                return false;
            }
            return true;
        }
    }
    private static abstract class MultiGradient extends D3DPaints {
        public static final int MULTI_MAX_FRACTIONS_D3D = 8;
        protected MultiGradient() {}
        @Override
        boolean isPaintValid(SunGraphics2D sg2d) {
            MultipleGradientPaint paint = (MultipleGradientPaint)sg2d.paint;
            if (paint.getFractions().length > MULTI_MAX_FRACTIONS_D3D) {
                return false;
            }
            D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
            D3DGraphicsDevice gd = (D3DGraphicsDevice)
                dstData.getDeviceConfiguration().getDevice();
            if (!gd.isCapPresent(CAPS_LCD_SHADER)) {
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
                D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
                D3DGraphicsDevice gd = (D3DGraphicsDevice)
                    dstData.getDeviceConfiguration().getDevice();
                if (gd.isCapPresent(CAPS_LCD_SHADER)) {
                    return true;
                }
            }
            return super.isPaintValid(sg2d);
        }
    }
    private static class RadialGradient extends MultiGradient {
        private RadialGradient() {}
    }
}

public class XRMaskFill extends MaskFill {
    static void register() {
        GraphicsPrimitive[] primitives = {
                new XRMaskFill(AnyColor, SrcOver, XRSurfaceData.IntRgbX11),
                new XRMaskFill(OpaqueColor, SrcNoEa, XRSurfaceData.IntRgbX11),
                new XRMaskFill(GradientPaint, SrcOver, XRSurfaceData.IntRgbX11),
                new XRMaskFill(OpaqueGradientPaint, SrcNoEa,
                        XRSurfaceData.IntRgbX11),
                new XRMaskFill(LinearGradientPaint, SrcOver,
                        XRSurfaceData.IntRgbX11),
                new XRMaskFill(OpaqueLinearGradientPaint, SrcNoEa,
                        XRSurfaceData.IntRgbX11),
                new XRMaskFill(RadialGradientPaint, SrcOver,
                        XRSurfaceData.IntRgbX11),
                new XRMaskFill(OpaqueRadialGradientPaint, SrcNoEa,
                        XRSurfaceData.IntRgbX11),
                new XRMaskFill(TexturePaint, SrcOver, XRSurfaceData.IntRgbX11),
                new XRMaskFill(OpaqueTexturePaint, SrcNoEa,
                        XRSurfaceData.IntRgbX11),
                new XRMaskFill(AnyColor, SrcOver, XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(OpaqueColor, SrcNoEa, XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(GradientPaint, SrcOver, XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(OpaqueGradientPaint, SrcNoEa,
                        XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(LinearGradientPaint, SrcOver,
                        XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(OpaqueLinearGradientPaint, SrcNoEa,
                        XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(RadialGradientPaint, SrcOver,
                        XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(OpaqueRadialGradientPaint, SrcNoEa,
                        XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(TexturePaint, SrcOver, XRSurfaceData.IntArgbPreX11),
                new XRMaskFill(OpaqueTexturePaint, SrcNoEa,
                        XRSurfaceData.IntArgbPreX11)
                };
        GraphicsPrimitiveMgr.register(primitives);
    }
    protected XRMaskFill(SurfaceType srcType, CompositeType compType,
            SurfaceType surfaceType) {
        super(srcType, compType, surfaceType);
    }
    protected native void maskFill(long xsdo, int x, int y, int w, int h,
            int maskoff, int maskscan, int masklen, byte[] mask);
    public void MaskFill(SunGraphics2D sg2d, SurfaceData sData, Composite comp,
            final int x, final int y, final int w, final int h,
            final byte[] mask, final int maskoff, final int maskscan) {
        try {
            SunToolkit.awtLock();
            XRSurfaceData x11sd = (XRSurfaceData) sData;
            x11sd.validateAsDestination(null, sg2d.getCompClip());
            XRCompositeManager maskBuffer = x11sd.maskBuffer;
            maskBuffer.validateCompositeState(comp, sg2d.transform, sg2d.paint, sg2d);
            int maskPict = maskBuffer.getMaskBuffer().uploadMask(w, h, maskscan, maskoff, mask);
            maskBuffer.XRComposite(XRUtils.None, maskPict, x11sd.picture, x, y, 0, 0, x, y, w, h);
            maskBuffer.getMaskBuffer().clearUploadMask(maskPict, w, h);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
}

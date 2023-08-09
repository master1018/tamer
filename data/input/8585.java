public class X11PMBlitBgLoops extends BlitBg {
    public static void register()
    {
        GraphicsPrimitive[] primitives = {
            new X11PMBlitBgLoops(X11SurfaceData.IntBgrX11_BM,
                                 X11SurfaceData.IntBgrX11),
            new X11PMBlitBgLoops(X11SurfaceData.IntRgbX11_BM,
                                 X11SurfaceData.IntRgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.ThreeByteBgrX11_BM,
                                 X11SurfaceData.ThreeByteBgrX11),
            new X11PMBlitBgLoops(X11SurfaceData.ThreeByteRgbX11_BM,
                                 X11SurfaceData.ThreeByteRgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.ByteIndexedX11_BM,
                                 X11SurfaceData.ByteIndexedOpaqueX11),
            new X11PMBlitBgLoops(X11SurfaceData.ByteGrayX11_BM,
                                 X11SurfaceData.ByteGrayX11),
            new X11PMBlitBgLoops(X11SurfaceData.Index8GrayX11_BM,
                                 X11SurfaceData.Index8GrayX11),
            new X11PMBlitBgLoops(X11SurfaceData.UShort555RgbX11_BM,
                                 X11SurfaceData.UShort555RgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.UShort565RgbX11_BM,
                                 X11SurfaceData.UShort565RgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.UShortIndexedX11_BM,
                                 X11SurfaceData.UShortIndexedX11),
            new X11PMBlitBgLoops(X11SurfaceData.IntRgbX11_BM,
                                 X11SurfaceData.IntArgbPreX11),
            new X11PMBlitBgLoops(X11SurfaceData.IntBgrX11_BM,
                                 X11SurfaceData.FourByteAbgrPreX11),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    public X11PMBlitBgLoops(SurfaceType srcType, SurfaceType dstType)
    {
        super(srcType, CompositeType.SrcNoEa, dstType);
    }
    @Override
    public void BlitBg(SurfaceData src, SurfaceData dst,
                       Composite comp, Region clip, int bgColor,
                       int sx, int sy,
                       int dx, int dy,
                       int w, int h)
    {
        SunToolkit.awtLock();
        try {
            int pixel = dst.pixelFor(bgColor);
            X11SurfaceData x11sd = (X11SurfaceData)dst;
            long xgc = x11sd.getBlitGC(clip, false);
            nativeBlitBg(src.getNativeOps(), dst.getNativeOps(),
                         xgc, pixel,
                         sx, sy, dx, dy, w, h);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    private native void nativeBlitBg(long srcData, long dstData,
                                     long xgc, int pixel,
                                     int sx, int sy,
                                     int dx, int dy,
                                     int w, int h);
}

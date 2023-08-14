public class X11PMBlitLoops extends Blit {
    public static void register()
    {
        GraphicsPrimitive[] primitives = {
            new X11PMBlitLoops(X11SurfaceData.IntBgrX11,
                               X11SurfaceData.IntBgrX11, false),
            new X11PMBlitLoops(X11SurfaceData.IntRgbX11,
                               X11SurfaceData.IntRgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteBgrX11,
                               X11SurfaceData.ThreeByteBgrX11, false),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteRgbX11,
                               X11SurfaceData.ThreeByteRgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.ByteIndexedOpaqueX11,
                               X11SurfaceData.ByteIndexedOpaqueX11, false),
            new X11PMBlitLoops(X11SurfaceData.ByteGrayX11,
                               X11SurfaceData.ByteGrayX11, false),
            new X11PMBlitLoops(X11SurfaceData.Index8GrayX11,
                               X11SurfaceData.Index8GrayX11, false),
            new X11PMBlitLoops(X11SurfaceData.UShort555RgbX11,
                               X11SurfaceData.UShort555RgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.UShort565RgbX11,
                               X11SurfaceData.UShort565RgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.UShortIndexedX11,
                               X11SurfaceData.UShortIndexedX11, false),
            new X11PMBlitLoops(X11SurfaceData.IntBgrX11_BM,
                               X11SurfaceData.IntBgrX11, true),
            new X11PMBlitLoops(X11SurfaceData.IntRgbX11_BM,
                               X11SurfaceData.IntRgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteBgrX11_BM,
                               X11SurfaceData.ThreeByteBgrX11, true),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteRgbX11_BM,
                               X11SurfaceData.ThreeByteRgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.ByteIndexedX11_BM,
                               X11SurfaceData.ByteIndexedOpaqueX11, true),
            new X11PMBlitLoops(X11SurfaceData.ByteGrayX11_BM,
                               X11SurfaceData.ByteGrayX11, true),
            new X11PMBlitLoops(X11SurfaceData.Index8GrayX11_BM,
                               X11SurfaceData.Index8GrayX11, true),
            new X11PMBlitLoops(X11SurfaceData.UShort555RgbX11_BM,
                               X11SurfaceData.UShort555RgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.UShort565RgbX11_BM,
                               X11SurfaceData.UShort565RgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.UShortIndexedX11_BM,
                               X11SurfaceData.UShortIndexedX11, true),
            new X11PMBlitLoops(X11SurfaceData.IntRgbX11,
                               X11SurfaceData.IntArgbPreX11, true),
            new X11PMBlitLoops(X11SurfaceData.IntRgbX11,
                               X11SurfaceData.IntArgbPreX11, false),
            new X11PMBlitLoops(X11SurfaceData.IntRgbX11_BM,
                               X11SurfaceData.IntArgbPreX11, true),
            new X11PMBlitLoops(X11SurfaceData.IntBgrX11,
                               X11SurfaceData.FourByteAbgrPreX11, true),
            new X11PMBlitLoops(X11SurfaceData.IntBgrX11,
                               X11SurfaceData.FourByteAbgrPreX11, false),
            new X11PMBlitLoops(X11SurfaceData.IntBgrX11_BM,
                               X11SurfaceData.FourByteAbgrPreX11, true),
            new DelegateBlitLoop(X11SurfaceData.IntBgrX11_BM,
                                 X11SurfaceData.IntBgrX11),
            new DelegateBlitLoop(X11SurfaceData.IntRgbX11_BM,
                                 X11SurfaceData.IntRgbX11),
            new DelegateBlitLoop(X11SurfaceData.ThreeByteBgrX11_BM,
                                 X11SurfaceData.ThreeByteBgrX11),
            new DelegateBlitLoop(X11SurfaceData.ThreeByteRgbX11_BM,
                                 X11SurfaceData.ThreeByteRgbX11),
            new DelegateBlitLoop(X11SurfaceData.ByteIndexedX11_BM,
                                 X11SurfaceData.ByteIndexedOpaqueX11),
            new DelegateBlitLoop(X11SurfaceData.ByteGrayX11_BM,
                                 X11SurfaceData.ByteGrayX11),
            new DelegateBlitLoop(X11SurfaceData.Index8GrayX11_BM,
                                 X11SurfaceData.Index8GrayX11),
            new DelegateBlitLoop(X11SurfaceData.UShort555RgbX11_BM,
                                 X11SurfaceData.UShort555RgbX11),
            new DelegateBlitLoop(X11SurfaceData.UShort565RgbX11_BM,
                                 X11SurfaceData.UShort565RgbX11),
            new DelegateBlitLoop(X11SurfaceData.UShortIndexedX11_BM,
                                 X11SurfaceData.UShortIndexedX11),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    public X11PMBlitLoops(SurfaceType srcType, SurfaceType dstType,
                          boolean over) {
        super(srcType,
              over ? CompositeType.SrcOverNoEa : CompositeType.SrcNoEa,
              dstType);
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy,
                     int dx, int dy,
                     int w, int h)
    {
        SunToolkit.awtLock();
        try {
            X11SurfaceData x11sd = (X11SurfaceData)dst;
            long xgc = x11sd.getBlitGC(null, false);
            nativeBlit(src.getNativeOps(), dst.getNativeOps(), xgc, clip,
                       sx, sy, dx, dy, w, h);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    private native void nativeBlit(long srcData, long dstData,
                                   long xgc, Region clip,
                                   int sx, int sy, int dx, int dy,
                                   int w, int h);
    static class DelegateBlitLoop extends Blit {
        SurfaceType dstType;
        public DelegateBlitLoop(SurfaceType realDstType, SurfaceType delegateDstType) {
            super(SurfaceType.Any, CompositeType.SrcNoEa, realDstType);
            this.dstType = delegateDstType;
        }
        public void Blit(SurfaceData src, SurfaceData dst,
                         Composite comp, Region clip,
                         int sx, int sy, int dx, int dy, int w, int h)
        {
            Blit blit = Blit.getFromCache(src.getSurfaceType(),
                                          CompositeType.SrcNoEa,
                                          dstType);
            blit.Blit(src, dst, comp, clip, sx, sy, dx, dy, w, h);
            updateBitmask(src, dst,
                          src.getColorModel() instanceof IndexColorModel);
        }
    }
    private static native void updateBitmask(SurfaceData src,
                                             SurfaceData dst,
                                             boolean isICM);
}

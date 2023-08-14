public class GDIBlitLoops extends Blit {
    int rmask, gmask, bmask;
    boolean indexed = false;
    public static void register()
    {
        GraphicsPrimitive[] primitives = {
            new GDIBlitLoops(SurfaceType.IntRgb,
                             GDIWindowSurfaceData.AnyGdi),
            new GDIBlitLoops(SurfaceType.Ushort555Rgb,
                             GDIWindowSurfaceData.AnyGdi,
                             0x7C00, 0x03E0, 0x001F),
            new GDIBlitLoops(SurfaceType.Ushort565Rgb,
                             GDIWindowSurfaceData.AnyGdi,
                             0xF800, 0x07E0, 0x001F),
            new GDIBlitLoops(SurfaceType.ThreeByteBgr,
                             GDIWindowSurfaceData.AnyGdi),
            new GDIBlitLoops(SurfaceType.ByteIndexedOpaque,
                             GDIWindowSurfaceData.AnyGdi,
                             true),
            new GDIBlitLoops(SurfaceType.Index8Gray,
                             GDIWindowSurfaceData.AnyGdi,
                             true),
            new GDIBlitLoops(SurfaceType.ByteGray,
                             GDIWindowSurfaceData.AnyGdi),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    public GDIBlitLoops(SurfaceType srcType, SurfaceType dstType) {
        this(srcType, dstType, 0, 0, 0);
    }
    public GDIBlitLoops(SurfaceType srcType, SurfaceType dstType,
                        boolean indexed)
    {
        this(srcType, dstType, 0, 0, 0);
        this.indexed = indexed;
    }
    public GDIBlitLoops(SurfaceType srcType, SurfaceType dstType,
                        int rmask, int gmask, int bmask)
    {
        super(srcType, CompositeType.SrcNoEa, dstType);
        this.rmask = rmask;
        this.gmask = gmask;
        this.bmask = bmask;
    }
    public native void nativeBlit(SurfaceData src, SurfaceData dst,
                                  Region clip,
                                  int sx, int sy, int dx, int dy,
                                  int w, int h,
                                  int rmask, int gmask, int bmask,
                                  boolean needLut);
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int sx, int sy, int dx, int dy, int w, int h)
    {
        nativeBlit(src, dst, clip, sx, sy, dx, dy, w, h,
                   rmask, gmask, bmask, indexed);
    }
}

public class OGLSurfaceDataProxy extends SurfaceDataProxy {
    public static SurfaceDataProxy createProxy(SurfaceData srcData,
                                               OGLGraphicsConfig dstConfig)
    {
        if (srcData instanceof OGLSurfaceData) {
            return UNCACHED;
        }
        return new OGLSurfaceDataProxy(dstConfig, srcData.getTransparency());
    }
    OGLGraphicsConfig oglgc;
    int transparency;
    public OGLSurfaceDataProxy(OGLGraphicsConfig oglgc, int transparency) {
        this.oglgc = oglgc;
        this.transparency = transparency;
    }
    @Override
    public SurfaceData validateSurfaceData(SurfaceData srcData,
                                           SurfaceData cachedData,
                                           int w, int h)
    {
        if (cachedData == null) {
            cachedData = oglgc.createManagedSurface(w, h, transparency);
        }
        return cachedData;
    }
    @Override
    public boolean isSupportedOperation(SurfaceData srcData,
                                        int txtype,
                                        CompositeType comp,
                                        Color bgColor)
    {
        return (bgColor == null || transparency == Transparency.OPAQUE);
    }
}

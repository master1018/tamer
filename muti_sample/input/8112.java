public class D3DSurfaceDataProxy extends SurfaceDataProxy {
    public static SurfaceDataProxy createProxy(SurfaceData srcData,
                                               D3DGraphicsConfig dstConfig)
    {
        if (srcData instanceof D3DSurfaceData) {
            return UNCACHED;
        }
        return new D3DSurfaceDataProxy(dstConfig, srcData.getTransparency());
    }
    D3DGraphicsConfig d3dgc;
    int transparency;
    public D3DSurfaceDataProxy(D3DGraphicsConfig d3dgc, int transparency) {
        this.d3dgc = d3dgc;
        this.transparency = transparency;
        activateDisplayListener();
    }
    @Override
    public SurfaceData validateSurfaceData(SurfaceData srcData,
                                           SurfaceData cachedData,
                                           int w, int h)
    {
        if (cachedData == null || cachedData.isSurfaceLost()) {
            try {
                cachedData = d3dgc.createManagedSurface(w, h, transparency);
            } catch (InvalidPipeException e) {
                if (!d3dgc.getD3DDevice().isD3DAvailable()) {
                    invalidate();
                    flush();
                    return null;
                }
            }
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

public class XRSurfaceDataProxy extends SurfaceDataProxy implements Transparency {
    public static SurfaceDataProxy createProxy(SurfaceData srcData,
            XRGraphicsConfig dstConfig) {
        if (srcData instanceof XRSurfaceData) {
            return UNCACHED;
        }
        return new XRSurfaceDataProxy(dstConfig, srcData.getTransparency());
    }
    XRGraphicsConfig xrgc;
    int transparency;
    public XRSurfaceDataProxy(XRGraphicsConfig x11gc) {
        this.xrgc = x11gc;
    }
    @Override
    public SurfaceData validateSurfaceData(SurfaceData srcData,
            SurfaceData cachedData, int w, int h) {
        if (cachedData == null) {
            cachedData = XRSurfaceData.createData(xrgc, w, h, xrgc
                    .getColorModel(), null, 0, getTransparency());
        }
        return cachedData;
    }
    public XRSurfaceDataProxy(XRGraphicsConfig x11gc, int transparency) {
        this.xrgc = x11gc;
        this.transparency = transparency;
    }
    @Override
    public boolean isSupportedOperation(SurfaceData srcData, int txtype,
            CompositeType comp, Color bgColor) {
        return (bgColor == null || transparency == Transparency.TRANSLUCENT);
    }
    public int getTransparency() {
        return transparency;
    }
}

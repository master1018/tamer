public class WindowsSurfaceManagerFactory extends SurfaceManagerFactory {
    public VolatileSurfaceManager createVolatileManager(SunVolatileImage vImg,
                                                        Object context)
    {
        GraphicsConfiguration gc = vImg.getGraphicsConfig();
        if (gc instanceof D3DGraphicsConfig) {
            return new D3DVolatileSurfaceManager(vImg, context);
        } else if (gc instanceof WGLGraphicsConfig) {
            return new WGLVolatileSurfaceManager(vImg, context);
        } else {
            return new BufImgVolatileSurfaceManager(vImg, context);
        }
    }
}

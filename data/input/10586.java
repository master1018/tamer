public class UnixSurfaceManagerFactory extends SurfaceManagerFactory {
    public VolatileSurfaceManager createVolatileManager(SunVolatileImage vImg,
                                                        Object context)
    {
        GraphicsConfiguration gc = vImg.getGraphicsConfig();
        if (gc instanceof GLXGraphicsConfig) {
            return new GLXVolatileSurfaceManager(vImg, context);
        } else if(gc instanceof XRGraphicsConfig) {
            return new XRVolatileSurfaceManager(vImg, context);
        }else {
            return new X11VolatileSurfaceManager(vImg, context);
        }
    }
}

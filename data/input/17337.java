public class XRVolatileSurfaceManager extends VolatileSurfaceManager {
    public XRVolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        super(vImg, context);
    }
    protected boolean isAccelerationEnabled() {
        return true;
    }
    protected SurfaceData initAcceleratedSurface() {
        SurfaceData sData;
        try {
            XRGraphicsConfig gc = (XRGraphicsConfig) vImg.getGraphicsConfig();
            ColorModel cm = gc.getColorModel();
            long drawable = 0;
            if (context instanceof Long) {
                drawable = ((Long)context).longValue();
            }
            sData = XRSurfaceData.createData(gc,
                                              vImg.getWidth(),
                                              vImg.getHeight(),
                                              cm, vImg, drawable,
                                              vImg.getTransparency());
        } catch (NullPointerException ex) {
            sData = null;
        } catch (OutOfMemoryError er) {
            sData = null;
        }
        return sData;
    }
    protected boolean isConfigValid(GraphicsConfiguration gc) {
        return true;
    }
    @Override
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        if (isConfigValid(gc) && isAccelerationEnabled()) {
            return new ImageCapabilities(true);
        }
        return new ImageCapabilities(false);
    }
}

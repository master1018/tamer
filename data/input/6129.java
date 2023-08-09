public class X11VolatileSurfaceManager extends VolatileSurfaceManager {
    private boolean accelerationEnabled;
    public X11VolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        super(vImg, context);
        accelerationEnabled = X11SurfaceData.isAccelerationEnabled() &&
            (vImg.getTransparency() == Transparency.OPAQUE);
        if ((context != null) && !accelerationEnabled) {
            accelerationEnabled = true;
            sdAccel = initAcceleratedSurface();
            sdCurrent = sdAccel;
            if (sdBackup != null) {
                sdBackup = null;
            }
        }
    }
    protected boolean isAccelerationEnabled() {
        return accelerationEnabled;
    }
    protected SurfaceData initAcceleratedSurface() {
        SurfaceData sData;
        try {
            X11GraphicsConfig gc = (X11GraphicsConfig)vImg.getGraphicsConfig();
            ColorModel cm = gc.getColorModel();
            long drawable = 0;
            if (context instanceof Long) {
                drawable = ((Long)context).longValue();
            }
            sData = X11SurfaceData.createData(gc,
                                              vImg.getWidth(),
                                              vImg.getHeight(),
                                              cm, vImg, drawable,
                                              Transparency.OPAQUE);
        } catch (NullPointerException ex) {
            sData = null;
        } catch (OutOfMemoryError er) {
            sData = null;
        }
        return sData;
    }
    protected boolean isConfigValid(GraphicsConfiguration gc) {
        return ((gc == null) || (gc == vImg.getGraphicsConfig()));
    }
    @Override
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        if (isConfigValid(gc) && isAccelerationEnabled()) {
            return new ImageCapabilities(true);
        }
        return new ImageCapabilities(false);
    }
}

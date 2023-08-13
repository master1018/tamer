public class BufImgVolatileSurfaceManager extends VolatileSurfaceManager {
    public BufImgVolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        super(vImg, context);
    }
    protected boolean isAccelerationEnabled() {
        return false;
    }
    protected SurfaceData initAcceleratedSurface() {
        return null;
    }
}

public abstract class VolatileSurfaceManager
    extends SurfaceManager
    implements DisplayChangedListener
{
    protected SunVolatileImage vImg;
    protected SurfaceData sdAccel;
    protected SurfaceData sdBackup;
    protected SurfaceData sdCurrent;
    protected SurfaceData sdPrevious;
    protected boolean lostSurface;
    protected Object context;
    protected VolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        this.vImg = vImg;
        this.context = context;
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            ((SunGraphicsEnvironment)ge).addDisplayChangedListener(this);
        }
    }
    public void initialize() {
        if (isAccelerationEnabled()) {
            sdAccel = initAcceleratedSurface();
            if (sdAccel != null) {
                sdCurrent = sdAccel;
            }
        }
        if (sdCurrent == null &&
            vImg.getForcedAccelSurfaceType() == UNDEFINED)
        {
            sdCurrent = getBackupSurface();
        }
    }
    public SurfaceData getPrimarySurfaceData() {
        return sdCurrent;
    }
    protected abstract boolean isAccelerationEnabled();
    public int validate(GraphicsConfiguration gc) {
        int returnCode = VolatileImage.IMAGE_OK;
        boolean lostSurfaceTmp = lostSurface;
        lostSurface = false;
        if (isAccelerationEnabled()) {
            if (!isConfigValid(gc)) {
                returnCode = VolatileImage.IMAGE_INCOMPATIBLE;
            } else if (sdAccel == null) {
                sdAccel = initAcceleratedSurface();
                if (sdAccel != null) {
                    sdCurrent = sdAccel;
                    sdBackup = null;
                    returnCode = VolatileImage.IMAGE_RESTORED;
                } else {
                    sdCurrent = getBackupSurface();
                }
            } else if (sdAccel.isSurfaceLost()) {
                try {
                    restoreAcceleratedSurface();
                    sdCurrent = sdAccel;
                    sdAccel.setSurfaceLost(false);
                    sdBackup = null;
                    returnCode = VolatileImage.IMAGE_RESTORED;
                } catch (sun.java2d.InvalidPipeException e) {
                    sdCurrent = getBackupSurface();
                }
            } else if (lostSurfaceTmp) {
                returnCode = VolatileImage.IMAGE_RESTORED;
            }
        } else if (sdAccel != null) {
            sdCurrent = getBackupSurface();
            sdAccel = null;
            returnCode = VolatileImage.IMAGE_RESTORED;
        }
        if ((returnCode != VolatileImage.IMAGE_INCOMPATIBLE) &&
            (sdCurrent != sdPrevious))
        {
            sdPrevious = sdCurrent;
            returnCode = VolatileImage.IMAGE_RESTORED;
        }
        if (returnCode == VolatileImage.IMAGE_RESTORED) {
            initContents();
        }
        return returnCode;
    }
    public boolean contentsLost() {
        return lostSurface;
    }
    protected abstract SurfaceData initAcceleratedSurface();
    protected SurfaceData getBackupSurface() {
        if (sdBackup == null) {
            BufferedImage bImg = vImg.getBackupImage();
            SunWritableRaster.stealTrackable(bImg
                                             .getRaster()
                                             .getDataBuffer()).setUntrackable();
            sdBackup = BufImgSurfaceData.createData(bImg);
        }
        return sdBackup;
    }
    public void initContents() {
        if (sdCurrent != null) {
            Graphics g = vImg.createGraphics();
            g.clearRect(0, 0, vImg.getWidth(), vImg.getHeight());
            g.dispose();
        }
    }
    public SurfaceData restoreContents() {
        return getBackupSurface();
    }
    public void acceleratedSurfaceLost() {
        if (isAccelerationEnabled() && (sdCurrent == sdAccel)) {
            lostSurface = true;
        }
    }
    protected void restoreAcceleratedSurface() {
    }
    public void displayChanged() {
        if (!isAccelerationEnabled()) {
            return;
        }
        lostSurface = true;
        if (sdAccel != null) {
            sdBackup = null;
            sdCurrent = getBackupSurface();
            SurfaceData oldData = sdAccel;
            sdAccel = null;
            oldData.invalidate();
        }
        vImg.updateGraphicsConfig();
    }
    public void paletteChanged() {
        lostSurface = true;
    }
    protected boolean isConfigValid(GraphicsConfiguration gc) {
        return ((gc == null) ||
                (gc.getDevice() == vImg.getGraphicsConfig().getDevice()));
    }
    @Override
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        if (isConfigValid(gc)) {
            return isAccelerationEnabled() ?
                new AcceleratedImageCapabilities() :
                new ImageCapabilities(false);
        }
        return super.getCapabilities(gc);
    }
    private class AcceleratedImageCapabilities
        extends ImageCapabilities
    {
        AcceleratedImageCapabilities() {
            super(false);
        }
        @Override
        public boolean isAccelerated() {
            return (sdCurrent == sdAccel);
        }
        @Override
        public boolean isTrueVolatile() {
            return isAccelerated();
        }
    }
    public void flush() {
        lostSurface = true;
        SurfaceData oldSD = sdAccel;
        sdAccel = null;
        if (oldSD != null) {
            oldSD.flush();
        }
    }
}

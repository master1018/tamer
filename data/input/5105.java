public class WGLVolatileSurfaceManager
    extends VolatileSurfaceManager
{
    private boolean accelerationEnabled;
    public WGLVolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        super(vImg, context);
        int transparency = vImg.getTransparency();
        WGLGraphicsConfig gc = (WGLGraphicsConfig)vImg.getGraphicsConfig();
        accelerationEnabled =
            (transparency == Transparency.OPAQUE) ||
            ((transparency == Transparency.TRANSLUCENT) &&
             (gc.isCapPresent(CAPS_EXT_FBOBJECT) ||
              gc.isCapPresent(CAPS_STORED_ALPHA)));
    }
    protected boolean isAccelerationEnabled() {
        return accelerationEnabled;
    }
    protected SurfaceData initAcceleratedSurface() {
        SurfaceData sData;
        Component comp = vImg.getComponent();
        WComponentPeer peer =
            (comp != null) ? (WComponentPeer)comp.getPeer() : null;
        try {
            boolean createVSynced = false;
            boolean forceback = false;
            if (context instanceof Boolean) {
                forceback = ((Boolean)context).booleanValue();
                if (forceback) {
                    BufferCapabilities caps = peer.getBackBufferCaps();
                    if (caps instanceof ExtendedBufferCapabilities) {
                        ExtendedBufferCapabilities ebc =
                            (ExtendedBufferCapabilities)caps;
                        if (ebc.getVSync() == VSYNC_ON &&
                            ebc.getFlipContents() == COPIED)
                        {
                            createVSynced = true;
                            forceback = false;
                        }
                    }
                }
            }
            if (forceback) {
                sData = WGLSurfaceData.createData(peer, vImg, FLIP_BACKBUFFER);
            } else {
                WGLGraphicsConfig gc =
                    (WGLGraphicsConfig)vImg.getGraphicsConfig();
                ColorModel cm = gc.getColorModel(vImg.getTransparency());
                int type = vImg.getForcedAccelSurfaceType();
                if (type == OGLSurfaceData.UNDEFINED) {
                    type = gc.isCapPresent(CAPS_EXT_FBOBJECT) ?
                        OGLSurfaceData.FBOBJECT : OGLSurfaceData.PBUFFER;
                }
                if (createVSynced) {
                    sData = WGLSurfaceData.createData(peer, vImg, type);
                } else {
                    sData = WGLSurfaceData.createData(gc,
                                                      vImg.getWidth(),
                                                      vImg.getHeight(),
                                                      cm, vImg, type);
                }
            }
        } catch (NullPointerException ex) {
            sData = null;
        } catch (OutOfMemoryError er) {
            sData = null;
        }
        return sData;
    }
    @Override
    protected boolean isConfigValid(GraphicsConfiguration gc) {
        return ((gc == null) ||
                ((gc instanceof WGLGraphicsConfig) &&
                 (gc == vImg.getGraphicsConfig())));
    }
    @Override
    public void initContents() {
        if (vImg.getForcedAccelSurfaceType() != OGLSurfaceData.TEXTURE) {
            super.initContents();
        }
    }
}

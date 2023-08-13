public class GLXVolatileSurfaceManager extends VolatileSurfaceManager {
    private boolean accelerationEnabled;
    public GLXVolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        super(vImg, context);
        int transparency = vImg.getTransparency();
        GLXGraphicsConfig gc = (GLXGraphicsConfig)vImg.getGraphicsConfig();
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
        X11ComponentPeer peer =
            (comp != null) ? (X11ComponentPeer)comp.getPeer() : null;
        try {
            boolean createVSynced = false;
            boolean forceback = false;
            if (context instanceof Boolean) {
                forceback = ((Boolean)context).booleanValue();
                if (forceback && peer instanceof BackBufferCapsProvider) {
                    BackBufferCapsProvider provider =
                        (BackBufferCapsProvider)peer;
                    BufferCapabilities caps = provider.getBackBufferCaps();
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
                sData = GLXSurfaceData.createData(peer, vImg, FLIP_BACKBUFFER);
            } else {
                GLXGraphicsConfig gc =
                    (GLXGraphicsConfig)vImg.getGraphicsConfig();
                ColorModel cm = gc.getColorModel(vImg.getTransparency());
                int type = vImg.getForcedAccelSurfaceType();
                if (type == OGLSurfaceData.UNDEFINED) {
                    type = gc.isCapPresent(CAPS_EXT_FBOBJECT) ?
                        OGLSurfaceData.FBOBJECT : OGLSurfaceData.PBUFFER;
                }
                if (createVSynced) {
                    sData = GLXSurfaceData.createData(peer, vImg, type);
                } else {
                    sData = GLXSurfaceData.createData(gc,
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
        return ((gc == null) || (gc == vImg.getGraphicsConfig()));
    }
    @Override
    public void initContents() {
        if (vImg.getForcedAccelSurfaceType() != OGLSurfaceData.TEXTURE) {
            super.initContents();
        }
    }
}

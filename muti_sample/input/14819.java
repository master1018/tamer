public class D3DVolatileSurfaceManager
    extends VolatileSurfaceManager
{
    private boolean accelerationEnabled;
    private int restoreCountdown;
    public D3DVolatileSurfaceManager(SunVolatileImage vImg, Object context) {
        super(vImg, context);
        int transparency = vImg.getTransparency();
        D3DGraphicsDevice gd = (D3DGraphicsDevice)
            vImg.getGraphicsConfig().getDevice();
        accelerationEnabled =
            (transparency == Transparency.OPAQUE) ||
            (transparency == Transparency.TRANSLUCENT &&
             (gd.isCapPresent(CAPS_RT_PLAIN_ALPHA) ||
              gd.isCapPresent(CAPS_RT_TEXTURE_ALPHA)));
    }
    protected boolean isAccelerationEnabled() {
        return accelerationEnabled;
    }
    public void setAccelerationEnabled(boolean accelerationEnabled) {
        this.accelerationEnabled = accelerationEnabled;
    }
    protected SurfaceData initAcceleratedSurface() {
        SurfaceData sData;
        Component comp = vImg.getComponent();
        WComponentPeer peer =
            (comp != null) ? (WComponentPeer)comp.getPeer() : null;
        try {
            boolean forceback = false;
            if (context instanceof Boolean) {
                forceback = ((Boolean)context).booleanValue();
            }
            if (forceback) {
                sData = D3DSurfaceData.createData(peer, vImg);
            } else {
                D3DGraphicsConfig gc =
                    (D3DGraphicsConfig)vImg.getGraphicsConfig();
                ColorModel cm = gc.getColorModel(vImg.getTransparency());
                int type = vImg.getForcedAccelSurfaceType();
                if (type == UNDEFINED) {
                    type = RT_TEXTURE;
                }
                sData = D3DSurfaceData.createData(gc,
                                                  vImg.getWidth(),
                                                  vImg.getHeight(),
                                                  cm, vImg,
                                                  type);
            }
        } catch (NullPointerException ex) {
            sData = null;
        } catch (OutOfMemoryError er) {
            sData = null;
        } catch (InvalidPipeException ipe) {
            sData = null;
        }
        return sData;
    }
    protected boolean isConfigValid(GraphicsConfiguration gc) {
        return ((gc == null) || (gc == vImg.getGraphicsConfig()));
    }
    private synchronized void setRestoreCountdown(int count) {
        restoreCountdown = count;
    }
    @Override
    protected void restoreAcceleratedSurface() {
        synchronized (this) {
            if (restoreCountdown > 0) {
                restoreCountdown--;
                throw new
                    InvalidPipeException("Will attempt to restore surface " +
                                          " in " + restoreCountdown);
            }
        }
        SurfaceData sData = initAcceleratedSurface();
        if (sData != null) {
            sdAccel = sData;
        } else {
            throw new InvalidPipeException("could not restore surface");
        }
    }
    @Override
    public SurfaceData restoreContents() {
        acceleratedSurfaceLost();
        return super.restoreContents();
    }
    static void handleVItoScreenOp(SurfaceData src, SurfaceData dst) {
        if (src instanceof D3DSurfaceData &&
            dst instanceof GDIWindowSurfaceData)
        {
            D3DSurfaceData d3dsd = (D3DSurfaceData)src;
            SurfaceManager mgr =
                SurfaceManager.getManager((Image)d3dsd.getDestination());
            if (mgr instanceof D3DVolatileSurfaceManager) {
                D3DVolatileSurfaceManager vsm = (D3DVolatileSurfaceManager)mgr;
                if (vsm != null) {
                    d3dsd.setSurfaceLost(true);
                    GDIWindowSurfaceData wsd = (GDIWindowSurfaceData)dst;
                    WComponentPeer p = wsd.getPeer();
                    if (D3DScreenUpdateManager.canUseD3DOnScreen(p,
                            (Win32GraphicsConfig)p.getGraphicsConfiguration(),
                            p.getBackBuffersNum()))
                    {
                        vsm.setRestoreCountdown(10);
                    } else {
                        vsm.setAccelerationEnabled(false);
                    }
                }
            }
        }
    }
    @Override
    public void initContents() {
        if (vImg.getForcedAccelSurfaceType() != TEXTURE) {
            super.initContents();
        }
    }
}

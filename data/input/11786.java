public abstract class XSurfaceData extends SurfaceData {
    static boolean isX11SurfaceDataInitialized = false;
    public static boolean isX11SurfaceDataInitialized() {
        return isX11SurfaceDataInitialized;
    }
    public static void setX11SurfaceDataInitialized() {
        isX11SurfaceDataInitialized = true;
    }
    public XSurfaceData(SurfaceType surfaceType, ColorModel cm) {
        super(surfaceType, cm);
    }
    protected native void initOps(X11ComponentPeer peer, X11GraphicsConfig gc, int depth);
    protected static native long XCreateGC(long pXSData);
    protected static native void XResetClip(long xgc);
    protected static native void XSetClip(long xgc, int lox, int loy, int hix, int hiy, Region complexclip);
    protected native void flushNativeSurface();
    protected native boolean isDrawableValid();
    protected native void setInvalid();
    protected static native void XSetGraphicsExposures(long xgc, boolean needExposures);
}

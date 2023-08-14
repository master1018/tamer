public class PaintSurface extends SurfaceView {
    static {
        System.loadLibrary("sampleplugin");
    }
    private final int npp;
    private boolean validNPP = true;
    private Object nppLock = new Object();
    public PaintSurface(Context context, int NPP, int width, int height) {
        super(context);
        this.npp = NPP;
        this.getHolder().setFormat(PixelFormat.RGBA_8888);
        this.getHolder().addCallback(new Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                synchronized (nppLock) {
                    if (validNPP) {
                        nativeSurfaceChanged(npp, format, width, height);
                    }
                }
            }
            public void surfaceCreated(SurfaceHolder holder) {
                synchronized (nppLock) {
                    if (validNPP) {
                        nativeSurfaceCreated(npp);
                    }
                }
            }
            public void surfaceDestroyed(SurfaceHolder holder) {
                synchronized (nppLock) {
                    if (validNPP) {
                        nativeSurfaceDestroyed(npp);
                    }
                }
            }
        });
        this.getHolder().setFixedSize(width, height);
        this.setWillNotDraw(false);
    }
    private void invalidateNPP() {
        synchronized (nppLock) {
            validNPP = false;
        }
    }
    private native void nativeSurfaceCreated(int npp);
    private native void nativeSurfaceChanged(int npp, int format, int width, int height);
    private native void nativeSurfaceDestroyed(int npp);
}

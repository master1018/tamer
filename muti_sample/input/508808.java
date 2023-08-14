public class EGLSurfaceImpl extends EGLSurface {
    int mEGLSurface;
    private int mNativePixelRef;
    public EGLSurfaceImpl() {
        mEGLSurface = 0;
        mNativePixelRef = 0;
    }
    public EGLSurfaceImpl(int surface) {
        mEGLSurface = surface;
        mNativePixelRef = 0;
    }
}

public class EGLContextImpl extends EGLContext {
    private GLImpl mGLContext;
    int mEGLContext;
    public EGLContextImpl(int ctx) {
        mEGLContext = ctx;
        mGLContext = new GLImpl();
    }
    @Override
    public GL getGL() {
        return mGLContext;
    }
}

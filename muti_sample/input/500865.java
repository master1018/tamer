public class EGLConfigImpl extends EGLConfig {
    private int mEGLConfig;
    EGLConfigImpl(int config) {
        mEGLConfig = config;
    }
    int get() {
        return mEGLConfig;
    }
}

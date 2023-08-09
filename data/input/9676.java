public abstract class DesktopBrowse {
    private static volatile DesktopBrowse mInstance;
    public static void setInstance(DesktopBrowse instance) {
        if (mInstance != null) {
            throw new IllegalStateException("DesktopBrowse instance has already been set.");
        }
        mInstance = instance;
    }
    public static DesktopBrowse getInstance() {
        return mInstance;
    }
    public abstract void browse(URL url);
}

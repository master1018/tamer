public class UpdaterWindow {
    private UpdaterWindowImpl mWindow;
    public interface ISdkListener {
        void onSdkChange(boolean init);
    }
    public UpdaterWindow(Shell parentShell, ISdkLog sdkLog, String osSdkRoot,
            boolean userCanChangeSdkRoot) {
        mWindow = new UpdaterWindowImpl(parentShell, sdkLog, osSdkRoot, userCanChangeSdkRoot);
    }
    public void registerPage(String title, Class<? extends Composite> pageClass) {
        mWindow.registerExtraPage(title, pageClass);
    }
    public void setInitialPage(Class<? extends Composite> pageClass) {
        mWindow.setInitialPage(pageClass);
    }
    public void setRequestAutoUpdate(boolean requestAutoUpdate) {
        mWindow.setRequestAutoUpdate(requestAutoUpdate);
    }
    public void addListeners(ISdkListener listener) {
        mWindow.addListeners(listener);
    }
    public void removeListener(ISdkListener listener) {
        mWindow.removeListener(listener);
    }
    public void open() {
        mWindow.open();
    }
}

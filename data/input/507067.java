public class QsbApplicationWrapper extends Application {
    private QsbApplication mApp;
    @Override
    public void onTerminate() {
        synchronized (this) {
            if (mApp != null) {
                mApp.close();
            }
        }
        super.onTerminate();
    }
    public synchronized QsbApplication getApp() {
        if (mApp == null) {
            mApp = createQsbApplication();
        }
        return mApp;
    }
    protected QsbApplication createQsbApplication() {
        return new QsbApplication(this);
    }
}

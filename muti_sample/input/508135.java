public class MockApplication extends Application {
    public MockApplication() {
    }
    @Override
    public void onCreate() {
        throw new UnsupportedOperationException();
    }
    @Override
    public void onTerminate() {
        throw new UnsupportedOperationException();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        throw new UnsupportedOperationException();
    }
}

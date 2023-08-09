public class Application extends ContextWrapper implements ComponentCallbacks {
    public Application() {
        super(null);
    }
    public void onCreate() {
    }
    public void onTerminate() {
    }
    public void onConfigurationChanged(Configuration newConfig) {
    }
    public void onLowMemory() {
    }
     final void attach(Context context) {
        attachBaseContext(context);
    }
}

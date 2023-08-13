public class ApiDemosApplication extends Application {
    @Override
    public void onCreate() {
        PreferenceManager.setDefaultValues(this, R.xml.default_values, false);
    }
    @Override
    public void onTerminate() {
    }
}

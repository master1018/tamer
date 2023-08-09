public class SettingsLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "SettingsLaunchPerformance";
    public SettingsLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.settings.Settings");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

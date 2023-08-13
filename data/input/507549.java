public class SimpleActivityLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "SimpleActivityLaunchPerformance";
    public SimpleActivityLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getContext(), "com.android.launchperf.SimpleActivity");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

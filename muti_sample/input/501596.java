public class EmptyActivityLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "EmptyActivityLaunchPerformance";
    public EmptyActivityLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getContext(), "com.android.launchperf.EmptyActivity");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

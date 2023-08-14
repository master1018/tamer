public class ComplexActivityLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "ComplexActivityLaunchPerformance";
    public ComplexActivityLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getContext(), "com.android.launchperf.ComplexActivity");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

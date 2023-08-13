public class PhoneLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "PhoneLaunchPerformance";
    public PhoneLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.phone.CallLogList");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

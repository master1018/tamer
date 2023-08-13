public class BrowserLaunchPerformance extends LaunchPerformanceBase {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.browser.BrowserActivity");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

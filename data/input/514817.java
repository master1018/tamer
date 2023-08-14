public class HelloWorldLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "HelloWorldLaunchPerformance";
    public HelloWorldLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.example.android.apis.app.HelloWorld");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

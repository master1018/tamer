public class CalculatorLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "CalculatorLaunchPerformance";
    public CalculatorLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.calculator2.Calculator");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

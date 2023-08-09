public class DeskClockLaunchPerformance extends LaunchPerformanceBase {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.deskclock.DeskClock");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

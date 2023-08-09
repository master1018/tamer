public class MmsLaunchPerformance extends LaunchPerformanceBase {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setPackage(getTargetContext().getPackageName());
        mIntent.setAction(Intent.ACTION_MAIN);
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

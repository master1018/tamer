public class VoiceDialerLaunchPerformance extends LaunchPerformanceBase {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClass(getTargetContext(), VoiceDialerActivity.class);
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

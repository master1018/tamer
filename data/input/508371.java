public class CameraLaunchPerformance extends LaunchPerformanceBase {
    @SuppressWarnings("unused")
    private static final String TAG = "CameraLaunchPerformance";
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.camera.Camera");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}

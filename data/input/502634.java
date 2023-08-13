public class InstrumentationRunner extends Instrumentation {
    @Override
    public void onCreate(Bundle arguments) {
        start();
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onStart() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(getTargetContext(),
                "android.tests.sigtest.SignatureTestActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SignatureTestActivity activity = (SignatureTestActivity) startActivitySync(intent);
        waitForIdleSync();
        finish(Activity.RESULT_OK, activity.mBundle);
    }
}

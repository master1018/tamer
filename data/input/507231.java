public class SmokeTestActivity extends LauncherActivity {
    @Override
    protected Intent getTargetIntent() {
        Intent targetIntent = new Intent(Intent.ACTION_MAIN, null);
        targetIntent.addCategory("android.intent.category.SMOKETEST_INSTRUMENTATION_TEST");
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return targetIntent;
    }
}

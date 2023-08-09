public class LaunchPerformanceBase extends Instrumentation {
    public static final String LOG_TAG = "Launch Performance";
    protected Bundle mResults;
    protected Intent mIntent;
    public LaunchPerformanceBase() {
        mResults = new Bundle();
        mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setAutomaticPerformanceSnapshots();
    }
    protected void LaunchApp() {
        startActivitySync(mIntent);
        waitForIdleSync();
    }
}

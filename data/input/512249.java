public class ReferenceAppTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
    private static final int SNAPSHOT_TIMEOUT_MS = 1000;
    private static final int DEFAULT_MAX_STATUP_TIME_MS = 5000;
    private int maxStartupTimeMs;
    public ReferenceAppTestCase(String pkg, Class<T> activityClass, int maxStartupTimeMs) {
        super(pkg, activityClass);
        this.maxStartupTimeMs = maxStartupTimeMs;
    }
    public ReferenceAppTestCase(String pkg, Class<T> activityClass) {
       this(pkg, activityClass, DEFAULT_MAX_STATUP_TIME_MS);
    }
    public void testActivityStartupTime() {
        long start = System.currentTimeMillis();
        Activity activity = getActivity();
        long end = System.currentTimeMillis();
        long startupTime = end - start;
        assertTrue("Activity Startup took more than " + maxStartupTimeMs +
                   " ms",
                   startupTime <= maxStartupTimeMs);
    }
    public void takeSnapshot(String name) {
        Log.d("ReferenceAppTestCase", "takeSnapshot:" + name);
        try {
            Thread.sleep(SNAPSHOT_TIMEOUT_MS);
        } catch (InterruptedException e) {
        }
    }
}

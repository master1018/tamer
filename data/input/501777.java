@TestTargetClass(Instrumentation.ActivityMonitor.class)
public class Instrumentation_ActivityMonitorTest extends InstrumentationTestCase {
    private static final long WAIT_TIMEOUT = 100;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Instrumentation.ActivityMonitor",
            args = {IntentFilter.class, ActivityResult.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Instrumentation.ActivityMonitor",
            args = {String.class, ActivityResult.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getResult",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBlocking",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "waitForActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHits",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "waitForActivityWithTimeout",
            args = {long.class}
        )
    })
    public void testActivityMonitor() throws Exception {
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, new Intent());
        Instrumentation instrumentation = getInstrumentation();
        ActivityMonitor am = instrumentation.addMonitor(
                InstrumentationTestActivity.class.getName(), result, false);
        Context context = instrumentation.getTargetContext();
        Intent intent = new Intent(context, InstrumentationTestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Activity lastActivity = am.getLastActivity();
        final long TIMEOUT_MSEC = 5000;
        long timeout = System.currentTimeMillis() + TIMEOUT_MSEC;
        while (lastActivity == null && System.currentTimeMillis() < timeout) {
            Thread.sleep(WAIT_TIMEOUT);
            lastActivity = am.getLastActivity();
        }
        Activity activity = am.waitForActivity();
        assertSame(activity, lastActivity);
        assertEquals(1, am.getHits());
        assertTrue(activity instanceof InstrumentationTestActivity);
        activity.finish();
        instrumentation.waitForIdleSync();
        context.startActivity(intent);
        timeout = System.currentTimeMillis() + TIMEOUT_MSEC;
        activity = null;
        while (activity == null && System.currentTimeMillis() < timeout) {
            Thread.sleep(WAIT_TIMEOUT);
            activity = am.waitForActivityWithTimeout(WAIT_TIMEOUT);
        }
        assertNotNull(activity);
        activity.finish();
        instrumentation.removeMonitor(am);
        am = new ActivityMonitor(InstrumentationTestActivity.class.getName(), result, true);
        assertSame(result, am.getResult());
        assertTrue(am.isBlocking());
        IntentFilter which = new IntentFilter();
        am = new ActivityMonitor(which, result, false);
        assertSame(which, am.getFilter());
        assertFalse(am.isBlocking());
    }
}

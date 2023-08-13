public class MultiAppStartupTest extends InstrumentationTestCase {
    private static final String PACKAGE_UNDER_TEST = "com.android.calculator2";
    private static final String ACTIVITY_UNDER_TEST = "Calculator";
    private static final int ACTIVITY_STARTUP_WAIT_TIME = 1000;
    private Intent buildIntent(final String pkgName, String className, boolean isMain) {
        final String fullClassName = pkgName + "." + className;
        Intent intent = new Intent();
        intent.setClassName(pkgName, fullClassName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isMain) {
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        }
        return intent;
    }
    private void launchActivity(final String pkgName, String className, boolean isMain) {
        Context ctx = getInstrumentation().getContext();
        ctx.startActivity(buildIntent(pkgName, className, isMain));
    }
    private long launchActivityUnderTest() {
        long start = System.currentTimeMillis();
        Intent i = buildIntent(PACKAGE_UNDER_TEST,
                               ACTIVITY_UNDER_TEST,
                               true);
        Activity a = getInstrumentation().startActivitySync(i);
        long end = System.currentTimeMillis();
        long diff = end - start;
        a.finish();
        return diff;
    }
    public void testMultipleApps() throws InterruptedException {
        long initialStartDuration =  launchActivityUnderTest();
        launchActivity(PACKAGE_UNDER_TEST,
                       ACTIVITY_UNDER_TEST,
                       true);
        launchActivity("com.android.browser", "BrowserActivity", true);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.mms", "ui.ConversationList", true);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.alarmclock", "AlarmClock", true);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.contacts", "TwelveKeyDialer", false);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.contacts", "RecentCallsListActivity", false);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.calendar", "LaunchActivity", true);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        long finalStartDuration = launchActivityUnderTest();
        assertTrue("Restart of inital app took to long: " +
                   finalStartDuration + " " + initialStartDuration,
                   finalStartDuration < initialStartDuration);
    }
}

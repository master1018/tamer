@TestTargetClass(Application.class)
public class ApplicationTest extends InstrumentationTestCase {
    @TestTargets({
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Application",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onConfigurationChanged",
        args = {android.content.res.Configuration.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onCreate",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "According to issue 1653192, a Java app can't allocate memory without" +
                " restriction, thus it's hard to test this callback.",
        method = "onLowMemory",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "The documentation states that one cannot rely on this method being called.",
        method = "onTerminate",
        args = {}
      )
    })
    public void testApplication() throws Throwable {
        final Instrumentation instrumentation = getInstrumentation();
        final Context targetContext = instrumentation.getTargetContext();
        final Intent intent = new Intent(targetContext, MockApplicationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Activity activity = instrumentation.startActivitySync(intent);
        final MockApplication mockApp = (MockApplication) activity.getApplication();
        assertTrue(mockApp.isConstructorCalled);
        assertTrue(mockApp.isOnCreateCalled);
        runTestOnUiThread(new Runnable() {
            public void run() {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockApp.isOnConfigurationChangedCalled);
    }
}

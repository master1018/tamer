@TestTargetClass(LauncherActivity.class)
public class LauncherActivityTest extends InstrumentationTestCase {
    private Instrumentation mInstrumentation;
    private LauncherActivityStub mActivity;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            if (!mActivity.isFinishing()) {
                mActivity.finish();
            }
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LauncherActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTargetIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "makeListItems",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onListItemClick",
            args = {android.widget.ListView.class, android.view.View.class, int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "intentForPosition",
            args = {int.class}
        )
    })
    @BrokenTest("flaky test, assertTrue(mActivity.isOnListItemClick) intermittently fails")
    public void testLaunchActivity() {
        new LauncherActivityStub();
        final String packageName = "com.android.cts.stub";
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(packageName, LauncherActivityStub.class.getName());
        mActivity = (LauncherActivityStub) mInstrumentation.startActivitySync(intent);
        assertTrue(mActivity.isOnCreateCalled);
        assertNotNull(mActivity.getSuperIntent());
        final List<ListItem> list = mActivity.makeListItems();
        assertTrue(list.size() > 0);
        assertNotNull(mActivity.intentForPosition(0));
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(mActivity.isOnListItemClick);
    }
}

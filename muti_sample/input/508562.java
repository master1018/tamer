@TestTargetClass(TabActivity.class)
public class TabActivityTest extends InstrumentationTestCase {
    private Instrumentation mInstrumentation;
    private MockTabActivity mActivity;
    private Activity mChildActivity;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = super.getInstrumentation();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            if (!mActivity.isFinishing()) {
                mActivity.finish();
            } else if (mChildActivity != null) {
                if (!mChildActivity.isFinishing()) {
                    mChildActivity.finish();
                }
            }
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TabActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultTab",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultTab",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTabHost",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTabWidget",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {android.os.Bundle.class}
        )
    })
    @ToBeFixed(bug = "1701364", explanation = "When testing TabActivity#setDefaultTab(int index),"
            + " setDefaultTab(String tag), we find that the set values are hard to get, there"
            + " is no proper method or other way to obtain these two default values.")
    public void testTabActivity() throws Throwable {
        new TabActivity();
        final String packageName = "com.android.cts.stub";
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(packageName, MockTabActivity.class.getName());
        mActivity = (MockTabActivity) mInstrumentation.startActivitySync(intent);
        assertTrue(mActivity.isOnContentChangedCalled);
        assertTrue(mActivity.isOnPostCreateCalled);
        final int defaultIndex = 1;
        mActivity.setDefaultTab(defaultIndex);
        final String defaultTab = "DefaultTab";
        mActivity.setDefaultTab(defaultTab);
        final TabHost tabHost = mActivity.getTabHost();
        assertNotNull(tabHost);
        assertNotNull(tabHost.getTabWidget());
        assertFalse(mActivity.isOnSaveInstanceStateCalled);
        final Intent embedded = new Intent(mInstrumentation.getTargetContext(),
                ChildTabActivity.class);
        mActivity.startActivity(embedded);
        mInstrumentation.waitForIdleSync();
        assertTrue(mActivity.isOnSaveInstanceStateCalled);
        sendKeys(KeyEvent.KEYCODE_BACK);
        mInstrumentation.waitForIdleSync();
        assertFalse(MockTabActivity.isOnRestoreInstanceStateCalled);
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mInstrumentation.waitForIdleSync();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mInstrumentation.waitForIdleSync();
        assertTrue(MockTabActivity.isOnRestoreInstanceStateCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onChildTitleChanged",
        args = {android.app.Activity.class, java.lang.CharSequence.class}
    )
    public void testChildTitleCallback() throws Exception {
        final Context context = mInstrumentation.getTargetContext();
        final Intent intent = new Intent(context, MockTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final MockTabActivity father = new MockTabActivity();
        final ComponentName componentName = new ComponentName(context, MockTabActivity.class);
        final ActivityInfo info = context.getPackageManager().getActivityInfo(componentName, 0);
        mChildActivity = mInstrumentation.newActivity(MockTabActivity.class, mInstrumentation
                .getTargetContext(), null, null, intent, info, MockTabActivity.class.getName(),
                father, null, null);
        assertNotNull(mChildActivity);
        final String newTitle = "New Title";
        mChildActivity.setTitle(newTitle);
        assertTrue(father.isOnChildTitleChangedCalled);
    }
}

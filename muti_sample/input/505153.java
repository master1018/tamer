@TestTargetClass(Activity.class)
public class NoActivityRelatedPermissionTest
        extends ActivityInstrumentationTestCase2<PermissionStubActivity> {
    private PermissionStubActivity mActivity;
    public NoActivityRelatedPermissionTest() {
        super("com.android.cts.permission", PermissionStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @UiThreadTest
    @MediumTest
    @Suppress
    @BrokenTest("This test passes, but crashes the UI thread later on. See issues 1909470, 1910487")
    public void testSystemAlertWindow() {
        final int[] types = new int[] {
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            };
        AlertDialog dialog = (AlertDialog) (mActivity.getDialog());
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        dialog.show();
        for (int i = 0; i < types.length; i++) {
            dialog = (AlertDialog) (mActivity.getDialog());
            dialog.getWindow().setType(types[i]);
            try {
                dialog.show();
                fail("Add dialog to Window Manager did not throw BadTokenException as expected");
            } catch (BadTokenException e) {
            }
        }
    }
    @UiThreadTest
    @MediumTest
    public void testSetPersistent() {
        try {
            mActivity.setPersistent(true);
            fail("Activity.setPersistent() did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @MediumTest
    public void testGetTask() {
        ActivityManager manager = (ActivityManager) getActivity()
                .getSystemService(Context.ACTIVITY_SERVICE);
        try {
            manager.getRunningTasks(1);
            fail("Activity.getRunningTasks did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
        try {
            manager.getRecentTasks(1, 0);
            fail("Activity.getRunningTasks did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
}

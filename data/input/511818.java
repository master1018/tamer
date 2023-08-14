@TestTargetClass(LauncherActivity.IconResizer.class)
public class LauncherActivity_IconResizerTest extends
        ActivityInstrumentationTestCase2<LauncherActivityStub> {
    private static final String PACKAGE = "com.android.cts.stub";
    private LauncherActivityStub mActivity;
    public LauncherActivity_IconResizerTest() {
        super(PACKAGE, LauncherActivityStub.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createIconThumbnail",
            args = {Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LauncherActivity.IconResizer",
            args = {}
        )
    })
    public void testIconResizer() throws Throwable {
        final IconResizer ir = mActivity.new IconResizer();
        final Drawable d = mActivity.getResources().getDrawable(R.drawable.pass);
        assertNotNull(d);
        runTestOnUiThread(new Runnable() {
            public void run() {
                Drawable thumbNail = ir.createIconThumbnail(d);
                assertNotNull(thumbNail);
                assertTrue(thumbNail.getIntrinsicHeight() > 0);
                assertTrue(thumbNail.getIntrinsicWidth() > 0);
            }
        });
        getInstrumentation().waitForIdleSync();
    }
}

@TestTargetClass(ViewFlipper.class)
public class ViewFlipperTest extends ActivityInstrumentationTestCase<ViewFlipperStubActivity> {
    private Activity mActivity;
    public ViewFlipperTest() {
        super("com.android.cts.stub", ViewFlipperStubActivity.class);
    }
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        assertNotNull(mActivity);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewFlipper}",
            method = "ViewFlipper",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewFlipper}",
            method = "ViewFlipper",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="ViewFlipper#ViewFlipper(Context, AttributeSet)" +
            " should check whether the input Context is null")
    public void testConstructor() {
        new ViewFlipper(mActivity);
        new ViewFlipper(mActivity, null);
        XmlPullParser parser = mActivity.getResources().getXml(R.layout.viewflipper_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new ViewFlipper(mActivity, attrs);
        try {
            new ViewFlipper(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ViewFlipper#setFlipInterval(int)}",
        method = "setFlipInterval",
        args = {int.class}
    )
    @ToBeFixed(bug="1386429", explanation="No getter and can't check indirectly")
    public void testSetFlipInterval() {
        ViewFlipper viewFlipper = new ViewFlipper(mActivity);
        viewFlipper.setFlipInterval(0);
        viewFlipper.setFlipInterval(-1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startFlipping",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopFlipping",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFlipping",
            args = {}
        )
    })
    public void testViewFlipper() {
        ViewFlipper viewFlipper = (ViewFlipper) mActivity.findViewById(R.id.viewflipper_test);
        final int FLIP_INTERVAL = 1000;
        TextView iv1 = (TextView) mActivity.findViewById(R.id.viewflipper_textview1);
        TextView iv2 = (TextView) mActivity.findViewById(R.id.viewflipper_textview2);
        assertFalse(viewFlipper.isFlipping());
        assertSame(iv1, viewFlipper.getCurrentView());
        viewFlipper.startFlipping();
        assertTrue(viewFlipper.isFlipping());
        assertSame(iv1, viewFlipper.getCurrentView());
        assertEquals(View.VISIBLE, iv1.getVisibility());
        assertEquals(View.GONE, iv2.getVisibility());
        waitForViewFlipping(FLIP_INTERVAL + 200);
        assertSame(iv2, viewFlipper.getCurrentView());
        assertEquals(View.GONE, iv1.getVisibility());
        assertEquals(View.VISIBLE, iv2.getVisibility());
        waitForViewFlipping(FLIP_INTERVAL + 200);
        assertSame(iv1, viewFlipper.getCurrentView());
        assertEquals(View.VISIBLE, iv1.getVisibility());
        assertEquals(View.GONE, iv2.getVisibility());
        viewFlipper.stopFlipping();
        assertFalse(viewFlipper.isFlipping());
    }
    private void waitForViewFlipping(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}

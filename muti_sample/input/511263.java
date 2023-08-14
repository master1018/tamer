public class DisabledLongpressTest extends ActivityInstrumentationTestCase<Longpress> {
    private View mSimpleView;
    private boolean mLongClicked;
    public DisabledLongpressTest() {
        super("com.android.frameworks.coretests", Longpress.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Longpress a = getActivity();
        mSimpleView = a.findViewById(R.id.simple_view);
        mSimpleView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                mLongClicked = true;
                return true;
            }
        });
        mSimpleView.setLongClickable(false);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mLongClicked = false;
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mSimpleView);
        assertTrue(mSimpleView.hasFocus());
        assertFalse(mLongClicked);
    }
    @LargeTest
    public void testKeypadLongClick() throws Exception {
        mSimpleView.requestFocus();
        getInstrumentation().waitForIdleSync();
        KeyUtils.longClick(this);
        getInstrumentation().waitForIdleSync();
        assertFalse(mLongClicked);
    }
    @LargeTest
    public void testTouchLongClick() throws Exception {
        TouchUtils.longClickView(this, mSimpleView);
        getInstrumentation().waitForIdleSync();
        assertFalse(mLongClicked);
    }
}

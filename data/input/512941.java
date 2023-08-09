public class LongpressTest extends ActivityInstrumentationTestCase<Longpress> {
    private View mSimpleView;
    private boolean mLongClicked;
    public LongpressTest() {
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
        assertTrue(mLongClicked);
    }
    @LargeTest
    public void testTouchLongClick() throws Exception {
        TouchUtils.longClickView(this, mSimpleView);
        getInstrumentation().waitForIdleSync();
        assertTrue(mLongClicked);
    }
}

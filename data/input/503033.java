public class DisabledTest extends ActivityInstrumentationTestCase<Disabled> {
    private Button mDisabled;
    private View mDisabledParent;
    private boolean mClicked;
    private boolean mParentClicked;
    public DisabledTest() {
        super("com.android.frameworks.coretests", Disabled.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Disabled a = getActivity();
        mDisabled = (Button) a.findViewById(R.id.disabledButton);
        mDisabled.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mClicked = true;
            }
        });
        mDisabledParent = a.findViewById(R.id.clickableParent);
        mDisabledParent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mParentClicked = true;
            }
        });
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mClicked = false;
        mParentClicked = false;
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mDisabled);
        assertNotNull(mDisabledParent);
        assertFalse(mDisabled.isEnabled());
        assertTrue(mDisabledParent.isEnabled());
        assertTrue(mDisabled.hasFocus());
    }
    @MediumTest
    public void testKeypadClick() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertFalse(mClicked);
        assertFalse(mParentClicked);
    }
    @LargeTest
    public void testTouchClick() throws Exception {
        TouchUtils.clickView(this, mDisabled);
        getInstrumentation().waitForIdleSync();
        assertFalse(mClicked);
        assertFalse(mParentClicked);
    }
}

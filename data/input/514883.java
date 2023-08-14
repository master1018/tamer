public class FocusAfterRemovalTest extends ActivityInstrumentationTestCase<FocusAfterRemoval> {
    private LinearLayout mLeftLayout;
    private Button mTopLeftButton;
    private Button mBottomLeftButton;
    private Button mTopRightButton;
    private Button mBottomRightButton;
    public FocusAfterRemovalTest() {
        super("com.android.frameworks.coretests", FocusAfterRemoval.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final FocusAfterRemoval a = getActivity();
        mLeftLayout = (LinearLayout) a.findViewById(R.id.leftLayout);
        mTopLeftButton = (Button) a.findViewById(R.id.topLeftButton);
        mBottomLeftButton = (Button) a.findViewById(R.id.bottomLeftButton);
        mTopRightButton = (Button) a.findViewById(R.id.topRightButton);
        mBottomRightButton = (Button) a.findViewById(R.id.bottomRightButton);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mLeftLayout);
        assertNotNull(mTopLeftButton);
        assertNotNull(mTopRightButton);
        assertNotNull(mBottomLeftButton);
        assertNotNull(mBottomRightButton);
        assertTrue(mTopLeftButton.hasFocus());
    }
    @MediumTest
    public void testFocusLeavesWhenParentLayoutIsGone() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals(View.GONE, mLeftLayout.getVisibility());
        assertTrue("focus should jump to visible button",
                mTopRightButton.hasFocus());
    }
    @MediumTest
    public void testFocusLeavesWhenParentLayoutInvisible() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue(mBottomLeftButton.hasFocus());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals(View.INVISIBLE,
                getActivity().findViewById(R.id.leftLayout).getVisibility());
        assertTrue("focus should jump to visible button",
                mTopRightButton.hasFocus());
    }
    @MediumTest
    public void testFocusLeavesWhenFocusedViewBecomesGone() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertTrue(mTopRightButton.hasFocus());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals(View.GONE, mTopRightButton.getVisibility());
        assertTrue("focus should jump to visible button",
                mTopLeftButton.hasFocus());
    }
    @MediumTest
    public void testFocusLeavesWhenFocusedViewBecomesInvisible() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue(mBottomRightButton.hasFocus());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals(View.INVISIBLE, mBottomRightButton.getVisibility());
        assertTrue("focus should jump to visible button",
                mTopLeftButton.hasFocus());
    }
}

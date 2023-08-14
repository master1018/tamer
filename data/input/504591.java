public class AdjacentListsWithAdjacentISVsInsideTest extends ActivityInstrumentationTestCase<AdjacentListsWithAdjacentISVsInside> {
    private ListView mLeftListView;
    private InternalSelectionView mLeftIsv;
    private InternalSelectionView mLeftMiddleIsv;
    private ListView mRightListView;
    private InternalSelectionView mRightMiddleIsv;
    private InternalSelectionView mRightIsv;
    public AdjacentListsWithAdjacentISVsInsideTest() {
        super("com.android.frameworks.coretests", AdjacentListsWithAdjacentISVsInside.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final AdjacentListsWithAdjacentISVsInside a = getActivity();
        mLeftListView = a.getLeftListView();
        mLeftIsv = a.getLeftIsv();
        mLeftMiddleIsv = a.getLeftMiddleIsv();
        mRightListView = a.getRightListView();
        mRightMiddleIsv = a.getRightMiddleIsv();
        mRightIsv = a.getRightIsv();
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue(mLeftListView.hasFocus());
        assertTrue(mLeftIsv.isFocused());
        assertEquals(0, mLeftIsv.getSelectedRow());
    }
    @MediumTest
    public void testFocusedRectAndFocusHintWorkWithinListItemHorizontal() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(1, mLeftIsv.getSelectedRow());
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertTrue(mLeftListView.hasFocus());
        assertTrue(mLeftMiddleIsv.isFocused());
        assertEquals("mLeftMiddleIsv.getSelectedRow()", 1, mLeftMiddleIsv.getSelectedRow());
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        assertTrue(mLeftIsv.isFocused());
        assertEquals("mLeftIsv.getSelectedRow()", 1, mLeftIsv.getSelectedRow());
    }
    @MediumTest
    public void testFocusTransfersOutsideOfListWhenNoCandidateInsideHorizontal() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT);
        assertTrue(mLeftListView.hasFocus());
        assertTrue(mLeftMiddleIsv.isFocused());
        assertEquals(2, mLeftMiddleIsv.getSelectedRow());
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertTrue("mRightListView.hasFocus()", mRightListView.hasFocus());
        assertTrue("mRightMiddleIsv.isFocused()", mRightMiddleIsv.isFocused());
        assertEquals("mRightMiddleIsv.getSelectedRow()", 2, mRightMiddleIsv.getSelectedRow());  
    }    
}

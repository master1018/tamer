public class ListButtonsDiagonalAcrossItemsTest extends ActivityInstrumentationTestCase<ListButtonsDiagonalAcrossItems> {
    private Button mLeftButton;
    private Button mCenterButton;
    private Button mRightButton;
    private ListView mListView;
    public ListButtonsDiagonalAcrossItemsTest() {
        super("com.android.frameworks.coretests", ListButtonsDiagonalAcrossItems.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLeftButton = getActivity().getLeftButton();
        mCenterButton = getActivity().getCenterButton();
        mRightButton = getActivity().getRightButton();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        final ListView lv = mListView;
        assertEquals("num children", 3, lv.getChildCount());
        assertEquals("selected position", 0, lv.getSelectedItemPosition());
        assertTrue("left button focused", mLeftButton.isFocused());
        assertTrue("left left of center",
                mLeftButton.getRight()
                        < mCenterButton.getLeft());
        assertTrue("center left of right",
                mCenterButton.getRight()
                        < mRightButton.getLeft());
        assertEquals("focus search right from left button should be center button",
            mCenterButton,
            FocusFinder.getInstance().findNextFocus(mListView, mLeftButton, View.FOCUS_RIGHT));
        assertEquals("focus search right from center button should be right button",
            mRightButton,
            FocusFinder.getInstance().findNextFocus(mListView, mCenterButton, View.FOCUS_RIGHT));
        assertEquals("focus search left from centr button should be left button",
            mLeftButton,
            FocusFinder.getInstance().findNextFocus(mListView, mCenterButton, View.FOCUS_LEFT));
    }
    @MediumTest
    public void testGoingRightDoesNotChangeSelection() {
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertEquals("selected position shouldn't have changed",
                0,
                mListView.getSelectedItemPosition());
        assertTrue("left should still be focused", mLeftButton.isFocused());
    }
    @MediumTest
    public void testGoingLeftDoesNotChangeSelection() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("list view postion", 1, mListView.getSelectedItemPosition());
        assertTrue("mCenterButton.isFocused()", mCenterButton.isFocused());
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        assertEquals("selected position shouldn't have changed",
                1,
                mListView.getSelectedItemPosition());
        assertTrue("center should still be focused", mCenterButton.isFocused());
    }
}

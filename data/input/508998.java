public class ListInterleaveFocusablesTest extends ActivityInstrumentationTestCase<ListInterleaveFocusables> {
    private ListView mListView;
    private ListUtil mListUtil;
    public ListInterleaveFocusablesTest() {
        super("com.android.frameworks.coretests", ListInterleaveFocusables.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
        mListUtil = new ListUtil(mListView, getInstrumentation());
    }
    @LargeTest
    public void testPreconditions() {
        assertEquals(7, mListView.getChildCount());
        assertTrue(mListView.getChildAt(1).isFocusable());
        assertTrue(mListView.getChildAt(3).isFocusable());
        assertTrue(mListView.getChildAt(6).isFocusable());
    }
    @MediumTest
    public void testGoingFromUnFocusableSelectedToFocusable() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selected item position", 1, mListView.getSelectedItemPosition());
        assertSelectedViewFocus(true);
    }
    @MediumTest
    public void testGoingDownFromUnFocusableSelectedToFocusableWithOtherFocusableAbove() {
        mListUtil.setSelectedPosition(2);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selected item position", 3, mListView.getSelectedItemPosition());
        assertSelectedViewFocus(true);
    }
    @MediumTest
    public void testGoingUpFromUnFocusableSelectedToFocusableWithOtherFocusableAbove() {
        mListUtil.setSelectedPosition(2);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("selected item position", 1, mListView.getSelectedItemPosition());
        assertSelectedViewFocus(true);
    }
    @MediumTest
    public void testGoingDownFromFocusableToUnfocusableWhenFocusableIsBelow() {
        mListUtil.setSelectedPosition(3);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selected item position", 4, mListView.getSelectedItemPosition());
        assertSelectedViewFocus(false);
    }
    @MediumTest
    public void testGoingUpFromFocusableToUnfocusableWhenFocusableIsBelow() {
        mListUtil.setSelectedPosition(6);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("selected item position", 5, mListView.getSelectedItemPosition());
        assertSelectedViewFocus(false);
    }
    public void assertSelectedViewFocus(boolean isFocused) {
        final View view = mListView.getSelectedView();
        assertEquals("selected view focused", isFocused, view.isFocused());
        assertEquals("selected position's isSelected should be the inverse "
                + "of it having focus", !isFocused, view.isSelected());
    }
}

public class ListItemFocusableAboveUnfocusableTest extends ActivityInstrumentationTestCase<ListItemFocusableAboveUnfocusable> {
    private ListView mListView;
    public ListItemFocusableAboveUnfocusableTest() {
        super("com.android.frameworks.coretests", ListItemFocusableAboveUnfocusable.class);
    }
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertEquals("selected position", 0, mListView.getSelectedItemPosition());
        assertTrue(mListView.getChildAt(0).isFocused());
        assertFalse(mListView.getChildAt(1).isFocusable());
    }
    @MediumTest
    public void testMovingToUnFocusableTakesFocusAway() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertFalse("focused item should have lost focus",
                mListView.getChildAt(0).isFocused());
        assertEquals("selected position", 1, mListView.getSelectedItemPosition());
    }
}

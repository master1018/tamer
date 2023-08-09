public class ListWithFirstScreenUnSelectableTest
        extends ActivityInstrumentationTestCase2<ListWithFirstScreenUnSelectable> {
    private ListView mListView;
    public ListWithFirstScreenUnSelectableTest() {
        super("com.android.frameworks.coretests", ListWithFirstScreenUnSelectable.class);
    }
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        mListView = getActivity().getListView();
    }
    public void testPreconditions() {
        assertTrue(mListView.isInTouchMode());
        assertEquals(1, mListView.getChildCount());
        assertFalse(mListView.getAdapter().isEnabled(0));
        assertEquals(AdapterView.INVALID_POSITION, mListView.getSelectedItemPosition());
    }
    public void testRessurectSelection() {
        sendKeys(KeyEvent.KEYCODE_SPACE);
        assertEquals(AdapterView.INVALID_POSITION, mListView.getSelectedItemPosition());
    }
    public void testScrollUpDoesNothing() {
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals(AdapterView.INVALID_POSITION, mListView.getSelectedItemPosition());
        assertEquals(1, mListView.getChildCount());
        assertEquals(0, mListView.getFirstVisiblePosition());
    }
    public void testScrollDownPansNextItemOn() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(2, mListView.getChildCount());
    }
}

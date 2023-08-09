public class ListOfButtonsTest extends ActivityInstrumentationTestCase<ListOfButtons> {
    private ListAdapter mListAdapter;
    private Button mButtonAtTop;
    private ListView mListView;
    public ListOfButtonsTest() {
        super("com.android.frameworks.coretests", ListOfButtons.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ListOfButtons a = getActivity();
        mListAdapter = a.getListAdapter();
        mButtonAtTop = (Button) a.findViewById(R.id.button);
        mListView = a.getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mListAdapter);
        assertNotNull(mButtonAtTop);
        assertNotNull(mListView);
        assertFalse(mButtonAtTop.hasFocus());
        assertTrue(mListView.hasFocus());
        assertEquals("expecting 0 index to be selected",
                0, mListView.getSelectedItemPosition());
    }
    @MediumTest
    public void testNavigateToButtonAbove() {
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue(mButtonAtTop.hasFocus());        
        assertFalse(mListView.hasFocus());
    }
    @MediumTest
    public void testNavigateToSecondItem() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue(mListView.hasFocus());
        View childOne = mListView.getChildAt(1);
        assertNotNull(childOne);
        assertEquals(childOne, mListView.getFocusedChild());
        assertTrue(childOne.hasFocus());
    }
    @MediumTest
    public void testNavigateUpAboveAndBackOut() {
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertFalse("button at top should have focus back",
                mButtonAtTop.hasFocus());
        assertTrue(mListView.hasFocus());
    }
    public void TODO_testNavigateThroughAllButtonsAndBack() {
        String[] labels = getActivity().getLabels();
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            getInstrumentation().waitForIdleSync();
            String indexInfo = "index: " + i + ", label: " + label;
            assertTrue(indexInfo, mListView.hasFocus());
            Button button = (Button) mListView.getSelectedView();
            assertNotNull(indexInfo, button);
            assertEquals(indexInfo, label, button.getText().toString());
            assertTrue(indexInfo, button.hasFocus());
        }
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        for (int i = labels.length - 1; i >= 0; i--) {
            String label = labels[i];
            String indexInfo = "index: " + i + ", label: " + label;
            assertTrue(indexInfo, mListView.hasFocus());
            Button button = (Button) mListView.getSelectedView();
            assertNotNull(indexInfo, button);
            assertEquals(indexInfo, label, button.getText().toString());
            assertTrue(indexInfo, button.hasFocus());
            sendKeys(KeyEvent.KEYCODE_DPAD_UP);
            getInstrumentation().waitForIdleSync();
        }
        assertTrue("button at top should have focus back",
                mButtonAtTop.hasFocus());
        assertFalse(mListView.hasFocus());
    }
    @MediumTest
    public void testGoInAndOutOfListWithItemsFocusable() {
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue(mButtonAtTop.hasFocus());
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        final String firstButtonLabel = getActivity().getLabels()[0];
        final Button firstButton = (Button) mListView.getSelectedView();
        assertTrue(firstButton.isFocused());
        assertEquals(firstButtonLabel, firstButton.getText());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue(mButtonAtTop.isFocused());
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue(firstButton.isFocused());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue(mButtonAtTop.isFocused());
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue(firstButton.isFocused());
    }
}

public class ListWithOnItemSelectedActionTest extends ActivityInstrumentationTestCase<ListWithOnItemSelectedAction> {
    private ListView mListView;
    public ListWithOnItemSelectedActionTest() {
        super("com.android.frameworks.coretests", ListWithOnItemSelectedAction.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    private String getValueOfSelectedTextView() {
        return ((TextView) mListView.getSelectedView()).getText().toString();
    }
    @MediumTest
    public void testPreconditions() {
        assertEquals(0, mListView.getSelectedItemPosition());
        assertEquals("header text field should be echoing contents of selected item",
                getValueOfSelectedTextView(),
                getActivity().getHeaderValue());
    }
    @MediumTest
    public void testHeaderEchoesSelectionAfterMove() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(1, mListView.getSelectedItemPosition());
        assertEquals("header text field should be echoing contents of selected item",
                getValueOfSelectedTextView(),
                getActivity().getHeaderValue());
    }
}

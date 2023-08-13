public class AddColumnTest extends ActivityInstrumentationTestCase<AddColumn> {
    private Button mAddRow;
    private TableLayout mTable;
    public AddColumnTest() {
        super("com.android.frameworks.coretests", AddColumn.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final AddColumn activity = getActivity();
        mAddRow = (Button) activity.findViewById(R.id.add_row_button);
        mTable = (TableLayout) activity.findViewById(R.id.table);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mAddRow);
        assertNotNull(mTable);
        assertTrue(mAddRow.hasFocus());
    }
    @MediumTest
    public void testWidths() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        TableRow row1 = (TableRow) mTable.getChildAt(0);
        TableRow row2 = (TableRow) mTable.getChildAt(1);
        assertTrue(row1.getChildCount() < row2.getChildCount());
        for (int i = 0; i < row1.getChildCount(); i++) {
            assertEquals(row2.getChildAt(i).getWidth(), row1.getChildAt(i).getWidth());
        }
    }
}

public class FocusChangeWithInterestingRectHintTest extends ActivityInstrumentationTestCase<AdjacentVerticalRectLists> {
    private InternalSelectionView mLeftColumn;
    private InternalSelectionView mMiddleColumn;
    private InternalSelectionView mRightColumn;
    public FocusChangeWithInterestingRectHintTest() {
        super("com.android.frameworks.coretests", AdjacentVerticalRectLists.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mLeftColumn = getActivity().getLeftColumn();
        mMiddleColumn = getActivity().getMiddleColumn();
        mRightColumn = getActivity().getRightColumn();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mLeftColumn);
        assertNotNull(mMiddleColumn);
        assertNotNull(mRightColumn);
        assertTrue(mLeftColumn.hasFocus());
        assertTrue("need at least 3 rows", mLeftColumn.getNumRows() > 2);
        assertEquals(mLeftColumn.getNumRows(), mMiddleColumn.getNumRows());
        assertEquals(mMiddleColumn.getNumRows(), mRightColumn.getNumRows());
    }
    @LargeTest
    public void testSnakeBackAndForth() {
        final int numRows = mLeftColumn.getNumRows();
        for (int row = 0; row < numRows; row++) {
            if ((row % 2) == 0) {
                assertEquals("row " + row + ": should be at left column",
                        row, mLeftColumn.getSelectedRow());
                sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
                assertTrue("row " + row + ": should be at middle column",
                        mMiddleColumn.hasFocus());
                assertEquals(row, mMiddleColumn.getSelectedRow());
                sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
                assertTrue("row " + row + ": should be at right column",
                        mRightColumn.hasFocus());
                assertEquals(row, mRightColumn.getSelectedRow());
                if (row < numRows - 1) {
                    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
                    assertEquals(row + 1, mRightColumn.getSelectedRow());
                }
            } else {
                assertTrue("row " + row + ": should be at right column",
                        mRightColumn.hasFocus());
                sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
                assertTrue("row " + row + ": should be at middle column",
                        mMiddleColumn.hasFocus());
                assertEquals(row, mMiddleColumn.getSelectedRow());
                sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
                assertEquals("row " + row + ": should be at left column",
                        row, mLeftColumn.getSelectedRow());
                if (row < numRows - 1) {
                    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
                    assertEquals(row + 1, mLeftColumn.getSelectedRow());
                }
           }
        }
    }
}

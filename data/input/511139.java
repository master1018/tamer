public class GridPaddingTest extends ActivityInstrumentationTestCase2<GridPadding> {
    private GridView mGridView;
    public GridPaddingTest() {
        super("com.android.frameworks.coretests", GridPadding.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        mGridView = getActivity().getGridView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mGridView);
        assertTrue("Not in touch mode", mGridView.isInTouchMode());
    }
    @MediumTest
    public void testResurrectSelection() {
        sendKeys("DPAD_DOWN");
        assertEquals("The first item should be selected", mGridView.getSelectedItemPosition(), 0);
    }
}

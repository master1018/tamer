public class ExpandableListWithHeadersTest extends
        ActivityInstrumentationTestCase2<ExpandableListWithHeaders> {
    private ExpandableListView mExpandableListView;
    private ListUtil mListUtil;
    public ExpandableListWithHeadersTest() {
        super(ExpandableListWithHeaders.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mExpandableListView = getActivity().getExpandableListView();
        mListUtil = new ListUtil(mExpandableListView, getInstrumentation());
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mExpandableListView);
    }
    @MediumTest
    public void testExpandOnFirstPosition() {
        mListUtil.arrowScrollToSelectedPosition(0);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertFalse(mExpandableListView.isGroupExpanded(0));
    }
    @LargeTest
    public void testExpandOnFirstGroup() {
        mListUtil.arrowScrollToSelectedPosition(getActivity().getNumOfHeadersAndFooters());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertTrue(mExpandableListView.isGroupExpanded(0));
    }
    @MediumTest
    public void testContextMenus() {
        ExpandableListTester tester = new ExpandableListTester(mExpandableListView, this);
        tester.testContextMenus();
    }
    @MediumTest
    public void testConvertionBetweenFlatAndPacked() {
        ExpandableListTester tester = new ExpandableListTester(mExpandableListView, this);
        tester.testConvertionBetweenFlatAndPackedOnGroups();
        tester.testConvertionBetweenFlatAndPackedOnChildren();
    }
    @MediumTest
    public void testSelectedPosition() {
        ExpandableListTester tester = new ExpandableListTester(mExpandableListView, this);
        tester.testSelectedPositionOnGroups();
        tester.testSelectedPositionOnChildren();
    }
}

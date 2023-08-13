@TestTargetClass(ExpandableListView.class)
public class ExpandableListViewWithHeadersTest extends
        ActivityInstrumentationTestCase2<ExpandableListWithHeaders> {
    private ExpandableListView mExpandableListView;
    private ListUtil mListUtil;
    public ExpandableListViewWithHeadersTest() {
        super("com.android.cts.stub", ExpandableListWithHeaders.class);
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ExpandableListView#expandGroup(int)}",
        method = "expandGroup",
        args = {int.class}
    )
    @MediumTest
    public void testExpandOnFirstPosition() {
        mListUtil.arrowScrollToSelectedPosition(0);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertFalse(mExpandableListView.isGroupExpanded(0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ExpandableListView#expandGroup(int)}",
        method = "expandGroup",
        args = {int.class}
    )
    @LargeTest
    public void testExpandOnFirstGroup() {
        mListUtil.arrowScrollToSelectedPosition(getActivity().getNumOfHeadersAndFooters());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertTrue(mExpandableListView.isGroupExpanded(0));
    }
}

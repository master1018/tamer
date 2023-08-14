public class ExpandableListBasicTest extends ActivityInstrumentationTestCase2<ExpandableListSimple> {
    private ExpandableListScenario mActivity;
    private ExpandableListView mExpandableListView;
    private ExpandableListAdapter mAdapter;
    private ListUtil mListUtil;
    public ExpandableListBasicTest() {
        super(ExpandableListSimple.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mExpandableListView = mActivity.getExpandableListView();
        mAdapter = mExpandableListView.getExpandableListAdapter();
        mListUtil = new ListUtil(mExpandableListView, getInstrumentation());
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mExpandableListView);
    }
    private int expandGroup(int numChildren, boolean atLeastOneChild) {
        final int groupPos = mActivity.findGroupWithNumChildren(numChildren, atLeastOneChild);
        assertTrue("Could not find group to expand", groupPos >= 0);
        assertFalse("Group is already expanded", mExpandableListView.isGroupExpanded(groupPos));
        mListUtil.arrowScrollToSelectedPosition(groupPos);
        getInstrumentation().waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertTrue("Group did not expand", mExpandableListView.isGroupExpanded(groupPos));
        return groupPos;
    }
    @MediumTest
    public void testExpandGroup() {
        expandGroup(-1, true);
    }
    @MediumTest
    public void testCollapseGroup() {
        final int groupPos = expandGroup(-1, true);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertFalse("Group did not collapse", mExpandableListView.isGroupExpanded(groupPos));
    }
    @MediumTest
    public void testExpandedGroupMovement() {
        mListUtil.arrowScrollToSelectedPosition(0);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertTrue("Group did not expand", mExpandableListView.isGroupExpanded(0));
        getInstrumentation().waitForIdleSync();
        assertTrue("Group did not expand", mExpandableListView.isGroupExpanded(0));
        List<MyGroup> groups = mActivity.getGroups();
        MyGroup insertedGroup = new MyGroup(1);
        groups.add(0, insertedGroup);
        assertTrue("Adapter is not an instance of the base adapter",
                mAdapter instanceof BaseExpandableListAdapter);
        final BaseExpandableListAdapter adapter = (BaseExpandableListAdapter) mAdapter;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue("The expanded state didn't stay with the proper group",
                mExpandableListView.isGroupExpanded(1));
        assertFalse("The expanded state was given to the inserted group",
                mExpandableListView.isGroupExpanded(0));
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

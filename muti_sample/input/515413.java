@TestTargetClass(ExpandableListView.class)
public class ExpandableListViewBasicTest extends
        ActivityInstrumentationTestCase2<ExpandableListSimple> {
    private ExpandableListScenario mActivity;
    private ExpandableListView mListView;
    private ExpandableListAdapter mAdapter;
    private ListUtil mListUtil;
    public ExpandableListViewBasicTest() {
        super("com.android.cts.stub", ExpandableListSimple.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mListView = mActivity.getExpandableListView();
        mAdapter = mListView.getExpandableListAdapter();
        mListUtil = new ListUtil(mListView, getInstrumentation());
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mListView);
    }
    private int expandGroup(int numChildren, boolean atLeastOneChild) {
        final int groupPos = mActivity.findGroupWithNumChildren(numChildren, atLeastOneChild);
        assertTrue("Could not find group to expand", groupPos >= 0);
        assertFalse("Group is already expanded", mListView.isGroupExpanded(groupPos));
        mListUtil.arrowScrollToSelectedPosition(groupPos);
        getInstrumentation().waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertTrue("Group did not expand", mListView.isGroupExpanded(groupPos));
        return groupPos;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ExpandableListView#expandGroup(int)}",
        method = "expandGroup",
        args = {int.class}
    )
    @MediumTest
    public void testExpandGroup() {
        expandGroup(-1, true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ExpandableListView#collapseGroup(int)}",
        method = "collapseGroup",
        args = {int.class}
    )
    @MediumTest
    public void testCollapseGroup() {
        final int groupPos = expandGroup(-1, true);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertFalse("Group did not collapse", mListView.isGroupExpanded(groupPos));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link ExpandableListView#expandGroup(int)}",
            method = "expandGroup",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link ExpandableListView#expandGroup(int)}",
            method = "isGroupExpanded",
            args = {int.class}
        )
    })
    @MediumTest
    public void testExpandedGroupMovement() {
        mListUtil.arrowScrollToSelectedPosition(0);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertTrue("Group did not expand", mListView.isGroupExpanded(0));
        getInstrumentation().waitForIdleSync();
        assertTrue("Group did not expand", mListView.isGroupExpanded(0));
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
                mListView.isGroupExpanded(1));
        assertFalse("The expanded state was given to the inserted group",
                mListView.isGroupExpanded(0));
    }
}

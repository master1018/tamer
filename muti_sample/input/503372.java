@TestTargetClass(ExpandableListActivity.class)
public class ExpandableListActivityTest extends ActivityTestsBase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIntent.putExtra("component", new ComponentName(getContext(),
                ExpandableListTestActivity.class));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListView",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setListAdapter",
            args = {android.widget.ExpandableListAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelectedPosition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupExpand",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupCollapse",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelectedChild",
            args = {int.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelectedGroup",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelectedId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ExpandableListActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListAdapter",
            args = {}
        )
    })
    public void testSelect() {
        runLaunchpad(LaunchpadActivity.EXPANDLIST_SELECT);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setListAdapter",
            args = {android.widget.ExpandableListAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupExpand",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupCollapse",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ExpandableListActivity",
            args = {}
        )
    })
    public void testView() {
        runLaunchpad(LaunchpadActivity.EXPANDLIST_VIEW);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setListAdapter",
            args = {android.widget.ExpandableListAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupExpand",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupCollapse",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {android.view.ContextMenu.class, android.view.View.class,
                    android.view.ContextMenu.ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListView",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ExpandableListActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildClick",
            args = {android.widget.ExpandableListView.class, android.view.View.class,
                    int.class, int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListAdapter",
            args = {}
        )
    })
    public void testCallback() {
        runLaunchpad(LaunchpadActivity.EXPANDLIST_CALLBACK);
    }
}
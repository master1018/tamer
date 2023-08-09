public class ExpandableListTester {
    private final ExpandableListView mExpandableListView;
    private final ExpandableListAdapter mAdapter;
    private final ListUtil mListUtil;
    private final ActivityInstrumentationTestCase2<? extends ExpandableListScenario>
        mActivityInstrumentation;
    Instrumentation mInstrumentation;
    public ExpandableListTester(
            ExpandableListView expandableListView,
            ActivityInstrumentationTestCase2<? extends ExpandableListScenario>
            activityInstrumentation) {
        mExpandableListView = expandableListView;
        Instrumentation instrumentation = activityInstrumentation.getInstrumentation();
        mListUtil = new ListUtil(mExpandableListView, instrumentation);
        mAdapter = mExpandableListView.getExpandableListAdapter();
        mActivityInstrumentation = activityInstrumentation;
        mInstrumentation = mActivityInstrumentation.getInstrumentation();
    }
    private void expandGroup(final int groupIndex, int flatPosition) {
        Assert.assertFalse("Group is already expanded", mExpandableListView
                .isGroupExpanded(groupIndex));
        mListUtil.arrowScrollToSelectedPosition(flatPosition);
        mInstrumentation.waitForIdleSync();
        mActivityInstrumentation.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        mActivityInstrumentation.getInstrumentation().waitForIdleSync();
        Assert.assertTrue("Group did not expand " + groupIndex, 
                mExpandableListView.isGroupExpanded(groupIndex));
    }
    void testContextMenus() {
        PositionTesterContextMenuListener menuListener = new PositionTesterContextMenuListener();
        mExpandableListView.setOnCreateContextMenuListener(menuListener);
        int index = 0;
        for (int i=0; i<mExpandableListView.getHeaderViewsCount(); i++) {
            menuListener.expectAdapterContextMenu(i);
            mListUtil.arrowScrollToSelectedPosition(index);
            View headerChild = mExpandableListView.getChildAt(index
                    - mExpandableListView.getFirstVisiblePosition());
            mExpandableListView.showContextMenuForChild(headerChild);
            mInstrumentation.waitForIdleSync();
            Assert.assertNull(menuListener.getErrorMessage(), menuListener.getErrorMessage());
            index++;
        }
        int groupCount = mAdapter.getGroupCount();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            expandGroup(groupIndex, index);
            menuListener.expectGroupContextMenu(groupIndex);
            mListUtil.arrowScrollToSelectedPosition(index);
            View groupChild = mExpandableListView.getChildAt(index
                    - mExpandableListView.getFirstVisiblePosition());
            mExpandableListView.showContextMenuForChild(groupChild);
            mInstrumentation.waitForIdleSync();
            Assert.assertNull(menuListener.getErrorMessage(), menuListener.getErrorMessage());
            index++;
            final int childrenCount = mAdapter.getChildrenCount(groupIndex);
            for (int childIndex = 0; childIndex < childrenCount; childIndex++) {
                mListUtil.arrowScrollToSelectedPosition(index);
                menuListener.expectChildContextMenu(groupIndex, childIndex);
                View child = mExpandableListView.getChildAt(index
                        - mExpandableListView.getFirstVisiblePosition());
                mExpandableListView.showContextMenuForChild(child);
                mInstrumentation.waitForIdleSync();
                Assert.assertNull(menuListener.getErrorMessage(), menuListener.getErrorMessage());
                index++;
            }
        }
        for (int i=0; i<mExpandableListView.getFooterViewsCount(); i++) {
            menuListener.expectAdapterContextMenu(index);
            mListUtil.arrowScrollToSelectedPosition(index);
            View footerChild = mExpandableListView.getChildAt(index
                    - mExpandableListView.getFirstVisiblePosition());
            mExpandableListView.showContextMenuForChild(footerChild);
            mInstrumentation.waitForIdleSync();
            Assert.assertNull(menuListener.getErrorMessage(), menuListener.getErrorMessage());
            index++;
        }
        mExpandableListView.setOnCreateContextMenuListener(null);
    }
    private int expandAGroup() {
        final int groupIndex = 2;
        final int headerCount = mExpandableListView.getHeaderViewsCount();
        Assert.assertTrue("Not enough groups", groupIndex < mAdapter.getGroupCount());
        expandGroup(groupIndex, groupIndex + headerCount);
        return groupIndex;
    }
    void testConvertionBetweenFlatAndPackedOnGroups() {
        final int headerCount = mExpandableListView.getHeaderViewsCount();
        for (int i=0; i<headerCount; i++) {
            Assert.assertEquals("Non NULL position for header item",
                    ExpandableListView.PACKED_POSITION_VALUE_NULL,
                    mExpandableListView.getExpandableListPosition(i));
        }
        final int groupCount = mAdapter.getGroupCount();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            int expectedFlatPosition = headerCount + groupIndex;
            long packedPositionForGroup = ExpandableListView.getPackedPositionForGroup(groupIndex);
            Assert.assertEquals("Group not found at flat position " + expectedFlatPosition,
                    packedPositionForGroup,
                    mExpandableListView.getExpandableListPosition(expectedFlatPosition));
            Assert.assertEquals("Wrong flat position for group " + groupIndex,
                    expectedFlatPosition,
                    mExpandableListView.getFlatListPosition(packedPositionForGroup));
        }
        for (int i=0; i<mExpandableListView.getFooterViewsCount(); i++) {
            Assert.assertEquals("Non NULL position for header item",
                    ExpandableListView.PACKED_POSITION_VALUE_NULL,
                    mExpandableListView.getExpandableListPosition(headerCount + groupCount + i));
        }
    }
    void testConvertionBetweenFlatAndPackedOnChildren() {
        final int headerCount = mExpandableListView.getHeaderViewsCount();
        final int groupIndex = expandAGroup();
        final int childrenCount = mAdapter.getChildrenCount(groupIndex);
        for (int childIndex = 0; childIndex < childrenCount; childIndex++) {
            int expectedFlatPosition = headerCount + groupIndex + 1 + childIndex;
            long childPos = ExpandableListView.getPackedPositionForChild(groupIndex, childIndex);
            Assert.assertEquals("Wrong flat position for child ",
                    childPos,
                    mExpandableListView.getExpandableListPosition(expectedFlatPosition));
            Assert.assertEquals("Wrong flat position for child ",
                    expectedFlatPosition,
                    mExpandableListView.getFlatListPosition(childPos));
        }
    }
    void testSelectedPositionOnGroups() {
        int index = 0;
        for (int i=0; i<mExpandableListView.getHeaderViewsCount(); i++) {
            mListUtil.arrowScrollToSelectedPosition(index);
            Assert.assertEquals("Header item is selected",
                    ExpandableListView.PACKED_POSITION_VALUE_NULL,
                    mExpandableListView.getSelectedPosition());
            index++;
        }
        final int groupCount = mAdapter.getGroupCount();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            mListUtil.arrowScrollToSelectedPosition(index);
            Assert.assertEquals("Group item is not selected",
                    ExpandableListView.getPackedPositionForGroup(groupIndex),
                    mExpandableListView.getSelectedPosition());
            index++;
        }
        for (int i=0; i<mExpandableListView.getFooterViewsCount(); i++) {
            mListUtil.arrowScrollToSelectedPosition(index);
            Assert.assertEquals("Footer item is selected",
                    ExpandableListView.PACKED_POSITION_VALUE_NULL,
                    mExpandableListView.getSelectedPosition());
            index++;
        }
    }
    void testSelectedPositionOnChildren() {
        final int headerCount = mExpandableListView.getHeaderViewsCount();
        final int groupIndex = expandAGroup();
        final int childrenCount = mAdapter.getChildrenCount(groupIndex);
        for (int childIndex = 0; childIndex < childrenCount; childIndex++) {
            int childFlatPosition = headerCount + groupIndex + 1 + childIndex;
            mListUtil.arrowScrollToSelectedPosition(childFlatPosition);
            Assert.assertEquals("Group item is not selected",
                    ExpandableListView.getPackedPositionForChild(groupIndex, childIndex),
                    mExpandableListView.getSelectedPosition());
        }
    }
}

@TestTargetClass(ExpandableListContextMenuInfo.class)
public class ExpandableListView_ExpandableListContextMenuInfoTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link ExpandableListContextMenuInfo}",
        method = "ExpandableListView.ExpandableListContextMenuInfo",
        args = {android.view.View.class, long.class, long.class}
    )
    public void testConstructor() {
        ListView listview = new ListView(getContext());
        ExpandableListContextMenuInfo expandableListContextMenuInfo =
            new ExpandableListView.ExpandableListContextMenuInfo(listview, 100L, 80L);
        assertNotNull(expandableListContextMenuInfo);
        assertSame(listview, expandableListContextMenuInfo.targetView);
        assertEquals(100L, expandableListContextMenuInfo.packedPosition);
        assertEquals(80L, expandableListContextMenuInfo.id);
    }
}

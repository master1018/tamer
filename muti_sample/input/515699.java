@TestTargetClass(android.webkit.DateSorter.class)
public class DateSorterTest extends AndroidTestCase {
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DateSorter",
            args = {android.content.Context.class}
        )
    })
    public void testConstructor() {
        new DateSorter(mContext);
    }
    public void testConstants() {
        assertTrue(DateSorter.DAY_COUNT >= 3);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Resource Strings used for creating the labels are not public",
            method = "getLabel",
            args = {int.class}
        )
    })
    public void testGetLabel() {
        DateSorter dateSorter = new DateSorter(mContext);
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < DateSorter.DAY_COUNT; i++) {
            String label = dateSorter.getLabel(i);
            assertNotNull(label);
            assertTrue(label.length() > 0);
            assertFalse(set.contains(label));
            set.add(label);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIndex",
            args = {long.class}
        )
    })
    public void testGetIndex() {
        DateSorter dateSorter = new DateSorter(mContext);
        for (int i = 0; i < DateSorter.DAY_COUNT; i++) {
            long boundary = dateSorter.getBoundary(i);
            int nextIndex = i + 1;
            assertEquals(i, dateSorter.getIndex(boundary + 1));
            if (i == DateSorter.DAY_COUNT - 1) break;
            assertEquals(nextIndex, dateSorter.getIndex(boundary));
            assertEquals(nextIndex, dateSorter.getIndex(boundary-1));
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBoundary",
            args = {int.class}
        )
    })
    public void testGetBoundary() {
        DateSorter dateSorter = new DateSorter(mContext);
        for (int i = 0; i < DateSorter.DAY_COUNT - 1; i++) {
            assertTrue(dateSorter.getBoundary(i) > dateSorter.getBoundary(i+1));
        }
    }
}

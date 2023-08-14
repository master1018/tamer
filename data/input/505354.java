@TestTargetClass(Intent.FilterComparison.class)
public class Intent_FilterComparisonTest extends AndroidTestCase {
    FilterComparison mFilterComparison;
    Intent mIntent;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFilterComparison = null;
        mIntent = new Intent();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent.FilterComparison",
        args = {android.content.Intent.class}
    )
    public void testConstructor() {
        mFilterComparison = null;
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        assertEquals(mIntent.filterHashCode(), mFilterComparison.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        FilterComparison target = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        assertTrue(mFilterComparison.equals(target));
        target = new Intent.FilterComparison(new Intent("test"));
        assertFalse(mFilterComparison.equals(target));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIntent",
        args = {}
    )
    public void testGetIntent() {
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        assertTrue(mFilterComparison.getIntent().equals(mIntent));
    }
}

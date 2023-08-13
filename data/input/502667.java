@TestTargetClass(android.database.DataSetObserver.class)
public class DataSetObserverTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onChanged, and this is an empty method.",
        method = "onChanged",
        args = {}
    )
    public void testOnChanged() {
        MockDataSetObserver dataSetObserver = new MockDataSetObserver();
        dataSetObserver.onChanged();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test OnInvalidated, and this is an empty method.",
        method = "onInvalidated",
        args = {}
    )
    public void testOnInvalidated() {
        MockDataSetObserver dataSetObserver = new MockDataSetObserver();
        dataSetObserver.onInvalidated();
    }
    private class MockDataSetObserver extends DataSetObserver {
    }
}

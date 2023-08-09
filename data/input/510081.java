@TestTargetClass(android.database.DataSetObservable.class)
public class DataSetObservableTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test notifyChanged and notifyInvalidated.",
            method = "notifyChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test notifyChanged and notifyInvalidated.",
            method = "notifyInvalidated",
            args = {}
        )
    })
    public void testNotifyMethods() {
        DataSetObservable dataSetObservalbe = new DataSetObservable();
        MockDataSetObserver dataSetObserver1 = new MockDataSetObserver();
        MockDataSetObserver dataSetObserver2 = new MockDataSetObserver();
        dataSetObservalbe.registerObserver(dataSetObserver1);
        dataSetObservalbe.registerObserver(dataSetObserver2);
        assertFalse(dataSetObserver1.hasChanged());
        assertFalse(dataSetObserver2.hasChanged());
        dataSetObservalbe.notifyChanged();
        assertTrue(dataSetObserver1.hasChanged());
        assertTrue(dataSetObserver2.hasChanged());
        assertFalse(dataSetObserver1.hasInvalidated());
        assertFalse(dataSetObserver2.hasInvalidated());
        dataSetObservalbe.notifyInvalidated();
        assertTrue(dataSetObserver1.hasInvalidated());
        assertTrue(dataSetObserver2.hasInvalidated());
        dataSetObservalbe.unregisterAll();
        dataSetObserver1.resetStatus();
        dataSetObserver2.resetStatus();
        dataSetObservalbe.notifyChanged();
        assertFalse(dataSetObserver1.hasChanged());
        assertFalse(dataSetObserver2.hasChanged());
        dataSetObservalbe.notifyInvalidated();
        assertFalse(dataSetObserver1.hasInvalidated());
        assertFalse(dataSetObserver2.hasInvalidated());
    }
    private class MockDataSetObserver extends DataSetObserver {
        private boolean mHasChanged = false;
        private boolean mHasInvalidated = false;
        @Override
        public void onChanged() {
            super.onChanged();
            mHasChanged = true;
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mHasInvalidated = true;
        }
        protected boolean hasChanged() {
            return mHasChanged;
        }
        protected boolean hasInvalidated() {
            return mHasInvalidated;
        }
        protected void resetStatus() {
            mHasChanged = false;
            mHasInvalidated = false;
        }
    }
}

public class MockDataSetObserver extends DataSetObserver {
    private int mChangedCount = 0;
    private int mInvalidatedCount = 0;
    public void assertNotChanged() {
        Assert.assertFalse("onChanged() was called", mChangedCount > 0);
    }
    public void assertNotInvalidated() {
        Assert.assertFalse("onInvalidated() was called", mInvalidatedCount > 0);
    }
    public void assertChanged() {
        Assert.assertTrue("onChanged() was not called", mChangedCount > 0);
    }
    public void assertInvalidated() {
        Assert.assertTrue("onInvalidated() was not called", mInvalidatedCount > 0);
    }
    @Override
    public void onChanged() {
        mChangedCount++;
    }
    @Override
    public void onInvalidated() {
        mInvalidatedCount++;
    }
}

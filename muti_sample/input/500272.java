@TestTargetClass(android.database.sqlite.SQLiteClosable.class)
public class SQLiteClosableTest extends AndroidTestCase {
    private class MockSQLiteClosable extends SQLiteClosable {
        private boolean mOnAllReferencesReleasedCalled = false;
        private boolean mOnAllReferencesReleasedFromContainerCalled = false;
        @Override
        protected void onAllReferencesReleased() {
            mOnAllReferencesReleasedCalled = true;
        }
        protected void onAllReferencesReleasedFromContainer() {
            mOnAllReferencesReleasedFromContainerCalled = true;
        }
        public boolean isOnAllReferencesReleasedCalled() {
            return mOnAllReferencesReleasedCalled;
        }
        public boolean isOnAllReferencesReleasedFromContainerCalled() {
            return mOnAllReferencesReleasedFromContainerCalled;
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test acquireReference(), releaseReference() and onAllReferencesReleased()",
            method = "acquireReference",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test acquireReference(), releaseReference() and onAllReferencesReleased()",
            method = "releaseReference",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test acquireReference(), releaseReference() and onAllReferencesReleased()",
            method = "onAllReferencesReleased",
            args = {}
        )
    })
    public void testAcquireReference() {
        MockSQLiteClosable closable = new MockSQLiteClosable();
        closable.acquireReference();
        closable.releaseReference();
        assertFalse(closable.isOnAllReferencesReleasedCalled());
        closable.releaseReference();
        assertTrue(closable.isOnAllReferencesReleasedCalled());
        try {
            closable.acquireReference();
            fail("should throw IllegalStateException.");
        } catch (IllegalStateException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test releaseReferenceFromContainer(), onAllReferencesReleasedFromContainer()",
            method = "releaseReferenceFromContainer",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test releaseReferenceFromContainer(), onAllReferencesReleasedFromContainer()",
            method = "onAllReferencesReleasedFromContainer",
            args = {}
        )
    })
    public void testReleaseReferenceFromContainer() {
        MockSQLiteClosable closable = new MockSQLiteClosable();
        closable.acquireReference();
        closable.releaseReferenceFromContainer();
        assertFalse(closable.isOnAllReferencesReleasedFromContainerCalled());
        closable.releaseReferenceFromContainer();
        assertTrue(closable.isOnAllReferencesReleasedFromContainerCalled());
    }
}

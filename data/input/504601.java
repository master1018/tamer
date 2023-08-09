    value = AbstractSelectionKey.class, 
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "empty protected constructor",
            method = "AbstractSelectionKey",
            args = {}
        )
    }
)
public class AbstractSelectionKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )
    public void test_isValid() throws Exception {
        MockSelectionKey testKey = new MockSelectionKey();
        assertTrue(testKey.isValid());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "cancel",
        args = {}
    )
    public void test_cancel() throws Exception {
        MockSelectionKey testKey = new MockSelectionKey();
        try {
            testKey.cancel();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        assertFalse(testKey.isValid());
    }
    private class MockSelectionKey extends AbstractSelectionKey {
        MockSelectionKey() {
            super();
        }
        public SelectableChannel channel() {
            return null;
        }
        public Selector selector() {
            return null;
        }
        public int interestOps() {
            return 0;
        }
        public SelectionKey interestOps(int arg0) {
            return null;
        }
        public int readyOps() {
            return 0;
        }
    }
}

    value = SelectableChannel.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "empty protected constructor",
            method = "SelectableChannel",
            args = {}
        )
    }
)
public class SelectableChannelTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Abstract method.",
        method = "register",
        args = {java.nio.channels.Selector.class, int.class}
    )
    public void test_register_LSelectorI() throws IOException {
        MockSelectableChannel msc = new MockSelectableChannel();
        msc.register(Selector.open(), SelectionKey.OP_ACCEPT);
        assertTrue(msc.isCalled);
    }
    private class MockSelectableChannel extends SelectableChannel {
        private boolean isCalled = false;
        public Object blockingLock() {
            return null;
        }
        public SelectableChannel configureBlocking(boolean block)
                throws IOException {
            return null;
        }
        public boolean isBlocking() {
            return false;
        }
        public boolean isRegistered() {
            return false;
        }
        public SelectionKey keyFor(Selector sel) {
            return null;
        }
        public SelectorProvider provider() {
            return null;
        }
        public SelectionKey register(Selector sel, int ops, Object att)
                throws ClosedChannelException {
            if (null == att) {
                isCalled = true;
            }
            return null;
        }
        public int validOps() {
            return 0;
        }
        protected void implCloseChannel() throws IOException {
        }
    }
}

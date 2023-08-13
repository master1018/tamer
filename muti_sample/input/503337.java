    value = AbstractInterruptibleChannel.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "empty protected constructor",
            method = "AbstractInterruptibleChannel",
            args = {}
        )
    }
)
public class AbstractInterruptibleChannelTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isOpen",
            args = {}
        )
    })
    public void test_close() throws IOException {
        MockInterruptibleChannel testMiChannel = new MockInterruptibleChannel();
        assertTrue(testMiChannel.isOpen());
        testMiChannel.isImplCloseCalled = false;
        testMiChannel.close();
        assertTrue(testMiChannel.isImplCloseCalled);
        assertFalse(testMiChannel.isOpen());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "begin",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "end",
            args = {boolean.class}
        )
    })
    public void test_begin_end() throws IOException {
        boolean complete = false;
        MockInterruptibleChannel testChannel = new MockInterruptibleChannel();
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        try {
            testChannel.superBegin();
            complete = false;
        } finally {
            testChannel.superEnd(complete);
        }
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        testChannel.superEnd(complete);
        testChannel.superBegin();
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        assertTrue(testChannel.isOpen());
        testChannel.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "begin",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "end",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "close",
            args = {}
        )
    })
    public void test_close_begin_end() throws IOException {
        boolean complete = false;
        MockInterruptibleChannel testChannel = new MockInterruptibleChannel();
        assertTrue(testChannel.isOpen());
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        assertTrue(testChannel.isOpen());
        testChannel.close();
        try {
            testChannel.superBegin();
            complete = false;
        } finally {
            try {
                testChannel.superEnd(complete);
                fail("should throw AsynchronousCloseException");
            } catch (AsynchronousCloseException e) {
            }
        }
        assertFalse(testChannel.isOpen());
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        assertFalse(testChannel.isOpen());
    }
    private class MockInterruptibleChannel extends AbstractInterruptibleChannel {
        private boolean isImplCloseCalled = false;
        public MockInterruptibleChannel() {
            super();
        }
        protected void implCloseChannel() throws IOException {
            isImplCloseCalled = true; 
        }
        void superBegin() {
            super.begin();
        }
        void superEnd(boolean completed) throws AsynchronousCloseException {
            super.end(completed);
        }
    }
}

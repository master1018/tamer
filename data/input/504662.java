public class AtParserTest extends TestCase {
    private class HandlerTest extends AtCommandHandler {
        boolean mBasicCalled, mActionCalled, mReadCalled, mTestCalled, 
                mSetCalled;
        int mBasicReturn, mActionReturn, mReadReturn, mTestReturn, mSetReturn;
        Object[] mSetArgs;
        String mBasicArgs;
        HandlerTest() {
            this(AtCommandResult.ERROR, AtCommandResult.ERROR,
                 AtCommandResult.ERROR, AtCommandResult.ERROR,
                 AtCommandResult.ERROR);
        }
        HandlerTest(int a, int b, int c, int d, int e) {
            mBasicReturn = a;
            mActionReturn = b;
            mReadReturn = c;
            mSetReturn = d;
            mTestReturn = e;
            reset();
        }
        public void reset() {
            mBasicCalled = false;
            mActionCalled = false;
            mReadCalled = false;
            mSetCalled = false;
            mTestCalled = false;
            mSetArgs = null;
            mBasicArgs = null;
        }
        public boolean wasCalled() {   
            return mBasicCalled || mActionCalled || mReadCalled ||
                    mTestCalled || mSetCalled;
        }
        @Override
        public AtCommandResult handleBasicCommand(String args) {
            mBasicCalled = true;
            mBasicArgs = args;
            return new AtCommandResult(mBasicReturn);
        }
        @Override
        public AtCommandResult handleActionCommand() {
            mActionCalled = true;
            return new AtCommandResult(mActionReturn);
        }
        @Override
        public AtCommandResult handleReadCommand() {
            mReadCalled = true;
            return new AtCommandResult(mReadReturn);
        }
        @Override
        public AtCommandResult handleSetCommand(Object[] args) {
            mSetCalled = true;
            mSetArgs = args;
            return new AtCommandResult(mSetReturn);
        }
        @Override
        public AtCommandResult handleTestCommand() {
            mTestCalled = true;
            return new AtCommandResult(mTestReturn);
        }
    }
    private AtParser mParser;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mParser = new AtParser();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}

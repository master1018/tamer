@TestTargetClass(ConditionVariable.class)
public class ConditionVariableTest extends TestCase {
    private static final int WAIT_TIME = 3000;
    private static final int BLOCK_TIME = 1000;
    private static final int BLOCK_TIME_DELTA = 200;
    private static final int SLEEP_TIME = 1000;
    private static final int TOLERANCE_MS = BLOCK_TIME;
    private ConditionVariable mConditionVariable;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mConditionVariable = new ConditionVariable();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ConditionVariable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ConditionVariable",
            args = {boolean.class}
        )
    })
    public void testConstructor() {
        assertFalse(mConditionVariable.block(BLOCK_TIME));
        assertFalse(new ConditionVariable(false).block(BLOCK_TIME));
        assertTrue(new ConditionVariable(true).block(BLOCK_TIME));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "block",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "block",
            args = {}
        )
    })
    public void testConditionVariable() throws Throwable {
        mConditionVariable.open();
        long time = System.currentTimeMillis();
        assertTrue(mConditionVariable.block(BLOCK_TIME));
        assertTrue(System.currentTimeMillis() - time < TOLERANCE_MS);
        mConditionVariable.close();
        time = System.currentTimeMillis();
        assertFalse(mConditionVariable.block(BLOCK_TIME));
        assertTrue(System.currentTimeMillis() - time >= BLOCK_TIME);
        time = System.currentTimeMillis();
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
                mConditionVariable.open();
            }
        });
        t.start();
        mConditionVariable.block();
        long timeDelta = System.currentTimeMillis() - time;
        assertTrue(timeDelta >= BLOCK_TIME && timeDelta <= BLOCK_TIME + BLOCK_TIME_DELTA);
        t.joinAndCheck(WAIT_TIME);
        time = System.currentTimeMillis();
        t = new TestThread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(BLOCK_TIME >> 1);
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
                mConditionVariable.open();
            }
        });
        t.start();
        assertTrue(mConditionVariable.block(BLOCK_TIME));
        t.joinAndCheck(WAIT_TIME);
    }
}

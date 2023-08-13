@TestTargetClass(CountDownTimer.class)
public class CountDownTimerTest extends InstrumentationTestCase {
    private Context mContext;
    private CountDownTimerTestStub mActivity;
    private long mStartTime;
    private final long OFFSET = 200;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        Intent intent = new Intent(mContext, CountDownTimerTestStub.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity = (CountDownTimerTestStub) getInstrumentation().startActivitySync(intent);
        mStartTime = System.currentTimeMillis();
        mActivity.countDownTimer.start();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CountDownTimer",
            args = {long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onTick",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onFinish",
            args = {}
        )
    })
    public void testCountDownTimer() {
        int count = (int) (mActivity.MILLISINFUTURE / mActivity.INTERVAL);
        final long TIMEOUT_MSEC = mActivity.MILLISINFUTURE + mActivity.INTERVAL + OFFSET * count;
        waitForAction(TIMEOUT_MSEC);
        assertTrue(mActivity.onFinished);
        assertEqualsTickTime(mActivity.tickTimes, OFFSET);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CountDownTimer",
            args = {long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onTick",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onFinish",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "cancel",
            args = {}
        )
    })
    public void testCountDownTimerCancel() {
        final long DELAY_MSEC = mActivity.INTERVAL + OFFSET;
        assertTrue(DELAY_MSEC < mActivity.MILLISINFUTURE);
        waitForAction(DELAY_MSEC);
        assertFalse(mActivity.onFinished);
        mActivity.countDownTimer.cancel();
        final long TIMEOUT_MSEC = mActivity.MILLISINFUTURE + mActivity.INTERVAL;
        waitForAction(TIMEOUT_MSEC);
        assertFalse(mActivity.onFinished);
        int count = Long.valueOf(DELAY_MSEC / mActivity.INTERVAL).intValue() + 1;
        assertEquals(count, mActivity.tickTimes.size());
        assertEqualsTickTime(mActivity.tickTimes, OFFSET);
    }
    private void assertEqualsTickTime(ArrayList<Long> tickTimes, long offset) {
        for (int i = 0; i < tickTimes.size(); i++) {
            long tickTime = tickTimes.get(i);
            long expecTickTime = mStartTime + i * mActivity.INTERVAL;
            assertTrue(Math.abs(expecTickTime - tickTime) < offset);
        }
    }
    private void waitForAction(long time) {
        try {
            Thread.sleep(time);
        } catch (final InterruptedException e) {
            fail("error occurs when wait for an action: " + e.toString());
        }
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mActivity != null) {
            mActivity.finish();
        }
    }
}

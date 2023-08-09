@TestTargetClass(TimingLogger.class)
public class TimingLoggerTest extends AndroidTestCase{
    private static final String LOG_TAG = "TimingLoggerTest";
    private static final int SLEEPING_MSEC = 100;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test TimingLogger",
            method = "TimingLogger",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test TimingLogger",
            method = "addSplit",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test TimingLogger",
            method = "dumpToLog",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test TimingLogger",
            method = "reset",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test TimingLogger",
            method = "reset",
            args = {}
        )
    })
    public void testTimingLogger() {
        TimingLogger timings = new TimingLogger(LOG_TAG, "testTimingLogger");
        for (int i = 0; i < 3; i++) {
            if (1 == i) {
                timings.reset(LOG_TAG, "testReset");
            } else if (2 == i) {
                timings.reset();
            }
            sleep();
            timings.addSplit("fisrt sleep");
            sleep();
            timings.addSplit("second sleep");
            sleep();
            timings.addSplit("third sleep");
            timings.dumpToLog();
        }
    }
    private void sleep() {
        try {
            Thread.sleep(SLEEPING_MSEC);
        } catch (InterruptedException e) {
        }
    }
}

@TestTargetClass(SystemClock.class)
public class SystemClockTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test currentThreadTimeMillis(), the sleep() will not affect the thread",
        method = "currentThreadTimeMillis",
        args = {}
    )
    public void testCurrentThreadTimeMillis() throws InterruptedException {
        long start = SystemClock.currentThreadTimeMillis();
        Thread.sleep(100);
        long end = SystemClock.currentThreadTimeMillis();
        assertFalse(end - 100 >= start);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "elapsedRealtime",
        args = {}
    )
    public void testElapsedRealtime() throws InterruptedException {
        long start = SystemClock.elapsedRealtime();
        Thread.sleep(100);
        long end = SystemClock.elapsedRealtime();
        assertTrue(end - 100 >= start);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCurrentTimeMillis",
        args = {long.class}
    )
    public void testSetCurrentTimeMillis() {
        long start = SystemClock.currentThreadTimeMillis();
        boolean actual = SystemClock.setCurrentTimeMillis(start + 10000);
        assertFalse(actual);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test sleep(long), it is similar to Thread.sleep().",
        method = "sleep",
        args = {long.class}
    )
    public void testSleep() {
        long start = SystemClock.currentThreadTimeMillis();
        SystemClock.sleep(100);
        long end = SystemClock.currentThreadTimeMillis();
        assertFalse(end - 100 >= start);
        start = SystemClock.elapsedRealtime();
        SystemClock.sleep(100);
        end = SystemClock.elapsedRealtime();
        assertTrue(end - 100 >= start);
        start = SystemClock.uptimeMillis();
        SystemClock.sleep(100);
        end = SystemClock.uptimeMillis();
        assertTrue(end - 100 >= start);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "uptimeMillis",
        args = {}
    )
    public void testUptimeMillis() throws InterruptedException {
        long start = SystemClock.uptimeMillis();
        Thread.sleep(100);
        long end = SystemClock.uptimeMillis();
        assertTrue(end - 100 >= start);
    }
}

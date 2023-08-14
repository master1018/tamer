@TestTargetClass(TimerTask.class) 
public class TimerTaskTest extends junit.framework.TestCase {
    Object sync = new Object(), start = new Object();
    class TimerTestTask extends TimerTask {
        private int wasRun = 0;
        private boolean sleepInRun = false;
        public void run() {
            synchronized (this) {
                wasRun++;
            }
            synchronized (start) {
                start.notify();
            }
            if (sleepInRun) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
            synchronized (sync) {
                sync.notify();
            }
        }
        public synchronized int wasRun() {
            return wasRun;
        }
        public void sleepInRun(boolean value) {
            sleepInRun = value;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TimerTask",
        args = {}
    )
    public void test_Constructor() {
        new TimerTestTask();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "cancel",
        args = {}
    )
    public void test_cancel() {
        Timer t = null;
        try {
            TimerTestTask testTask = new TimerTestTask();
            assertTrue("Unsheduled tasks should return false for cancel()",
                    !testTask.cancel());
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 500);
            assertTrue("TimerTask should not have run yet", testTask.cancel());            
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 50);
            while (testTask.wasRun() == 0) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                }
            }
            assertFalse(
                    "TimerTask.cancel() should return false if task has run",
                    testTask.cancel());
            assertFalse(
                    "TimerTask.cancel() should return false if called a second time",
                    testTask.cancel());
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 500, 500); 
            assertTrue(
                    "TimerTask.cancel() should return true if sheduled for repeated execution even if not run",
                    testTask.cancel());
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 50, 50); 
            while (testTask.wasRun() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }                
            }            
            assertTrue(
                    "TimerTask.cancel() should return true if sheduled for repeated execution and run",
                    testTask.cancel());            
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 5000); 
            assertTrue(
                    "TimerTask.cancel() should return true if task has never run",
                    testTask.cancel());
            assertFalse(
                    "TimerTask.cancel() should return false if called a second time",
                    testTask.cancel());
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.sleepInRun(true);
            synchronized (start) {
                t.schedule(testTask, 0);
                try {
                    start.wait();
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            assertFalse("TimerTask should have been cancelled", testTask
                    .cancel());
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "scheduledExecutionTime",
        args = {}
    )
    public void test_scheduledExecutionTime() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 100);
            long time = System.currentTimeMillis() + 100;
            synchronized (sync) {
                try {
                    sync.wait(500);
                } catch (InterruptedException e) {
                }
            }
            long scheduledExecutionTime = testTask.scheduledExecutionTime();
            assertTrue(scheduledExecutionTime <= time);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 100, 500);
            long estNow = System.currentTimeMillis() + 100;
            synchronized (sync) {
                try {
                    sync.wait(500);
                } catch (InterruptedException e) {
                }
            }
            scheduledExecutionTime = testTask.scheduledExecutionTime();
            assertTrue(scheduledExecutionTime <= estNow);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "run",
        args = {}
    )
    public void test_run() {
        Timer t = null;
        try {
            TimerTestTask testTask = new TimerTestTask();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            assertEquals("TimerTask.run() method should not have been called",
                    0, testTask.wasRun());
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            while(testTask.wasRun() < 1) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                }
            }
            assertFalse(testTask.cancel());
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
}

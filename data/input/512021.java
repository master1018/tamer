@TestTargetClass(Timer.class) 
public class TimerTest extends junit.framework.TestCase {
    int timerCounter = 0;
    Object sync = new Object();
    class TimerTestTask extends TimerTask {
        int wasRun = 0;
        boolean sleepInRun = false;
        boolean incrementCount = false;
        int terminateCount = -1;
        Timer timer = null;
        public TimerTestTask() {
        }
        public TimerTestTask(Timer t) {
            timer = t;
        }
        public void run() {
            synchronized (this) {
                wasRun++;
            }
            if (incrementCount)
                timerCounter++;
            if (terminateCount == timerCounter && timer != null)
                timer.cancel();
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
        public void sleepInRun(boolean sleepInRun) {
            this.sleepInRun = sleepInRun;
        }
        public void incrementCount(boolean incrementCount) {
            this.incrementCount = incrementCount;
        }
        public void terminateCount(int terminateCount) {
            this.terminateCount = terminateCount;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Timer",
        args = {boolean.class}
    )
    public void test_ConstructorZ() {
        Timer t = null;
        try {
            t = new Timer(true);
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            synchronized (sync) {
                try {
                    sync.wait(1000);
                } catch (InterruptedException e) {
                }
            }
            assertEquals("TimerTask.run() method not called after 200ms",
                    1, testTask.wasRun());
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Timer",
        args = {}
    )
    public void test_Constructor() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            synchronized (sync) {
                try {
                    sync.wait(1000);
                } catch (InterruptedException e) {
                }
            }
            assertEquals("TimerTask.run() method not called after 200ms",
                    1, testTask.wasRun());
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Timer",
        args = {java.lang.String.class, boolean.class}
    )
    public void test_ConstructorSZ() {
        Timer t = null;
        try {
            t = new Timer("test_ConstructorSZThread", true);
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            synchronized (sync) {
                try {
                    sync.wait(1000);
                } catch (InterruptedException e) {}
            }
            assertEquals("TimerTask.run() method not called after 200ms", 1,
                    testTask.wasRun());
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
        try {
            new Timer(null, true);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        try {
            new Timer(null, false);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Timer",
        args = {java.lang.String.class}
    )
    public void test_ConstructorS() {
        Timer t = null;
        try {
            t = new Timer("test_ConstructorSThread");
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            synchronized (sync) {
                try {
                    sync.wait(1000);
                } catch (InterruptedException e) {}
            }
            assertEquals("TimerTask.run() method not called after 200ms", 1,
                    testTask.wasRun());
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
        try {
            new Timer(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
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
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            boolean exception = false;
            try {
                t.schedule(testTask, 100, 200);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after Timer.cancel() should throw exception",
                    exception);
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 100, 500);
            synchronized (sync) {
                try {
                    sync.wait(1000);
                } catch (InterruptedException e) {
                }
            }
            assertEquals("TimerTask.run() method not called after 200ms",
                    1, testTask.wasRun());
            t.cancel();
            synchronized (sync) {
                try {
                    sync.wait(500);
                } catch (InterruptedException e) {
                }
            }
            assertEquals("TimerTask.run() method should not have been called after cancel",
                    1, testTask.wasRun());
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 100, 500);
            synchronized (sync) {
                try {
                    sync.wait(500);
                } catch (InterruptedException e) {
                }
            }
            assertEquals("TimerTask.run() method not called after 200ms",
                    1, testTask.wasRun());
            t.cancel();
            t.cancel();
            t.cancel();
            synchronized (sync) {
                try {
                    sync.wait(500);
                } catch (InterruptedException e) {
                }
            }
            assertEquals("TimerTask.run() method should not have been called after cancel",
                    1, testTask.wasRun());
            t = new Timer();
            testTask = new TimerTestTask(t);
            testTask.incrementCount(true);
            testTask.terminateCount(5); 
            t.schedule(testTask, 100, 100);
            synchronized (sync) {
                try {
                    sync.wait(200);
                    sync.wait(200);
                    sync.wait(200);
                    sync.wait(200);
                    sync.wait(200);
                    sync.wait(200);
                } catch (InterruptedException e) {
                }
            }
            assertTrue("TimerTask.run() method should be called 5 times not "
                    + testTask.wasRun(), testTask.wasRun() == 5);
            t.cancel();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "purge",
        args = {}
    )
    public void test_purge() throws Exception {
        Timer t = null;
        try {
            t = new Timer();
            assertEquals(0, t.purge());
            TimerTestTask[] tasks = new TimerTestTask[100];
            int[] delayTime = { 50, 80, 20, 70, 40, 10, 90, 30, 60 };
            int j = 0;
            for (int i = 0; i < 100; i++) {
                tasks[i] = new TimerTestTask();
                t.schedule(tasks[i], delayTime[j++], 200);
                if (j == 9) {
                    j = 0;
                }
            }
            for (int i = 0; i < 50; i++) {
                tasks[i].cancel();
            }
            assertTrue(t.purge() <= 50);
            assertEquals(0, t.purge());
        } finally {
            if (t != null) {
                t.cancel();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "schedule",
        args = {java.util.TimerTask.class, java.util.Date.class}
    )
    public void test_scheduleLjava_util_TimerTaskLjava_util_Date() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            Date d = new Date(System.currentTimeMillis() + 100);
            t.cancel();
            boolean exception = false;
            try {
                t.schedule(testTask, d);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after Timer.cancel() should throw exception",
                    exception);
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(System.currentTimeMillis() + 100);
            testTask.cancel();
            exception = false;
            try {
                t.schedule(testTask, d);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after cancelling it should throw exception",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(-100);
            exception = false;
            try {
                t.schedule(testTask, d);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task with negative date should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            exception = false;
            d = new Date(System.currentTimeMillis() + 100);
            try {
                t.schedule(null, d);
            } catch (NullPointerException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task should throw NullPointerException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, null);
            } catch (NullPointerException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null date should throw NullPointerException",
                    exception);
            t.cancel();
            t = new Timer();
            d = new Date(-100);
            exception = false;
            try {
                t.schedule(null, d);
            } catch (NullPointerException e) {
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task with negative date should throw IllegalArgumentException first",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(System.currentTimeMillis() + 200);
            t.schedule(testTask, d);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            assertEquals("TimerTask.run() method not called after 200ms",
                    1, testTask.wasRun());
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 100);
            t.schedule(testTask, d);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 150);
            t.schedule(testTask, d);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 70);
            t.schedule(testTask, d);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 10);
            t.schedule(testTask, d);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "Multiple tasks should have incremented counter 4 times not "
                            + timerCounter, timerCounter == 4);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "schedule",
        args = {java.util.TimerTask.class, long.class}
    )
    public void test_scheduleLjava_util_TimerTaskJ() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            boolean exception = false;
            try {
                t.schedule(testTask, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after Timer.cancel() should throw exception",
                    exception);
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.cancel();
            exception = false;
            try {
                t.schedule(testTask, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after cancelling it should throw exception",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, -100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task with negative delay should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            exception = false;
            try {
                t.schedule(null, 10);
            } catch (NullPointerException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task should throw NullPointerException",
                    exception);
            t.cancel();
            t = new Timer();
            exception = false;
            try {
                t.schedule(null, -10);
            } catch (NullPointerException e) {
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task with negative delays should throw IllegalArgumentException first",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            assertEquals("TimerTask.run() method not called after 200ms",
                    1, testTask.wasRun());
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 100);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 150);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 70);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 10);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "Multiple tasks should have incremented counter 4 times not "
                            + timerCounter, timerCounter == 4);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "schedule",
        args = {java.util.TimerTask.class, long.class, long.class}
    )
    public void test_scheduleLjava_util_TimerTaskJJ() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            boolean exception = false;
            try {
                t.schedule(testTask, 100, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after Timer.cancel() should throw exception",
                    exception);
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.cancel();
            exception = false;
            try {
                t.schedule(testTask, 100, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after cancelling it should throw exception",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, -100, 100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task with negative delay should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, 100, -100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task with negative period should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, 100, 0);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task with 0 period should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            exception = false;
            try {
                t.schedule(null, 10, 10);
            } catch (NullPointerException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task should throw NullPointerException",
                    exception);
            t.cancel();
            t = new Timer();
            exception = false;
            try {
                t.schedule(null, -10, -10);
            } catch (NullPointerException e) {
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task with negative delays should throw IllegalArgumentException first",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 100, 100);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "TimerTask.run() method should have been called at least twice ("
                            + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 100, 100); 
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 200, 100); 
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 300, 200); 
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 100, 200); 
            try {
                Thread.sleep(1200); 
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "Multiple tasks should have incremented counter 24 times not "
                            + timerCounter, timerCounter >= 24);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "schedule",
        args = {java.util.TimerTask.class, java.util.Date.class, long.class}
    )
    public void test_scheduleLjava_util_TimerTaskLjava_util_DateJ() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            Date d = new Date(System.currentTimeMillis() + 100);
            t.cancel();
            boolean exception = false;
            try {
                t.schedule(testTask, d, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after Timer.cancel() should throw exception",
                    exception);
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            testTask = new TimerTestTask();
            testTask.cancel();
            exception = false;
            try {
                t.schedule(testTask, d, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task after cancelling it should throw exception",
                    exception);
            t.cancel();
            t = new Timer();
            d = new Date(-100);
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, d, 100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task with negative delay should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, d, -100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a task with negative period should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            exception = false;
            try {
                t.schedule(null, d, 10);
            } catch (NullPointerException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task should throw NullPointerException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.schedule(testTask, null, 10);
            } catch (NullPointerException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task should throw NullPointerException",
                    exception);
            t.cancel();
            t = new Timer();
            d = new Date(-100);
            exception = false;
            try {
                t.schedule(null, d, 10);
            } catch (NullPointerException e) {
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task with negative dates should throw IllegalArgumentException first",
                    exception);
            t.cancel();
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            testTask = new TimerTestTask();
            t.schedule(testTask, d, 100);
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "TimerTask.run() method should have been called at least twice ("
                            + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 100);
            t.schedule(testTask, d, 100); 
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 200);
            t.schedule(testTask, d, 100); 
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 300);
            t.schedule(testTask, d, 200); 
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 100);
            t.schedule(testTask, d, 200); 
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "Multiple tasks should have incremented counter 24 times not "
                            + timerCounter, timerCounter >= 24);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "scheduleAtFixedRate",
        args = {java.util.TimerTask.class, long.class, long.class}
    )
    public void test_scheduleAtFixedRateLjava_util_TimerTaskJJ() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            boolean exception = false;
            try {
                t.scheduleAtFixedRate(testTask, 100, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "scheduleAtFixedRate after Timer.cancel() should throw exception",
                    exception);
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.scheduleAtFixedRate(testTask, -100, 100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "scheduleAtFixedRate with negative delay should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.scheduleAtFixedRate(testTask, 100, -100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "scheduleAtFixedRate with negative period should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            t.scheduleAtFixedRate(testTask, 100, 100);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "TimerTask.run() method should have been called at least twice ("
                            + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();
            class SlowThenFastTask extends TimerTask {
                int wasRun = 0;
                long startedAt;
                long lastDelta;
                public void run() {
                    if (wasRun == 0)
                        startedAt = System.currentTimeMillis();
                    lastDelta = System.currentTimeMillis()
                            - (startedAt + (100 * wasRun));
                    wasRun++;
                    if (wasRun == 2) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                public long lastDelta() {
                    return lastDelta;
                }
                public int wasRun() {
                    return wasRun;
                }
            }
            t = new Timer();
            SlowThenFastTask slowThenFastTask = new SlowThenFastTask();
            t.scheduleAtFixedRate(slowThenFastTask, 100, 100);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            long lastDelta = slowThenFastTask.lastDelta();
            assertTrue("Fixed Rate Schedule should catch up, but is off by "
                    + lastDelta + " ms", slowThenFastTask.lastDelta < 300);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "scheduleAtFixedRate",
        args = {java.util.TimerTask.class, java.util.Date.class, long.class}
    )
    public void test_scheduleAtFixedRateLjava_util_TimerTaskLjava_util_DateJ() {
        Timer t = null;
        try {
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            boolean exception = false;
            Date d = new Date(System.currentTimeMillis() + 100);
            try {
                t.scheduleAtFixedRate(testTask, d, 100);
            } catch (IllegalStateException e) {
                exception = true;
            }
            assertTrue(
                    "scheduleAtFixedRate after Timer.cancel() should throw exception",
                    exception);
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            d = new Date(-100);
            try {
                t.scheduleAtFixedRate(testTask, d, 100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "scheduleAtFixedRate with negative Date should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.scheduleAtFixedRate(testTask, d, -100);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "scheduleAtFixedRate with negative period should throw IllegalArgumentException",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            exception = false;
            try {
                t.scheduleAtFixedRate(testTask, null, 100);
            } catch (NullPointerException e) {
                exception = true;
            }
            assertTrue(
                    "scheduleAtFixedRate with null date should throw NullPointerException",
                    exception);
            t.cancel();
            t = new Timer();
            exception = false;
            d = new Date(-100);
            try {
                t.scheduleAtFixedRate(null, d, 10);
            } catch (NullPointerException e) {
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task with negative date should throw IllegalArgumentException first",
                    exception);
            t.cancel();
            t = new Timer();
            exception = false;
            try {
                t.scheduleAtFixedRate(null, null, -10);
            } catch (NullPointerException e) {
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            assertTrue(
                    "Scheduling a null task & null date & negative period should throw IllegalArgumentException first",
                    exception);
            t.cancel();
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(System.currentTimeMillis() + 100);
            t.scheduleAtFixedRate(testTask, d, 100);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
            assertTrue(
                    "TimerTask.run() method should have been called at least twice ("
                            + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();
            class SlowThenFastTask extends TimerTask {
                int wasRun = 0;
                long startedAt;
                long lastDelta;
                public void run() {
                    if (wasRun == 0)
                        startedAt = System.currentTimeMillis();
                    lastDelta = System.currentTimeMillis()
                            - (startedAt + (100 * wasRun));
                    wasRun++;
                    if (wasRun == 2) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                public long lastDelta() {
                    return lastDelta;
                }
                public int wasRun() {
                    return wasRun;
                }
            }
            t = new Timer();
            SlowThenFastTask slowThenFastTask = new SlowThenFastTask();
            d = new Date(System.currentTimeMillis() + 100);
            t.scheduleAtFixedRate(slowThenFastTask, d, 100);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            long lastDelta = slowThenFastTask.lastDelta();
            assertTrue("Fixed Rate Schedule should catch up, but is off by "
                    + lastDelta + " ms", lastDelta < 300);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }
    public void testThrowingTaskKillsTimerThread() throws InterruptedException {
        final AtomicReference<Thread> threadRef = new AtomicReference<Thread>();
        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                threadRef.set(Thread.currentThread());
                throw new RuntimeException("task failure!");
            }
        }, 1);
        Thread.sleep(400);
        Thread timerThread = threadRef.get();
        assertFalse(timerThread.isAlive());
    }
    protected void setUp() {
        timerCounter = 0;
    }
    protected void tearDown() {
    }
}

@TestTargetClass(Looper.class)
public class LooperTest extends AndroidTestCase {
    public static final long WAIT_TIME = 1000;
    private boolean mHasRun;
    private Looper mLooper = null;
    private boolean mHasQuit;
    private Handler mLoopHandler;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dump",
        args = {Printer.class, String.class}
    )
    public void testDump() {
        StringBuilderPrinter printer = new StringBuilderPrinter(new StringBuilder());
        final String prefix = "LooperTest";
        Looper.myLooper().dump(printer, prefix);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMainLooper",
        args = {}
    )
    public void testGetMainLooper() {
        Looper looper = Looper.getMainLooper();
        assertNotNull(looper);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "loop",
        args = {}
    )
    public void testLoop() {
        MockRunnable run = new MockRunnable();
        Handler handler = new Handler();
        Message msg = Message.obtain(handler, run);
        handler.sendMessageAtTime(msg, 0);
        assertFalse(run.runCalled);
        Looper.loop();
        assertTrue(run.runCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "myLooper",
        args = {}
    )
    public void testMyLooper() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                assertNull(Looper.myLooper());
                Looper.prepare();
                assertNotNull(Looper.myLooper());
            }
        });
        t.runTest(WAIT_TIME);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "myQueue",
        args = {}
    )
    public void testMyQueue() throws Throwable {
        MessageQueue mq = Looper.myQueue();
        assertNotNull(mq);
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                try {
                    assertNull(Looper.myQueue());
                    fail("should throw exception");
                } catch (Throwable e) {
                }
            }
        });
        t.runTest(WAIT_TIME);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "prepare",
        args = {}
    )
    public void testPrepare() throws Throwable {
        try {
            Looper.prepare();
            fail("should throw exception");
        } catch (RuntimeException e) {
        }
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    Looper.prepare();
                    fail("should throw exception");
                } catch (Throwable e) {
                }
            }
        });
        t.runTest(WAIT_TIME);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "prepareMainLooper",
        args = {}
    )
    public void testPrepareMainLooper() throws Throwable {
        try {
            Looper.prepareMainLooper();
            fail("should throw exception");
        } catch (RuntimeException e) {
        }
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepareMainLooper();
                try {
                    Looper.prepareMainLooper();
                    fail("should throw exception");
                } catch (Throwable e) {
                }
            }
        });
        t.runTest(WAIT_TIME);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "quit",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getThread",
            args = {}
        )
    })
    public void testQuit() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                mHasQuit = false;
                Looper.prepare();
                mLoopHandler = new Handler();
                mLooper = Looper.myLooper();
                Looper.loop();
                mHasQuit = true;
            }
        });
        t.start();
        Thread.sleep(WAIT_TIME);
        assertSame(t, mLooper.getThread());
        int time = 100;
        assertTrue(mLoopHandler.sendEmptyMessageAtTime(0, SystemClock.uptimeMillis() + time));
        Thread.sleep(WAIT_TIME);
        mLooper.quit();
        Thread.sleep(WAIT_TIME);
        assertFalse(mLoopHandler.sendEmptyMessageAtTime(1, SystemClock.uptimeMillis() + time));
        assertTrue(mHasQuit);
        t.joinAndCheck(WAIT_TIME);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setMessageLogging",
        args = {Printer.class}
    )
    public void testSetMessageLogging() throws Throwable {
        mHasRun = false;
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                MockPrinter mp = new MockPrinter();
                Looper.myLooper().setMessageLogging(mp);
                MockRunnable run = new MockRunnable();
                Handler handler = new Handler();
                Message msg = Message.obtain(handler, run);
                handler.sendMessageAtTime(msg, 0);
                Looper.loop();
                assertNotNull(mp.str);
                mHasRun = true;
            }
        });
        t.runTest(WAIT_TIME);
        assertTrue(mHasRun);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertNotNull(Looper.myLooper().toString());
    }
    class MockPrinter implements Printer {
        public String str;
        public void println(String x) {
            str = x;
        }
    }
    private class MockRunnable implements Runnable {
        public boolean runCalled = false;
        public void run() {
            runCalled = true;
            Looper.myLooper().quit();
        }
        public void stop() {
            Looper.myLooper().quit();
        }
    }
}

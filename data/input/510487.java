public class HandlerThreadTest extends TestCase {
    private static final int TEST_WHAT = 1;
    private boolean mGotMessage = false;
    private int mGotMessageWhat = -1;
    private volatile boolean mDidSetup = false;
    private volatile int mLooperTid = -1;
    @MediumTest
    public void testHandlerThread() throws Exception {
        HandlerThread th1 =  new HandlerThread("HandlerThreadTest") {
            protected void onLooperPrepared() {
                synchronized (HandlerThreadTest.this) {
                    mDidSetup = true;
                    mLooperTid = Process.myTid();
                    HandlerThreadTest.this.notify();
                }
            }
        };
        assertFalse(th1.isAlive());
        assertNull(th1.getLooper());
        th1.start();
        assertTrue(th1.isAlive());
        assertNotNull(th1.getLooper());
        synchronized (this) {
            while (!mDidSetup) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        assertNotSame(-1, mLooperTid);
        assertNotSame(Process.myTid(), mLooperTid);
        final Handler h1 = new Handler(th1.getLooper()) {
            public void handleMessage(Message msg) {
                assertEquals(TEST_WHAT, msg.what);
                assertEquals(mLooperTid, Process.myTid());
                mGotMessageWhat = msg.what;
                mGotMessage = true;
                synchronized(this) {
                    notifyAll();
                }
            }
        };
        Message msg = h1.obtainMessage(TEST_WHAT);
        synchronized (h1) {
            h1.sendMessage(msg);
            try {
                h1.wait();
            } catch (InterruptedException e) {
            }
        }
        assertTrue(mGotMessage);
        assertEquals(TEST_WHAT, mGotMessageWhat);
    }
}

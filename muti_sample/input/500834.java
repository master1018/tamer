public class PriorityThreadFactoryTest extends AndroidTestCase {
    public void testPriority() throws InterruptedException {
        priorityTest(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        priorityTest(android.os.Process.THREAD_PRIORITY_DEFAULT);
        priorityTest(android.os.Process.THREAD_PRIORITY_FOREGROUND);
    }
    private void priorityTest(int priority) throws InterruptedException {
        ThreadFactory factory = new PriorityThreadFactory(priority);
        CheckPriorityRunnable r = new CheckPriorityRunnable();
        Thread t = factory.newThread(r);
        t.start();
        assertEquals(priority, r.getPriority());
    }
    private static class CheckPriorityRunnable implements Runnable {
        private Integer mPriority = null;
        public synchronized int getPriority() throws InterruptedException {
            while (mPriority == null) {
                wait();
            }
            return mPriority.intValue();
        }
        public synchronized void run() {
            int tid = android.os.Process.myTid();
            mPriority = new Integer(android.os.Process.getThreadPriority(tid));
            notify();
        }
    }
}

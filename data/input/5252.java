public class Fairness {
    private static void testFairness(boolean fair,
                                     final BlockingQueue<Integer> q)
        throws Throwable
    {
        final ReentrantLock lock = new ReentrantLock();
        final Condition ready = lock.newCondition();
        final int threadCount = 10;
        final Throwable[] badness = new Throwable[1];
        lock.lock();
        for (int i = 0; i < threadCount; i++) {
            final Integer I = i;
            Thread t = new Thread() { public void run() {
                try {
                    lock.lock();
                    ready.signal();
                    lock.unlock();
                    q.put(I);
                } catch (Throwable t) { badness[0] = t; }}};
            t.start();
            ready.await();
            while (t.getState() == Thread.State.RUNNABLE)
                Thread.yield();
        }
        for (int i = 0; i < threadCount; i++) {
            int j = q.take();
            if (fair ? j != i : j != threadCount - 1 - i)
                throw new Error(String.format("fair=%b i=%d j=%d%n",
                                              fair, i, j));
        }
        if (badness[0] != null) throw new Error(badness[0]);
    }
    public static void main(String[] args) throws Throwable {
        testFairness(false, new SynchronousQueue<Integer>());
        testFairness(false, new SynchronousQueue<Integer>(false));
        testFairness(true,  new SynchronousQueue<Integer>(true));
    }
}

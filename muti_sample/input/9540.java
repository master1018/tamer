public class PollMemoryLeak {
    public static void main(String[] args) throws InterruptedException {
        final BlockingQueue[] qs = {
            new LinkedBlockingQueue(10),
            new LinkedTransferQueue(),
            new ArrayBlockingQueue(10),
            new SynchronousQueue(),
            new SynchronousQueue(true),
        };
        final long start = System.currentTimeMillis();
        final long end = start + 10 * 1000;
        while (System.currentTimeMillis() < end)
            for (BlockingQueue q : qs)
                q.poll(1, TimeUnit.NANOSECONDS);
    }
}

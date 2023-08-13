public class AsExecutor {
    public static void main(String[] args) throws Exception {
        ThreadFactory factory = new PrivilegedThreadFactory();
        AsynchronousChannelGroup group1 = AsynchronousChannelGroup
            .withFixedThreadPool(5, factory);
        AsynchronousChannelGroup group2 = AsynchronousChannelGroup
            .withCachedThreadPool(Executors.newCachedThreadPool(factory), 0);
        try {
            testSimpleTask(group1);
            testSimpleTask(group2);
            System.setSecurityManager( new SecurityManager() );
            testSimpleTask(group1);
            testSimpleTask(group2);
            testAttackingTask(group1);
            testAttackingTask(group2);
        } finally {
            group1.shutdown();
            group2.shutdown();
        }
    }
    static void testSimpleTask(AsynchronousChannelGroup group) throws Exception {
        Executor executor = (Executor)group;
        final CountDownLatch latch = new CountDownLatch(1);
        executor.execute(new Runnable() {
            public void run() {
                latch.countDown();
            }
        });
        latch.await();
    }
    static void testAttackingTask(AsynchronousChannelGroup group) throws Exception {
        Executor executor = (Executor)group;
        Attack task = new Attack();
        executor.execute(task);
        task.waitUntilDone();
        if (!task.failedDueToSecurityException())
            throw new RuntimeException("SecurityException expected");
    }
}

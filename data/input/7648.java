public class Restart {
    static final Random rand = new Random();
    public static void main(String[] args) throws Exception {
        final ThreadGroup tg = new ThreadGroup("test");
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        final Thread.UncaughtExceptionHandler ueh =
            new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    exceptionCount.incrementAndGet();
                }
            };
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(tg, r);
                t.setUncaughtExceptionHandler(ueh);
                return t;
            }
        };
        int nThreads = 1 + rand.nextInt(4);
        AsynchronousChannelGroup group =
            AsynchronousChannelGroup.withFixedThreadPool(nThreads, factory);
        testRestart(group, 100);
        group.shutdown();
        ExecutorService pool = Executors.newCachedThreadPool(factory);
        group = AsynchronousChannelGroup.withCachedThreadPool(pool, rand.nextInt(5));
        testRestart(group, 100);
        group.shutdown();
        Thread.sleep(3000);
        int actual = exceptionCount.get();
        if (actual != 200)
            throw new RuntimeException(actual + " exceptions, expected: " + 200);
    }
    static void testRestart(AsynchronousChannelGroup group, int count)
        throws Exception
    {
        AsynchronousServerSocketChannel listener =
            AsynchronousServerSocketChannel.open(group)
                .bind(new InetSocketAddress(0));
        for (int i=0; i<count; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            listener.accept((Void)null, new CompletionHandler<AsynchronousSocketChannel,Void>() {
                public void completed(AsynchronousSocketChannel ch, Void att) {
                    try {
                        ch.close();
                    } catch (IOException ignore) { }
                    latch.countDown();
                    if (rand.nextBoolean()) {
                        throw new Error();
                    } else {
                        throw new RuntimeException();
                    }
                }
                public void failed(Throwable exc, Void att) {
                }
            });
            int port = ((InetSocketAddress)(listener.getLocalAddress())).getPort();
            AsynchronousSocketChannel ch = AsynchronousSocketChannel.open();
            InetAddress lh = InetAddress.getLocalHost();
            ch.connect(new InetSocketAddress(lh, port)).get();
            ch.close();
            latch.await();
        }
        listener.close();
    }
}

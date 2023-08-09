public class RemovePollRace {
    int count = 1024 * 1024;
    final Map<String,String> results = new ConcurrentHashMap<String,String>();
    Collection<Queue<Boolean>> concurrentQueues() {
        List<Queue<Boolean>> queues = new ArrayList<Queue<Boolean>>();
        queues.add(new ConcurrentLinkedDeque<Boolean>());
        queues.add(new ConcurrentLinkedQueue<Boolean>());
        queues.add(new ArrayBlockingQueue<Boolean>(count, false));
        queues.add(new ArrayBlockingQueue<Boolean>(count, true));
        queues.add(new LinkedBlockingQueue<Boolean>());
        queues.add(new LinkedBlockingDeque<Boolean>());
        queues.add(new LinkedTransferQueue<Boolean>());
        Collections.shuffle(queues);
        return queues;
    }
    void prettyPrintResults() {
        List<String> classNames = new ArrayList<String>(results.keySet());
        Collections.sort(classNames);
        int maxClassNameLength = 0;
        int maxNanosLength = 0;
        for (String name : classNames) {
            if (maxClassNameLength < name.length())
                maxClassNameLength = name.length();
            if (maxNanosLength < results.get(name).length())
                maxNanosLength = results.get(name).length();
        }
        String format = String.format("%%%ds %%%ds nanos/item%%n",
                                      maxClassNameLength, maxNanosLength);
        for (String name : classNames)
            System.out.printf(format, name, results.get(name));
    }
    void test(String[] args) throws Throwable {
        if (args.length > 0)
            count = Integer.valueOf(args[0]);
        for (Queue<Boolean> queue : concurrentQueues())
            test(queue);
        results.clear();
        for (Queue<Boolean> queue : concurrentQueues())
            test(queue);
        prettyPrintResults();
    }
    void await(CountDownLatch latch) {
        try { latch.await(); }
        catch (InterruptedException e) { unexpected(e); }
    }
    void test(final Queue<Boolean> q) throws Throwable {
        long t0 = System.nanoTime();
        final int SPINS = 5;
        final AtomicLong removes = new AtomicLong(0);
        final AtomicLong polls = new AtomicLong(0);
        final int adderCount =
            Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
        final int removerCount =
            Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
        final int pollerCount = removerCount;
        final int threadCount = adderCount + removerCount + pollerCount;
        final CountDownLatch startingGate = new CountDownLatch(1);
        final CountDownLatch addersDone = new CountDownLatch(adderCount);
        final Runnable remover = new Runnable() {
            public void run() {
                await(startingGate);
                int spins = 0;
                for (;;) {
                    boolean quittingTime = (addersDone.getCount() == 0);
                    if (q.remove(Boolean.TRUE))
                        removes.getAndIncrement();
                    else if (quittingTime)
                        break;
                    else if (++spins > SPINS) {
                        Thread.yield();
                        spins = 0;
                    }}}};
        final Runnable poller = new Runnable() {
            public void run() {
                await(startingGate);
                int spins = 0;
                for (;;) {
                    boolean quittingTime = (addersDone.getCount() == 0);
                    if (q.poll() == Boolean.TRUE)
                        polls.getAndIncrement();
                    else if (quittingTime)
                        break;
                    else if (++spins > SPINS) {
                        Thread.yield();
                        spins = 0;
                    }}}};
        final Runnable adder = new Runnable() {
            public void run() {
                await(startingGate);
                for (int i = 0; i < count; i++) {
                    for (;;) {
                        try { q.add(Boolean.TRUE); break; }
                        catch (IllegalStateException e) { Thread.yield(); }
                    }
                }
                addersDone.countDown();
            }};
        final List<Thread> adders   = new ArrayList<Thread>();
        final List<Thread> removers = new ArrayList<Thread>();
        final List<Thread> pollers  = new ArrayList<Thread>();
        for (int i = 0; i < adderCount; i++)
            adders.add(checkedThread(adder));
        for (int i = 0; i < removerCount; i++)
            removers.add(checkedThread(remover));
        for (int i = 0; i < pollerCount; i++)
            pollers.add(checkedThread(poller));
        final List<Thread> allThreads = new ArrayList<Thread>();
        allThreads.addAll(removers);
        allThreads.addAll(pollers);
        allThreads.addAll(adders);
        for (Thread t : allThreads)
            t.start();
        startingGate.countDown();
        for (Thread t : allThreads)
            t.join();
        String className = q.getClass().getSimpleName();
        long elapsed = System.nanoTime() - t0;
        int nanos = (int) ((double) elapsed / (adderCount * count));
        results.put(className, String.valueOf(nanos));
        if (removes.get() + polls.get() != adderCount * count) {
            String msg = String.format
                ("class=%s removes=%s polls=%d count=%d",
                 className, removes.get(), polls.get(), count);
            fail(msg);
        }
    }
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        new RemovePollRace().instanceMain(args);}
    public void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
    Thread checkedThread(final Runnable r) {
        return new Thread() {public void run() {
            try {r.run();} catch (Throwable t) {unexpected(t);}}};}
}

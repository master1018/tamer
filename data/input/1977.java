public class GCRetention {
    int count = 1024 * 1024;
    final Map<String,String> results = new ConcurrentHashMap<String,String>();
    Collection<Queue<Boolean>> queues() {
        List<Queue<Boolean>> queues = new ArrayList<Queue<Boolean>>();
        queues.add(new ConcurrentLinkedDeque<Boolean>());
        queues.add(new ConcurrentLinkedQueue<Boolean>());
        queues.add(new ArrayBlockingQueue<Boolean>(count, false));
        queues.add(new ArrayBlockingQueue<Boolean>(count, true));
        queues.add(new LinkedBlockingQueue<Boolean>());
        queues.add(new LinkedBlockingDeque<Boolean>());
        queues.add(new PriorityBlockingQueue<Boolean>());
        queues.add(new PriorityQueue<Boolean>());
        queues.add(new LinkedList<Boolean>());
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
    void test(String[] args) {
        if (args.length > 0)
            count = Integer.valueOf(args[0]);
        for (Queue<Boolean> queue : queues())
            test(queue);
        results.clear();
        for (Queue<Boolean> queue : queues())
            test(queue);
        prettyPrintResults();
    }
    void test(Queue<Boolean> q) {
        long t0 = System.nanoTime();
        for (int i = 0; i < count; i++)
            check(q.add(Boolean.TRUE));
        System.gc();
        System.gc();
        Boolean x;
        while ((x = q.poll()) != null)
            equal(x, Boolean.TRUE);
        check(q.isEmpty());
        for (int i = 0; i < 10 * count; i++) {
            for (int k = 0; k < 3; k++)
                check(q.add(Boolean.TRUE));
            for (int k = 0; k < 3; k++)
                if (q.poll() != Boolean.TRUE)
                    fail();
        }
        check(q.isEmpty());
        String className = q.getClass().getSimpleName();
        long elapsed = System.nanoTime() - t0;
        int nanos = (int) ((double) elapsed / (10 * 3 * count));
        results.put(className, String.valueOf(nanos));
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
        new GCRetention().instanceMain(args);}
    public void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}

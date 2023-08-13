public class RacingCows {
    private static void realMain(String[] args) throws Throwable {
        final int iterations = 100000;
        final Integer two = Integer.valueOf(2);
        final Integer three = Integer.valueOf(3);
        final Set<Integer> s1 = new CopyOnWriteArraySet<Integer>();
        final Set<Integer> s2 = new CopyOnWriteArraySet<Integer>();
        s1.add(1);
        final Thread t1 = new CheckedThread() { public void realRun() {
            for (int i = 0; i < iterations; i++) {
                s2.add(two);
                s2.remove(two);
            }}};
        t1.start();
        for (int i = 0; i < iterations; i++) {
            check(! s1.equals(s2));
            check(! s2.equals(s1));
        }
        t1.join();
        final List<Integer> l1 = new CopyOnWriteArrayList<Integer>();
        final List<Integer> l2 = new CopyOnWriteArrayList<Integer>();
        final List<Integer> l3 = new CopyOnWriteArrayList<Integer>();
        l1.add(1);
        final Thread t2 = new CheckedThread() { public void realRun() {
            for (int i = 0; i < iterations; i++) {
                switch (i%2) {
                case 0: l2.add(two);    break;
                case 1: l2.add(0, two); break;
                }
                switch (i%3) {
                case 0: l2.remove(two); break;
                case 1: l2.remove(0);   break;
                case 2: l2.clear();     break;
                }}}};
        t2.start();
        final Thread t3 = new CheckedThread() { public void realRun() {
            l3.add(three);
            for (int i = 0; i < iterations; i++) {
                switch (i%2) {
                case 0: l3.add(two);    break;
                case 1: l3.add(0, two); break;
                }
                switch (i%2) {
                case 0: l3.remove(two); break;
                case 1: l3.remove(0);   break;
                }}}};
        t3.start();
        for (int i = 0; i < iterations; i++) {
            check(! l1.equals(l2));
            check(! l2.equals(l1));
            try { new CopyOnWriteArrayList<Integer>(l2); }
            catch (Throwable t) { unexpected(t); }
            try { new CopyOnWriteArrayList<Integer>().addAllAbsent(l3); }
            catch (Throwable t) { unexpected(t); }
            try { new CopyOnWriteArrayList<Integer>().addAll(l3); }
            catch (Throwable t) { unexpected(t); }
            try { new CopyOnWriteArrayList<Integer>().addAll(0,l3); }
            catch (Throwable t) { unexpected(t); }
        }
        t2.join();
        t3.join();
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
    private abstract static class CheckedThread extends Thread {
        public abstract void realRun() throws Throwable;
        public void run() {
            try { realRun(); } catch (Throwable t) { unexpected(t); }}}
}

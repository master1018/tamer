public class EqualsRace {
    private static void realMain(String[] args) throws Throwable {
        final int iterations = 100000;
        final List<Integer> list = new CopyOnWriteArrayList<Integer>();
        final Integer one = Integer.valueOf(1);
        final List<Integer> oneElementList = Arrays.asList(one);
        final Thread t = new CheckedThread() { public void realRun() {
            for (int i = 0; i < iterations; i++) {
                list.add(one);
                list.remove(one);
            }}};
        t.start();
        for (int i = 0; i < iterations; i++) {
            list.equals(oneElementList);
            list.equals(Collections.EMPTY_LIST);
        }
        t.join();
        check(list.size() == 0);
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

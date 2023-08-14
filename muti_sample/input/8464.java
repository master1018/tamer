public class Iterate {
    private static class Godot implements Delayed {
        public long getDelay(TimeUnit unit) {return Long.MAX_VALUE;}
        public int compareTo(Delayed other) {return 0;}
    }
    private static void realMain(String[] args) throws Throwable {
        Godot[] godots = new Godot[] { new Godot(), new Godot(), new Godot() };
        DelayQueue<Godot> q = new DelayQueue<Godot>(Arrays.asList(godots));
        Iterator<Godot> it = q.iterator();
        q.clear();
        check(it.hasNext());
        equal(it.next(), godots[0]);
        it.remove();
        check(q.isEmpty());
        q.addAll(Arrays.asList(godots));
        it = q.iterator();
        check(it.hasNext());
        it.next();
        equal(it.next(), godots[1]);
        it.remove();
        equal(q.size(), 2);
        check(q.contains(godots[0]));
        check(q.contains(godots[2]));
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
}

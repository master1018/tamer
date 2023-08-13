public class SetFromMap {
    static volatile int passed = 0, failed = 0;
    static void pass() { passed++; }
    static void fail() { failed++; Thread.dumpStack(); }
    static void unexpected(Throwable t) { failed++; t.printStackTrace(); }
    static void check(boolean cond) { if (cond) pass(); else fail(); }
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else {System.out.println(x + " not equal to " + y); fail(); }}
    public static void main(String[] args) throws Throwable {
        try { realMain(); } catch (Throwable t) { unexpected(t); }
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Exception("Some tests failed");
    }
    private static void realMain() throws Throwable {
        try {
            Map<String,Boolean> m = new IdentityHashMap<String,Boolean>();
            Set<String> s = Collections.newSetFromMap(m);
            String foo1 = new String("foo");
            String foo2 = new String("foo");
            String bar = new String("bar");
            check(s.add(foo1));
            check(s.add(foo2));
            check(s.add(bar));
            equal(s.size(), 3);
            check(s.contains(foo1));
            check(s.contains(foo2));
            check(! s.contains(new String(foo1)));
        } catch (Throwable t) { unexpected(t); }
    }
}

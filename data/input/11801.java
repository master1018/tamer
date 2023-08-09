public class NullAtEnd {
    static volatile int passed = 0, failed = 0;
    static void fail(String msg) {
        failed++;
        new AssertionError(msg).printStackTrace();
    }
    static void pass() {
        passed++;
    }
    static void unexpected(Throwable t) {
        failed++;
        t.printStackTrace();
    }
    static void check(boolean condition, String msg) {
        if (condition)
            passed++;
        else
            fail(msg);
    }
    static void check(boolean condition) {
        check(condition, "Assertion failure");
    }
    private static boolean eq(Object x, Object y) {
        return x == null ? y == null : x.equals(y);
    }
    private static final Comparator<String> NULL_AT_END
        = new Comparator<String>() {
            public int compare(String x, String y) {
                if (x == null && y == null) return 0;
                if (x == null && y != null) return 1;
                if (x != null && y == null) return -1;
                return x.compareTo(y);
            }
        };
    public static void main(String[] args) {
        try {
            SortedMap<String,String> m1
                = new TreeMap<String,String>(NULL_AT_END);
            check(eq(m1.put("a", "a"), null));
            check(eq(m1.put("b", "b"), null));
            check(eq(m1.put("c", "c"), null));
            check(eq(m1.put(null, "d"), null));
            SortedMap<String,String> m2 = new TreeMap<String,String>(m1);
            check(eq(m1.lastKey(), null));
            check(eq(m1.get(m1.lastKey()), "d"));
            check(eq(m1.remove(m1.lastKey()), "d"));
            check(eq(m1.lastKey(), "c"));
            check(eq(m2.entrySet().toString(), "[a=a, b=b, c=c, null=d]"));
            SortedMap<String,String> m3 = m2.tailMap("b");
            check(eq(m3.lastKey(), null));
            check(eq(m3.get(m3.lastKey()), "d"));
            check(eq(m3.remove(m3.lastKey()), "d"));
            check(eq(m3.lastKey(), "c"));
        } catch (Throwable t) { unexpected(t); }
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Error("Some tests failed");
    }
}

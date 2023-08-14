public class NullPermissiveComparator {
    static void equal(Map m, String s) {
        equal(m.toString(), s);
    }
    static void realMain(String[] args) throws Throwable {
        final Comparator nullLow = new Comparator() {
                public int compare(Object x, Object y) {
                    return x == y ?  0 :
                        x == null ? -1 :
                        y == null ?  1 :
                        ((Comparable)x).compareTo(y); }};
        final Comparator nullHigh = new Comparator() {
                public int compare(Object x, Object y) {
                    return x == y ?  0 :
                        x == null ?  1 :
                        y == null ? -1 :
                        ((Comparable)x).compareTo(y); }};
        TreeMap m = new TreeMap(nullLow);
        m.put("a", "A");
        m.put("b", "B");
        m.put("c", "C");
        equal(m, "{a=A, b=B, c=C}");
        equal(m.headMap("b"), "{a=A}");
        equal(m.tailMap("b"), "{b=B, c=C}");
        equal(m.headMap(null), "{}");
        equal(m.tailMap(null), "{a=A, b=B, c=C}");
        m.put(null, "NULL");
        equal(m, "{null=NULL, a=A, b=B, c=C}");
        equal(m.headMap("b"), "{null=NULL, a=A}");
        equal(m.tailMap("b"), "{b=B, c=C}");
        equal(m.headMap(null), "{}");
        equal(m.tailMap(null), "{null=NULL, a=A, b=B, c=C}");
        m = new TreeMap(nullHigh);
        m.put("a", "A");
        m.put("b", "B");
        m.put("c", "C");
        equal(m, "{a=A, b=B, c=C}");
        equal(m.headMap("b"), "{a=A}");
        equal(m.tailMap("b"), "{b=B, c=C}");
        equal(m.headMap(null), "{a=A, b=B, c=C}");
        equal(m.tailMap(null), "{}");
        m.put(null, "NULL");
        equal(m, "{a=A, b=B, c=C, null=NULL}");
        equal(m.headMap("b"), "{a=A}");
        equal(m.tailMap("b"), "{b=B, c=C, null=NULL}");
        equal(m.headMap(null), "{a=A, b=B, c=C}");
        equal(m.tailMap(null), "{null=NULL}");
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

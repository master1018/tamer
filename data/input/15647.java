public class ForgetMeNot {
    private static void checkQ(PriorityQueue<Integer> q, Integer...elts) {
        check(Arrays.equals(q.toArray(), elts));
    }
    private static void noMoreElements(final Iterator<Integer> it) {
        for (int j = 0; j < 2; j++) {
            THROWS(NoSuchElementException.class,
                   new Fun() { void f() { it.next(); }});
            check(! it.hasNext());
        }
    }
    private static void removeIsCurrentlyIllegal(final Iterator<Integer> it) {
        for (int j = 0; j < 2; j++) {
            THROWS(IllegalStateException.class,
                   new Fun() { void f() { it.remove(); }});
        }
    }
    private static void remove(Iterator<Integer> it,
                               Queue<Integer> q) {
        int size = q.size();
        it.remove();
        removeIsCurrentlyIllegal(it);
        equal(size, q.size()+1);
    }
    private static void realMain(String[] args) throws Throwable {
        final PriorityQueue<Integer> q = new PriorityQueue<Integer>();
        Iterator<Integer> it;
        checkQ(q);
        check(q.isEmpty());
        check(! q.contains(1));
        it = q.iterator();
        removeIsCurrentlyIllegal(it);
        noMoreElements(it);
        q.clear();
        check(q.isEmpty());
        q.add(1);
        checkQ(q, 1);
        check(! q.isEmpty());
        check(q.contains(1));
        it = q.iterator();
        removeIsCurrentlyIllegal(it);
        check(it.hasNext());
        equal(it.next(), 1);
        noMoreElements(it);
        remove(it, q);
        check(q.isEmpty());
        noMoreElements(it);
        checkQ(q);
        q.clear();
        final Integer[] a = {0, 4, 1, 6, 7, 2, 3}; 
        q.addAll(Arrays.asList(a));
        checkQ(q, a);
        it = q.iterator();
        checkQ(q, a);
        removeIsCurrentlyIllegal(it);
        checkQ(q, a);
        check(it.hasNext());
        removeIsCurrentlyIllegal(it);
        checkQ(q, a);
        check(it.hasNext());
        equal(it.next(), 0);
        equal(it.next(), 4);
        equal(it.next(), 1);
        equal(it.next(), 6);
        check(it.hasNext());
        checkQ(q, a);
        remove(it, q);
        checkQ(q, 0, 3, 1, 4, 7, 2);
        check(it.hasNext());
        removeIsCurrentlyIllegal(it);
        equal(it.next(), 7);
        remove(it, q);
        checkQ(q, 0, 2, 1, 4, 3);
        check(it.hasNext());
        removeIsCurrentlyIllegal(it);
        check(it.hasNext());
        equal(it.next(), 3);
        equal(it.next(), 2);
        check(! it.hasNext());
        remove(it, q);
        checkQ(q, 0, 3, 1, 4);
        check(! it.hasNext());
        noMoreElements(it);
        removeIsCurrentlyIllegal(it);
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
    private static abstract class Fun {abstract void f() throws Throwable;}
    static void THROWS(Class<? extends Throwable> k, Fun... fs) {
        for (Fun f : fs)
            try { f.f(); fail("Expected " + k.getName() + " not thrown"); }
            catch (Throwable t) {
                if (k.isAssignableFrom(t.getClass())) pass();
                else unexpected(t);}}
}

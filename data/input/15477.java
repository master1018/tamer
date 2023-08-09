public class CheckedIdentityMap {
    void test(String[] args) throws Throwable {
        Map<Integer, Integer> m1 = checkedMap(
            new IdentityHashMap<Integer, Integer>(),
            Integer.class, Integer.class);
        Map<Integer, Integer> m2 = checkedMap(
            new IdentityHashMap<Integer, Integer>(),
            Integer.class, Integer.class);
        m1.put(new Integer(1), new Integer(1));
        m2.put(new Integer(1), new Integer(1));
        Map.Entry<Integer, Integer> e1 = m1.entrySet().iterator().next();
        Map.Entry<Integer, Integer> e2 = m2.entrySet().iterator().next();
        check(! e1.equals(e2));
        check(e1.hashCode() == hashCode(e1));
        check(e2.hashCode() == hashCode(e2));
    }
    int hashCode(Map.Entry<?,?> e) {
        return (System.identityHashCode(e.getKey()) ^
                System.identityHashCode(e.getValue()));
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
        new CheckedIdentityMap().instanceMain(args);}
    void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
    abstract class F {abstract void f() throws Throwable;}
    void THROWS(Class<? extends Throwable> k, F... fs) {
        for (F f : fs)
            try {f.f(); fail("Expected " + k.getName() + " not thrown");}
            catch (Throwable t) {
                if (k.isAssignableFrom(t.getClass())) pass();
                else unexpected(t);}}
    Thread checkedThread(final Runnable r) {
        return new Thread() {public void run() {
            try {r.run();} catch (Throwable t) {unexpected(t);}}};}
}

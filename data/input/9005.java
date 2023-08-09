public class T6433170 {
    private void checkEmpty(Collection x) {
        check(x.isEmpty());
        check(x.size() == 0);
        check(x.toArray().length == 0);
    }
    void test(String[] args) throws Throwable {
        test(checkedList(
                 checkedList(new ArrayList(), String.class),
                 Object.class));
        test(checkedSet(
                 checkedSet(new HashSet(), String.class),
                 Object.class));
        test(checkedCollection(
                 checkedCollection(new Vector(), String.class),
                 Object.class));
    }
    void test(final Collection checked) {
        checkEmpty(checked);
        final List mixedList = Arrays.asList("1", 2, "3");
        THROWS(ClassCastException.class,
               new F(){void f(){ checked.addAll(mixedList); }});
        checkEmpty(checked);
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
        new T6433170().instanceMain(args);}
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
}

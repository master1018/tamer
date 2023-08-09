public class Throws {
    private static void realMain(String[] args) throws Throwable {
        final ThreadFactory fac = defaultThreadFactory();
        final ThreadFactory nullFactory = null;
        final RejectedExecutionHandler reh
            = new RejectedExecutionHandler() {
                    public void rejectedExecution(Runnable r,
                                                  ThreadPoolExecutor executor) {}};
        final RejectedExecutionHandler nullHandler = null;
        THROWS(
            NullPointerException.class,
            new Fun(){void f(){ newFixedThreadPool(3, null); }},
            new Fun(){void f(){ newCachedThreadPool(null); }},
            new Fun(){void f(){ newSingleThreadScheduledExecutor(null); }},
            new Fun(){void f(){ newScheduledThreadPool(0, null); }},
            new Fun(){void f(){ unconfigurableExecutorService(null); }},
            new Fun(){void f(){ unconfigurableScheduledExecutorService(null); }},
            new Fun(){void f(){ callable(null, "foo"); }},
            new Fun(){void f(){ callable((Runnable) null); }},
            new Fun(){void f(){ callable((PrivilegedAction<?>) null); }},
            new Fun(){void f(){ callable((PrivilegedExceptionAction<?>) null); }},
            new Fun(){void f(){ privilegedCallable((Callable<?>) null); }},
            new Fun(){void f(){ new ScheduledThreadPoolExecutor(0, nullFactory); }},
            new Fun(){void f(){ new ScheduledThreadPoolExecutor(0, nullFactory, reh); }},
            new Fun(){void f(){ new ScheduledThreadPoolExecutor(0, fac, nullHandler); }});
        THROWS(
            IllegalArgumentException.class,
            new Fun(){void f(){ newFixedThreadPool(-42); }},
            new Fun(){void f(){ newFixedThreadPool(0)  ; }},
            new Fun(){void f(){ newFixedThreadPool(-42, fac); }},
            new Fun(){void f(){ newFixedThreadPool(0,   fac); }},
            new Fun(){void f(){ newScheduledThreadPool(-42); }},
            new Fun(){void f(){ new ScheduledThreadPoolExecutor(-42); }},
            new Fun(){void f(){ new ScheduledThreadPoolExecutor(-42, reh); }},
            new Fun(){void f(){ new ScheduledThreadPoolExecutor(-42, fac, reh); }});
        try { newFixedThreadPool(1).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { newFixedThreadPool(1, fac).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { newSingleThreadExecutor().shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { newCachedThreadPool().shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { newSingleThreadScheduledExecutor().shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { newSingleThreadScheduledExecutor(fac).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { newScheduledThreadPool(0).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { newScheduledThreadPool(0, fac).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { new ScheduledThreadPoolExecutor(0).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { new ScheduledThreadPoolExecutor(0, fac).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
        try { new ScheduledThreadPoolExecutor(0, fac, reh).shutdownNow(); pass(); }
        catch (Throwable t) { unexpected(t); }
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
    private abstract static class Fun {abstract void f() throws Throwable;}
    static void THROWS(Class<? extends Throwable> k, Fun... fs) {
        for (Fun f : fs)
            try { f.f(); fail("Expected " + k.getName() + " not thrown"); }
            catch (Throwable t) {
                if (k.isAssignableFrom(t.getClass())) pass();
                else unexpected(t);}}
}

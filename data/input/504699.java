public class ExecutorsTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(ExecutorsTest.class);
    }
    static class TimedCallable<T> implements Callable<T> {
        private final ExecutorService exec;
        private final Callable<T> func;
        private final long msecs;
        TimedCallable(ExecutorService exec, Callable<T> func, long msecs) {
            this.exec = exec;
            this.func = func;
            this.msecs = msecs;
        }
        public T call() throws Exception {
            Future<T> ftask = exec.submit(func);
            try {
                return ftask.get(msecs, TimeUnit.MILLISECONDS);
            } finally {
                ftask.cancel(true);
            }
        }
    }
    private static class Fib implements Callable<BigInteger> {
        private final BigInteger n;
        Fib(long n) {
            if (n < 0) throw new IllegalArgumentException("need non-negative arg, but got " + n);
            this.n = BigInteger.valueOf(n);
        }
        public BigInteger call() {
            BigInteger f1 = BigInteger.ONE;
            BigInteger f2 = f1;
            for (BigInteger i = BigInteger.ZERO; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
                BigInteger t = f1.add(f2);
                f1 = f2;
                f2 = t;
            }
            return f1;
        }
    };
    public void testNewCachedThreadPool1() {
        ExecutorService e = Executors.newCachedThreadPool();
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        joinPool(e);
    }
    public void testNewCachedThreadPool2() {
        ExecutorService e = Executors.newCachedThreadPool(new SimpleThreadFactory());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        joinPool(e);
    }
    public void testNewCachedThreadPool3() {
        try {
            ExecutorService e = Executors.newCachedThreadPool(null);
            shouldThrow();
        }
        catch(NullPointerException success) {
        }
    }
    public void testNewSingleThreadExecutor1() {
        ExecutorService e = Executors.newSingleThreadExecutor();
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        joinPool(e);
    }
    public void testNewSingleThreadExecutor2() {
        ExecutorService e = Executors.newSingleThreadExecutor(new SimpleThreadFactory());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        joinPool(e);
    }
    public void testNewSingleThreadExecutor3() {
        try {
            ExecutorService e = Executors.newSingleThreadExecutor(null);
            shouldThrow();
        }
        catch(NullPointerException success) {
        }
    }
    public void testCastNewSingleThreadExecutor() {
        ExecutorService e = Executors.newSingleThreadExecutor();
        try {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor)e;
        } catch (ClassCastException success) {
        } finally {
            joinPool(e);
        }
    }
    public void testNewFixedThreadPool1() {
        ExecutorService e = Executors.newFixedThreadPool(2);
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        joinPool(e);
    }
    public void testNewFixedThreadPool2() {
        ExecutorService e = Executors.newFixedThreadPool(2, new SimpleThreadFactory());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        joinPool(e);
    }
    public void testNewFixedThreadPool3() {
        try {
            ExecutorService e = Executors.newFixedThreadPool(2, null);
            shouldThrow();
        }
        catch(NullPointerException success) {
        }
    }
    public void testNewFixedThreadPool4() {
        try {
            ExecutorService e = Executors.newFixedThreadPool(0);
            shouldThrow();
        }
        catch(IllegalArgumentException success) {
        }
    }
    public void testunconfigurableExecutorService() {
        ExecutorService e = Executors.unconfigurableExecutorService(Executors.newFixedThreadPool(2));
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        e.execute(new NoOpRunnable());
        joinPool(e);
    }
    public void testunconfigurableExecutorServiceNPE() {
        try {
            ExecutorService e = Executors.unconfigurableExecutorService(null);
        }
        catch (NullPointerException success) {
        }
    }
    public void testunconfigurableScheduledExecutorServiceNPE() {
        try {
            ExecutorService e = Executors.unconfigurableScheduledExecutorService(null);
        }
        catch (NullPointerException success) {
        }
    }
    public void testNewSingleThreadScheduledExecutor() {
        try {
            TrackedCallable callable = new TrackedCallable();
            ScheduledExecutorService p1 = Executors.newSingleThreadScheduledExecutor();
            Future f = p1.schedule(callable, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertFalse(callable.done);
            Thread.sleep(MEDIUM_DELAY_MS);
            assertTrue(callable.done);
            assertEquals(Boolean.TRUE, f.get());
            joinPool(p1);
        } catch(RejectedExecutionException e){}
        catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    public void testnewScheduledThreadPool() {
        try {
            TrackedCallable callable = new TrackedCallable();
            ScheduledExecutorService p1 = Executors.newScheduledThreadPool(2);
            Future f = p1.schedule(callable, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertFalse(callable.done);
            Thread.sleep(MEDIUM_DELAY_MS);
            assertTrue(callable.done);
            assertEquals(Boolean.TRUE, f.get());
            joinPool(p1);
        } catch(RejectedExecutionException e){}
        catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    public void testunconfigurableScheduledExecutorService() {
        try {
            TrackedCallable callable = new TrackedCallable();
            ScheduledExecutorService p1 = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(2));
            Future f = p1.schedule(callable, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertFalse(callable.done);
            Thread.sleep(MEDIUM_DELAY_MS);
            assertTrue(callable.done);
            assertEquals(Boolean.TRUE, f.get());
            joinPool(p1);
        } catch(RejectedExecutionException e){}
        catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    public void testTimedCallable() {
        int N = 10000;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<Callable<BigInteger>> tasks = new ArrayList<Callable<BigInteger>>(N);
        try {
            long startTime = System.currentTimeMillis();
            long i = 0;
            while (tasks.size() < N) {
                tasks.add(new TimedCallable<BigInteger>(executor, new Fib(i), 1));
                i += 10;
            }
            int iters = 0;
            BigInteger sum = BigInteger.ZERO;
            for (Iterator<Callable<BigInteger>> it = tasks.iterator(); it.hasNext();) {
                try {
                    ++iters;
                    sum = sum.add(it.next().call());
                }
                catch (TimeoutException success) {
                    assertTrue(iters > 0);
                    return;
                }
                catch (Exception e) {
                    unexpectedException();
                }
            }
            long elapsed = System.currentTimeMillis() - startTime;
            assertTrue(elapsed < N);
        }
        finally {
            joinPool(executor);
        }
    }
    public void testDefaultThreadFactory() {
        final ThreadGroup egroup = Thread.currentThread().getThreadGroup();
        Runnable r = new Runnable() {
                public void run() {
		    try {
			Thread current = Thread.currentThread();
			threadAssertTrue(!current.isDaemon());
			threadAssertTrue(current.getPriority() <= Thread.NORM_PRIORITY);
			ThreadGroup g = current.getThreadGroup();
			SecurityManager s = System.getSecurityManager();
			if (s != null)
			    threadAssertTrue(g == s.getThreadGroup());
			else
			    threadAssertTrue(g == egroup);
			String name = current.getName();
			threadAssertTrue(name.endsWith("thread-1"));
		    } catch (SecurityException ok) {
		    }
                }
            };
        ExecutorService e = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
        e.execute(r);
        try {
            e.shutdown();
        } catch(SecurityException ok) {
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
        } catch (Exception eX) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testPrivilegedThreadFactory() {
        Policy savedPolicy = null;
        try {
            savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy();
            policy.addPermission(new RuntimePermission("getContextClassLoader"));
            policy.addPermission(new RuntimePermission("setContextClassLoader"));
            Policy.setPolicy(policy);
        } catch (AccessControlException ok) {
            return;
        }
        final ThreadGroup egroup = Thread.currentThread().getThreadGroup();
        final ClassLoader thisccl = Thread.currentThread().getContextClassLoader();
        final AccessControlContext thisacc = AccessController.getContext();
        Runnable r = new Runnable() {
                public void run() {
		    try {
			Thread current = Thread.currentThread();
			threadAssertTrue(!current.isDaemon());
			threadAssertTrue(current.getPriority() <= Thread.NORM_PRIORITY);
			ThreadGroup g = current.getThreadGroup();
			SecurityManager s = System.getSecurityManager();
			if (s != null)
			    threadAssertTrue(g == s.getThreadGroup());
			else
			    threadAssertTrue(g == egroup);
			String name = current.getName();
			threadAssertTrue(name.endsWith("thread-1"));
			threadAssertTrue(thisccl == current.getContextClassLoader());
			threadAssertTrue(thisacc.equals(AccessController.getContext()));
		    } catch(SecurityException ok) {
		    }
                }
            };
        ExecutorService e = Executors.newSingleThreadExecutor(Executors.privilegedThreadFactory());
        Policy.setPolicy(savedPolicy);
        e.execute(r);
        try {
            e.shutdown();
        } catch(SecurityException ok) {
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
        } catch (Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    void checkCCL() {
            AccessController.getContext().checkPermission(new RuntimePermission("getContextClassLoader"));
    }
    class CheckCCL implements Callable<Object> {
        public Object call() {
            checkCCL();
            return null;
        }
    }
    public void testCreatePrivilegedCallableUsingCCLWithNoPrivs() {
        Policy savedPolicy = null;
        try {
            savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy();
            Policy.setPolicy(policy);
        } catch (AccessControlException ok) {
            return;
        }
        try {
            checkCCL();
            Policy.setPolicy(savedPolicy);
            return;
        } catch(AccessControlException ok) {
        }
        try {
            Callable task = Executors.privilegedCallableUsingCurrentClassLoader(new NoOpCallable());
            shouldThrow();
        } catch(AccessControlException success) {
        } catch(Exception ex) {
            unexpectedException();
        }
        finally {
            Policy.setPolicy(savedPolicy);
        }
    }
    public void testprivilegedCallableUsingCCLWithPrivs() {
        Policy savedPolicy = null;
        try {
            savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy();
            policy.addPermission(new RuntimePermission("getContextClassLoader"));
            policy.addPermission(new RuntimePermission("setContextClassLoader"));
            Policy.setPolicy(policy);
        } catch (AccessControlException ok) {
            return;
        }
        try {
            Callable task = Executors.privilegedCallableUsingCurrentClassLoader(new NoOpCallable());
            task.call();
        } catch(Exception ex) {
            unexpectedException();
        }
        finally {
            Policy.setPolicy(savedPolicy);
        }
    }
    public void testprivilegedCallableWithNoPrivs() {
        Callable task;
        Policy savedPolicy = null;
        AdjustablePolicy policy = null;
        AccessControlContext noprivAcc = null;
        try {
            savedPolicy = Policy.getPolicy();
            policy = new AdjustablePolicy();
            Policy.setPolicy(policy);
            noprivAcc = AccessController.getContext();
            task = Executors.privilegedCallable(new CheckCCL());
            Policy.setPolicy(savedPolicy);
        } catch (AccessControlException ok) {
            return; 
        }
        try {
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        checkCCL();
                        return null;
                    }}, noprivAcc);
            return;
        } catch(AccessControlException ok) {
        }
        try {
            task.call();
            shouldThrow();
        } catch(AccessControlException success) {
        } catch(Exception ex) {
            unexpectedException();
        }
    }
    public void testprivilegedCallableWithPrivs() {
        Policy savedPolicy = null;
        try {
            savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy();
            policy.addPermission(new RuntimePermission("getContextClassLoader"));
            policy.addPermission(new RuntimePermission("setContextClassLoader"));
            Policy.setPolicy(policy);
        } catch (AccessControlException ok) {
            return;
        }
        Callable task = Executors.privilegedCallable(new CheckCCL());
        try {
            task.call();
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            Policy.setPolicy(savedPolicy);
        }
    }
    public void testCallable1() {
        try {
            Callable c = Executors.callable(new NoOpRunnable());
            assertNull(c.call());
        } catch(Exception ex) {
            unexpectedException();
        }
    }
    public void testCallable2() {
        try {
            Callable c = Executors.callable(new NoOpRunnable(), one);
            assertEquals(one, c.call());
        } catch(Exception ex) {
            unexpectedException();
        }
    }
    public void testCallable3() {
        try {
            Callable c = Executors.callable(new PrivilegedAction() {
                    public Object run() { return one; }});
        assertEquals(one, c.call());
        } catch(Exception ex) {
            unexpectedException();
        }
    }
    public void testCallable4() {
        try {
            Callable c = Executors.callable(new PrivilegedExceptionAction() {
                    public Object run() { return one; }});
            assertEquals(one, c.call());
        } catch(Exception ex) {
            unexpectedException();
        }
    }
    public void testCallableNPE1() {
        try {
            Runnable r = null;
            Callable c = Executors.callable(r);
        } catch (NullPointerException success) {
        }
    }
    public void testCallableNPE2() {
        try {
            Runnable r = null;
            Callable c = Executors.callable(r, one);
        } catch (NullPointerException success) {
        }
    }
    public void testCallableNPE3() {
        try {
            PrivilegedAction r = null;
            Callable c = Executors.callable(r);
        } catch (NullPointerException success) {
        }
    }
    public void testCallableNPE4() {
        try {
            PrivilegedExceptionAction r = null;
            Callable c = Executors.callable(r);
        } catch (NullPointerException success) {
        }
    }
}

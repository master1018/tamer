public class ThreadPoolExecutorTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(ThreadPoolExecutorTest.class);
    }
    static class ExtendedTPE extends ThreadPoolExecutor {
        volatile boolean beforeCalled = false;
        volatile boolean afterCalled = false;
        volatile boolean terminatedCalled = false;
        public ExtendedTPE() {
            super(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        }
        protected void beforeExecute(Thread t, Runnable r) {
            beforeCalled = true;
        }
        protected void afterExecute(Runnable r, Throwable t) {
            afterCalled = true;
        }
        protected void terminated() {
            terminatedCalled = true;
        }
    }
    static class FailingThreadFactory implements ThreadFactory{
        int calls = 0;
        public Thread newThread(Runnable r){
            if (++calls > 1) return null;
            return new Thread(r);
        }   
    }
    public void testExecute() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            p1.execute(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(SHORT_DELAY_MS);
                        } catch(InterruptedException e){
                            threadUnexpectedException();
                        }
                    }
                });
            Thread.sleep(SMALL_DELAY_MS);
        } catch(InterruptedException e){
            unexpectedException();
        } 
        joinPool(p1);
    }
    public void testGetActiveCount() {
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getActiveCount());
        p2.execute(new MediumRunnable());
        try {
            Thread.sleep(SHORT_DELAY_MS);
        } catch(Exception e){
            unexpectedException();
        }
        assertEquals(1, p2.getActiveCount());
        joinPool(p2);
    }
    public void testPrestartCoreThread() {
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getPoolSize());
        assertTrue(p2.prestartCoreThread());
        assertEquals(1, p2.getPoolSize());
        assertTrue(p2.prestartCoreThread());
        assertEquals(2, p2.getPoolSize());
        assertFalse(p2.prestartCoreThread());
        assertEquals(2, p2.getPoolSize());
        joinPool(p2);
    }
    public void testPrestartAllCoreThreads() {
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getPoolSize());
        p2.prestartAllCoreThreads();
        assertEquals(2, p2.getPoolSize());
        p2.prestartAllCoreThreads();
        assertEquals(2, p2.getPoolSize());
        joinPool(p2);
    }
    public void testGetCompletedTaskCount() {
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getCompletedTaskCount());
        p2.execute(new ShortRunnable());
        try {
            Thread.sleep(SMALL_DELAY_MS);
        } catch(Exception e){
            unexpectedException();
        }
        assertEquals(1, p2.getCompletedTaskCount());
        try { p2.shutdown(); } catch(SecurityException ok) { return; }
        joinPool(p2);
    }
    public void testGetCorePoolSize() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(1, p1.getCorePoolSize());
        joinPool(p1);
    }
    public void testGetKeepAliveTime() {
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(2, 2, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(1, p2.getKeepAliveTime(TimeUnit.SECONDS));
        joinPool(p2);
    }
    public void testGetThreadFactory() {
        ThreadFactory tf = new SimpleThreadFactory();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), tf, new NoOpREHandler());
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }
    public void testSetThreadFactory() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        ThreadFactory tf = new SimpleThreadFactory();
        p.setThreadFactory(tf);
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }
    public void testSetThreadFactoryNull() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            p.setThreadFactory(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(p);
        }
    }
    public void testGetRejectedExecutionHandler() {
        RejectedExecutionHandler h = new NoOpREHandler();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), h);
        assertSame(h, p.getRejectedExecutionHandler());
        joinPool(p);
    }
    public void testSetRejectedExecutionHandler() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        RejectedExecutionHandler h = new NoOpREHandler();
        p.setRejectedExecutionHandler(h);
        assertSame(h, p.getRejectedExecutionHandler());
        joinPool(p);
    }
    public void testSetRejectedExecutionHandlerNull() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            p.setRejectedExecutionHandler(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(p);
        }
    }
    public void testGetLargestPoolSize() {
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            assertEquals(0, p2.getLargestPoolSize());
            p2.execute(new MediumRunnable());
            p2.execute(new MediumRunnable());
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, p2.getLargestPoolSize());
        } catch(Exception e){
            unexpectedException();
        } 
        joinPool(p2);
    }
    public void testGetMaximumPoolSize() {
        ThreadPoolExecutor p2 = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(2, p2.getMaximumPoolSize());
        joinPool(p2);
    }
    public void testGetPoolSize() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p1.getPoolSize());
        p1.execute(new MediumRunnable());
        assertEquals(1, p1.getPoolSize());
        joinPool(p1);
    }
    public void testGetTaskCount() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            assertEquals(0, p1.getTaskCount());
            p1.execute(new MediumRunnable());
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, p1.getTaskCount());
        } catch(Exception e){
            unexpectedException();
        } 
        joinPool(p1);
    }
    public void testIsShutdown() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertFalse(p1.isShutdown());
        try { p1.shutdown(); } catch(SecurityException ok) { return; }
        assertTrue(p1.isShutdown());
        joinPool(p1);
    }
    public void testIsTerminated() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertFalse(p1.isTerminated());
        try {
            p1.execute(new MediumRunnable());
        } finally {
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
        }
        try {
            assertTrue(p1.awaitTermination(LONG_DELAY_MS, TimeUnit.MILLISECONDS));
            assertTrue(p1.isTerminated());
        } catch(Exception e){
            unexpectedException();
        }        
    }
    public void testIsTerminating() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertFalse(p1.isTerminating());
        try {
            p1.execute(new SmallRunnable());
            assertFalse(p1.isTerminating());
        } finally {
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
        }
        try {
            assertTrue(p1.awaitTermination(LONG_DELAY_MS, TimeUnit.MILLISECONDS));
            assertTrue(p1.isTerminated());
            assertFalse(p1.isTerminating());
        } catch(Exception e){
            unexpectedException();
        }        
    }
    public void testGetQueue() {
        BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(10);
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, q);
        FutureTask[] tasks = new FutureTask[5];
        for(int i = 0; i < 5; i++){
            tasks[i] = new FutureTask(new MediumPossiblyInterruptedRunnable(), Boolean.TRUE);
            p1.execute(tasks[i]);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            BlockingQueue<Runnable> wq = p1.getQueue();
            assertSame(q, wq);
            assertFalse(wq.contains(tasks[0]));
            assertTrue(wq.contains(tasks[4]));
            for (int i = 1; i < 5; ++i)
                tasks[i].cancel(true);
            p1.shutdownNow();
        } catch(Exception e) {
            unexpectedException();
        } finally {
            joinPool(p1);
        }
    }
    public void testRemove() {
        BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(10);
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, q);
        FutureTask[] tasks = new FutureTask[5];
        for(int i = 0; i < 5; i++){
            tasks[i] = new FutureTask(new MediumPossiblyInterruptedRunnable(), Boolean.TRUE);
            p1.execute(tasks[i]);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(p1.remove(tasks[0]));
            assertTrue(q.contains(tasks[4]));
            assertTrue(q.contains(tasks[3]));
            assertTrue(p1.remove(tasks[4]));
            assertFalse(p1.remove(tasks[4]));
            assertFalse(q.contains(tasks[4]));
            assertTrue(q.contains(tasks[3]));
            assertTrue(p1.remove(tasks[3]));
            assertFalse(q.contains(tasks[3]));
        } catch(Exception e) {
            unexpectedException();
        } finally {
            joinPool(p1);
        }
    }
    public void testPurge() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        FutureTask[] tasks = new FutureTask[5];
        for(int i = 0; i < 5; i++){
            tasks[i] = new FutureTask(new MediumPossiblyInterruptedRunnable(), Boolean.TRUE);
            p1.execute(tasks[i]);
        }
        tasks[4].cancel(true);
        tasks[3].cancel(true);
        p1.purge();
        long count = p1.getTaskCount();
        assertTrue(count >= 2 && count < 5);
        joinPool(p1);
    }
    public void testShutDownNow() {
        ThreadPoolExecutor p1 = new ThreadPoolExecutor(1, 1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        List l;
        try {
            for(int i = 0; i < 5; i++)
                p1.execute(new MediumPossiblyInterruptedRunnable());
        }
        finally {
            try {
                l = p1.shutdownNow();
            } catch (SecurityException ok) { return; }
        }
        assertTrue(p1.isShutdown());
        assertTrue(l.size() <= 4);
    }
    public void testConstructor1() {
        try {
            new ThreadPoolExecutor(-1,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor2() {
        try {
            new ThreadPoolExecutor(1,-1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor3() {
        try {
            new ThreadPoolExecutor(1,0,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor4() {
        try {
            new ThreadPoolExecutor(1,2,-1L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor5() {
        try {
            new ThreadPoolExecutor(2,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructorNullPointerException() {
        try {
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,null);
            shouldThrow();
        }
        catch (NullPointerException success){}  
    }
    public void testConstructor6() {
        try {
            new ThreadPoolExecutor(-1,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        } catch (IllegalArgumentException success){}
    }
    public void testConstructor7() {
        try {
            new ThreadPoolExecutor(1,-1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor8() {
        try {
            new ThreadPoolExecutor(1,0,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor9() {
        try {
            new ThreadPoolExecutor(1,2,-1L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor10() {
        try {
            new ThreadPoolExecutor(2,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructorNullPointerException2() {
        try {
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,null,new SimpleThreadFactory());
            shouldThrow();
        }
        catch (NullPointerException success){}  
    }
    public void testConstructorNullPointerException3() {
        try {
            ThreadFactory f = null;
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),f);
            shouldThrow();
        }
        catch (NullPointerException success){}  
    }
    public void testConstructor11() {
        try {
            new ThreadPoolExecutor(-1,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor12() {
        try {
            new ThreadPoolExecutor(1,-1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor13() {
        try {
            new ThreadPoolExecutor(1,0,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor14() {
        try {
            new ThreadPoolExecutor(1,2,-1L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor15() {
        try {
            new ThreadPoolExecutor(2,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructorNullPointerException4() {
        try {
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,null,new NoOpREHandler());
            shouldThrow();
        }
        catch (NullPointerException success){}  
    }
    public void testConstructorNullPointerException5() {
        try {
            RejectedExecutionHandler r = null;
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),r);
            shouldThrow();
        }
        catch (NullPointerException success){}  
    }
    public void testConstructor16() {
        try {
            new ThreadPoolExecutor(-1,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor17() {
        try {
            new ThreadPoolExecutor(1,-1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor18() {
        try {
            new ThreadPoolExecutor(1,0,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor19() {
        try {
            new ThreadPoolExecutor(1,2,-1L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructor20() {
        try {
            new ThreadPoolExecutor(2,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        }
        catch (IllegalArgumentException success){}
    }
    public void testConstructorNullPointerException6() {
        try {
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,null,new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        }
        catch (NullPointerException success){}  
    }
    public void testConstructorNullPointerException7() {
        try {
            RejectedExecutionHandler r = null;
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),r);
            shouldThrow();
        }
        catch (NullPointerException success){}  
    }
    public void testConstructorNullPointerException8() {
        try {
            ThreadFactory f = null;
            new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),f,new NoOpREHandler());
            shouldThrow();
        }
        catch (NullPointerException successdn8){}  
    }
    public void testSaturatedExecute() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
        try {
            for(int i = 0; i < 5; ++i){
                p.execute(new MediumRunnable());
            }
            shouldThrow();
        } catch(RejectedExecutionException success){}
        joinPool(p);
    }
    public void testSaturatedExecute2() {
        RejectedExecutionHandler h = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try {
            TrackedNoOpRunnable[] tasks = new TrackedNoOpRunnable[5];
            for(int i = 0; i < 5; ++i){
                tasks[i] = new TrackedNoOpRunnable();
            }
            TrackedLongRunnable mr = new TrackedLongRunnable();
            p.execute(mr);
            for(int i = 0; i < 5; ++i){
                p.execute(tasks[i]);
            }
            for(int i = 1; i < 5; ++i) {
                assertTrue(tasks[i].done);
            }
            try { p.shutdownNow(); } catch(SecurityException ok) { return; }
        } catch(RejectedExecutionException ex){
            unexpectedException();
        } finally {
            joinPool(p);
        }
    }
    public void testSaturatedExecute3() {
        RejectedExecutionHandler h = new ThreadPoolExecutor.DiscardPolicy();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try {
            TrackedNoOpRunnable[] tasks = new TrackedNoOpRunnable[5];
            for(int i = 0; i < 5; ++i){
                tasks[i] = new TrackedNoOpRunnable();
            }
            p.execute(new TrackedLongRunnable());
            for(int i = 0; i < 5; ++i){
                p.execute(tasks[i]);
            }
            for(int i = 0; i < 5; ++i){
                assertFalse(tasks[i].done);
            }
            try { p.shutdownNow(); } catch(SecurityException ok) { return; }
        } catch(RejectedExecutionException ex){
            unexpectedException();
        } finally {
            joinPool(p);
        }
    }
    public void testSaturatedExecute4() {
        RejectedExecutionHandler h = new ThreadPoolExecutor.DiscardOldestPolicy();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try {
            p.execute(new TrackedLongRunnable());
            TrackedLongRunnable r2 = new TrackedLongRunnable();
            p.execute(r2);
            assertTrue(p.getQueue().contains(r2));
            TrackedNoOpRunnable r3 = new TrackedNoOpRunnable();
            p.execute(r3);
            assertFalse(p.getQueue().contains(r2));
            assertTrue(p.getQueue().contains(r3));
            try { p.shutdownNow(); } catch(SecurityException ok) { return; }
        } catch(RejectedExecutionException ex){
            unexpectedException();
        } finally {
            joinPool(p);
        }
    }
    public void testRejectedExecutionExceptionOnShutdown() {
        ThreadPoolExecutor tpe = 
            new ThreadPoolExecutor(1,1,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(1));
        try { tpe.shutdown(); } catch(SecurityException ok) { return; }
        try {
            tpe.execute(new NoOpRunnable());
            shouldThrow();
        } catch(RejectedExecutionException success){}
        joinPool(tpe);
    }
    public void testCallerRunsOnShutdown() {
        RejectedExecutionHandler h = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try { p.shutdown(); } catch(SecurityException ok) { return; }
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            p.execute(r);
            assertFalse(r.done);
        } catch(RejectedExecutionException success){
            unexpectedException();
        } finally {
            joinPool(p);
        }
    }
    public void testDiscardOnShutdown() {
        RejectedExecutionHandler h = new ThreadPoolExecutor.DiscardPolicy();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try { p.shutdown(); } catch(SecurityException ok) { return; }
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            p.execute(r);
            assertFalse(r.done);
        } catch(RejectedExecutionException success){
            unexpectedException();
        } finally {
            joinPool(p);
        }
    }
    public void testDiscardOldestOnShutdown() {
        RejectedExecutionHandler h = new ThreadPoolExecutor.DiscardOldestPolicy();
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try { p.shutdown(); } catch(SecurityException ok) { return; }
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            p.execute(r);
            assertFalse(r.done);
        } catch(RejectedExecutionException success){
            unexpectedException();
        } finally {
            joinPool(p);
        }
    }
    public void testExecuteNull() {
        ThreadPoolExecutor tpe = null;
        try {
            tpe = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
            tpe.execute(null);
            shouldThrow();
        } catch(NullPointerException success){}
        joinPool(tpe);
    }
    public void testCorePoolSizeIllegalArgumentException() {
        ThreadPoolExecutor tpe = null;
        try {
            tpe = new ThreadPoolExecutor(1,2,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        } catch(Exception e){}
        try {
            tpe.setCorePoolSize(-1);
            shouldThrow();
        } catch(IllegalArgumentException success){
        } finally {
            try { tpe.shutdown(); } catch(SecurityException ok) { return; }
        }
        joinPool(tpe);
    }   
    public void testMaximumPoolSizeIllegalArgumentException() {
        ThreadPoolExecutor tpe = null;
        try {
            tpe = new ThreadPoolExecutor(2,3,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        } catch(Exception e){}
        try {
            tpe.setMaximumPoolSize(1);
            shouldThrow();
        } catch(IllegalArgumentException success){
        } finally {
            try { tpe.shutdown(); } catch(SecurityException ok) { return; }
        }
        joinPool(tpe);
    }
    public void testMaximumPoolSizeIllegalArgumentException2() {
        ThreadPoolExecutor tpe = null;
        try {
            tpe = new ThreadPoolExecutor(2,3,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        } catch(Exception e){}
        try {
            tpe.setMaximumPoolSize(-1);
            shouldThrow();
        } catch(IllegalArgumentException success){
        } finally {
            try { tpe.shutdown(); } catch(SecurityException ok) { return; }
        }
        joinPool(tpe);
    }
    public void testKeepAliveTimeIllegalArgumentException() {
        ThreadPoolExecutor tpe = null;
        try {
            tpe = new ThreadPoolExecutor(2,3,LONG_DELAY_MS, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        } catch(Exception e){}
        try {
            tpe.setKeepAliveTime(-1,TimeUnit.MILLISECONDS);
            shouldThrow();
        } catch(IllegalArgumentException success){
        } finally {
            try { tpe.shutdown(); } catch(SecurityException ok) { return; }
        }
        joinPool(tpe);
    }
    public void testTerminated() {
        ExtendedTPE tpe = new ExtendedTPE();
        try { tpe.shutdown(); } catch(SecurityException ok) { return; }
        assertTrue(tpe.terminatedCalled);
        joinPool(tpe);
    }
    public void testBeforeAfter() {
        ExtendedTPE tpe = new ExtendedTPE();
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            tpe.execute(r);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(r.done);
            assertTrue(tpe.beforeCalled);
            assertTrue(tpe.afterCalled);
            try { tpe.shutdown(); } catch(SecurityException ok) { return; }
        }
        catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(tpe);
        }
    }
    public void testSubmitCallable() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            Future<String> future = e.submit(new StringTask());
            String result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testSubmitRunnable() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            Future<?> future = e.submit(new NoOpRunnable());
            future.get();
            assertTrue(future.isDone());
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testSubmitRunnable2() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            Future<String> future = e.submit(new NoOpRunnable(), TEST_STRING);
            String result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAny1() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            e.invokeAny(null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAny2() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            e.invokeAny(new ArrayList<Callable<String>>());
        } catch (IllegalArgumentException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAny3() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAny(l);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAny4() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            e.invokeAny(l);
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAny5() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            String result = e.invokeAny(l);
            assertSame(TEST_STRING, result);
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAll1() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            e.invokeAll(null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAll2() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            List<Future<String>> r = e.invokeAll(new ArrayList<Callable<String>>());
            assertTrue(r.isEmpty());
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAll3() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAll(l);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAll4() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            List<Future<String>> result = e.invokeAll(l);
            assertEquals(1, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                it.next().get();
        } catch(ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testInvokeAll5() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            List<Future<String>> result = e.invokeAll(l);
            assertEquals(2, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                assertSame(TEST_STRING, it.next().get());
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAny1() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            e.invokeAny(null, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAnyNullTimeUnit() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            e.invokeAny(l, MEDIUM_DELAY_MS, null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAny2() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            e.invokeAny(new ArrayList<Callable<String>>(), MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (IllegalArgumentException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAny3() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAny(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            ex.printStackTrace();
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAny4() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            e.invokeAny(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch(ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAny5() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            String result = e.invokeAny(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertSame(TEST_STRING, result);
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAll1() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            e.invokeAll(null, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAllNullTimeUnit() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            e.invokeAll(l, MEDIUM_DELAY_MS, null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAll2() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            List<Future<String>> r = e.invokeAll(new ArrayList<Callable<String>>(), MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertTrue(r.isEmpty());
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAll3() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAll(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAll4() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            List<Future<String>> result = e.invokeAll(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(1, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                it.next().get();
        } catch(ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAll5() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            List<Future<String>> result = e.invokeAll(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(2, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                assertSame(TEST_STRING, it.next().get());
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testTimedInvokeAll6() {
        ExecutorService e = new ThreadPoolExecutor(2, 2, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(Executors.callable(new MediumPossiblyInterruptedRunnable(), TEST_STRING));
            l.add(new StringTask());
            List<Future<String>> result = e.invokeAll(l, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(3, result.size());
            Iterator<Future<String>> it = result.iterator(); 
            Future<String> f1 = it.next();
            Future<String> f2 = it.next();
            Future<String> f3 = it.next();
            assertTrue(f1.isDone());
            assertTrue(f2.isDone());
            assertTrue(f3.isDone());
            assertFalse(f1.isCancelled());
            assertTrue(f2.isCancelled());
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testFailingThreadFactory() {
        ExecutorService e = new ThreadPoolExecutor(100, 100, LONG_DELAY_MS, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new FailingThreadFactory());
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            for (int k = 0; k < 100; ++k) {
                e.execute(new NoOpRunnable());
            }
            Thread.sleep(LONG_DELAY_MS);
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
    public void testRejectedRecycledTask() {
        final int nTasks = 1000;
        final AtomicInteger nRun = new AtomicInteger(0);
        final Runnable recycledTask = new Runnable() {
                public void run() {
                    nRun.getAndIncrement();
                } };
        final ThreadPoolExecutor p = 
            new ThreadPoolExecutor(1, 30, 60, TimeUnit.SECONDS, 
                                   new ArrayBlockingQueue(30));
        try {
            for (int i = 0; i < nTasks; ++i) {
                for (;;) {
                    try {
                        p.execute(recycledTask);
                        break;
                    }
                    catch (RejectedExecutionException ignore) {
                    }
                }
            }
            Thread.sleep(5000); 
            assertEquals(nRun.get(), nTasks);
        } catch(Exception ex) {
            ex.printStackTrace();
            unexpectedException();
        } finally {
            p.shutdown();
        }
    }
}

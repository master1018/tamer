public class AbstractQueuedSynchronizerTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AbstractQueuedSynchronizerTest.class);
    }
    static class Mutex extends AbstractQueuedSynchronizer {
        public boolean isHeldExclusively() { return getState() == 1; }
        public boolean tryAcquire(int acquires) {
            assertTrue(acquires == 1); 
            return compareAndSetState(0, 1);
        }
        public boolean tryRelease(int releases) {
            if (getState() == 0) throw new IllegalMonitorStateException();
            setState(0);
            return true;
        }
        public AbstractQueuedSynchronizer.ConditionObject newCondition() { return new AbstractQueuedSynchronizer.ConditionObject(); }
    }
    static class BooleanLatch extends AbstractQueuedSynchronizer { 
        public boolean isSignalled() { return getState() != 0; }
        public int tryAcquireShared(int ignore) {
            return isSignalled()? 1 : -1;
        }
        public boolean tryReleaseShared(int ignore) {
            setState(1);
            return true;
        }
    }
    class InterruptibleSyncRunnable implements Runnable {
        final Mutex sync;
        InterruptibleSyncRunnable(Mutex l) { sync = l; }
        public void run() {
            try {
                sync.acquireInterruptibly(1);
            } catch(InterruptedException success){}
        }
    }
    class InterruptedSyncRunnable implements Runnable {
        final Mutex sync;
        InterruptedSyncRunnable(Mutex l) { sync = l; }
        public void run() {
            try {
                sync.acquireInterruptibly(1);
                threadShouldThrow();
            } catch(InterruptedException success){}
        }
    }
    public void testIsHeldExclusively() { 
        Mutex rl = new Mutex();
        assertFalse(rl.isHeldExclusively());
    }
    public void testAcquire() { 
        Mutex rl = new Mutex();
        rl.acquire(1);
        assertTrue(rl.isHeldExclusively());
        rl.release(1);
        assertFalse(rl.isHeldExclusively());
    }
    public void testTryAcquire() { 
        Mutex rl = new Mutex();
        assertTrue(rl.tryAcquire(1));
        assertTrue(rl.isHeldExclusively());
        rl.release(1);
    }
    public void testhasQueuedThreads() { 
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertFalse(sync.hasQueuedThreads());
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThreads());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThreads());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThreads());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.hasQueuedThreads());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testIsQueuedNPE() { 
        final Mutex sync = new Mutex();
        try {
            sync.isQueued(null);
            shouldThrow();
        } catch (NullPointerException success) {
        }
    }
    public void testIsQueued() { 
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertFalse(sync.isQueued(t1));
            assertFalse(sync.isQueued(t2));
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.isQueued(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.isQueued(t1));
            assertTrue(sync.isQueued(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.isQueued(t1));
            assertTrue(sync.isQueued(t2));
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.isQueued(t1));
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.isQueued(t2));
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testGetFirstQueuedThread() { 
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertNull(sync.getFirstQueuedThread());
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(t1, sync.getFirstQueuedThread());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(t1, sync.getFirstQueuedThread());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(t2, sync.getFirstQueuedThread());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertNull(sync.getFirstQueuedThread());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testHasContended() { 
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertFalse(sync.hasContended());
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testGetQueuedThreads() { 
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertTrue(sync.getQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getQueuedThreads().contains(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getQueuedThreads().contains(t1));
            assertTrue(sync.getQueuedThreads().contains(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.getQueuedThreads().contains(t1));
            assertTrue(sync.getQueuedThreads().contains(t2));
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testGetExclusiveQueuedThreads() { 
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getExclusiveQueuedThreads().contains(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getExclusiveQueuedThreads().contains(t1));
            assertTrue(sync.getExclusiveQueuedThreads().contains(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.getExclusiveQueuedThreads().contains(t1));
            assertTrue(sync.getExclusiveQueuedThreads().contains(t2));
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testGetSharedQueuedThreads() { 
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testInterruptedException2() { 
        final Mutex sync = new Mutex();
        sync.acquire(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        sync.tryAcquireNanos(1, MEDIUM_DELAY_MS * 1000 * 1000);
                        threadShouldThrow();
                    } catch(InterruptedException success){}
                }
            });
        try {
            t.start();
            t.interrupt();
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testTryAcquireWhenSynced() { 
        final Mutex sync = new Mutex();
        sync.acquire(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertFalse(sync.tryAcquire(1));
                }
            });
        try {
            t.start();
            t.join();
            sync.release(1);
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testAcquireNanos_Timeout() { 
        final Mutex sync = new Mutex();
        sync.acquire(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(sync.tryAcquireNanos(1, 1000 * 1000));
                    } catch (Exception ex) {
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            t.join();
            sync.release(1);
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testGetState() {
        final Mutex sync = new Mutex();
        sync.acquire(1);
        assertTrue(sync.isHeldExclusively());
        sync.release(1);
        assertFalse(sync.isHeldExclusively());
        Thread t = new Thread(new Runnable() { 
                public void run() {
                    sync.acquire(1);
                    try {
                        Thread.sleep(SMALL_DELAY_MS);
                    }
                    catch(Exception e) {
                        threadUnexpectedException();
                    }
                    sync.release(1);
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.isHeldExclusively());
            t.join();
            assertFalse(sync.isHeldExclusively());
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testAcquireInterruptibly1() { 
        final Mutex sync = new Mutex();
        sync.acquire(1);
        Thread t = new Thread(new InterruptedSyncRunnable(sync));
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            sync.release(1);
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 
    public void testAcquireInterruptibly2() {
        final Mutex sync = new Mutex();        
        try {
            sync.acquireInterruptibly(1);
        } catch(Exception e) {
            unexpectedException();
        }
        Thread t = new Thread(new InterruptedSyncRunnable(sync));
        try {
            t.start();
            t.interrupt();
            assertTrue(sync.isHeldExclusively());
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testOwns() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        final Mutex sync2 = new Mutex();
        assertTrue(sync.owns(c));
        assertFalse(sync2.owns(c));
    }
    public void testAwait_IllegalMonitor() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        try {
            c.await();
            shouldThrow();
        }
        catch (IllegalMonitorStateException success) {
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testSignal_IllegalMonitor() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        try {
            c.signal();
            shouldThrow();
        }
        catch (IllegalMonitorStateException success) {
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwaitNanos_Timeout() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        try {
            sync.acquire(1);
            long t = c.awaitNanos(100);
            assertTrue(t <= 0);
            sync.release(1);
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwait_Timeout() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        try {
            sync.acquire(1);
            assertFalse(c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            sync.release(1);
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwaitUntil_Timeout() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        try {
            sync.acquire(1);
            java.util.Date d = new java.util.Date();
            assertFalse(c.awaitUntil(new java.util.Date(d.getTime() + 10)));
            sync.release(1);
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwait() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            c.signal();
            sync.release(1);
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testHasWaitersNPE() {
        final Mutex sync = new Mutex();
        try {
            sync.hasWaiters(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitQueueLengthNPE() {
        final Mutex sync = new Mutex();
        try {
            sync.getWaitQueueLength(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitingThreadsNPE() {
        final Mutex sync = new Mutex();
        try {
            sync.getWaitingThreads(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testHasWaitersIAE() {
        final Mutex sync = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject c = (sync.newCondition());
        final Mutex sync2 = new Mutex();
        try {
            sync2.hasWaiters(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testHasWaitersIMSE() {
        final Mutex sync = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject c = (sync.newCondition());
        try {
            sync.hasWaiters(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitQueueLengthIAE() {
        final Mutex sync = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject c = (sync.newCondition());
        final Mutex sync2 = new Mutex();
        try {
            sync2.getWaitQueueLength(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitQueueLengthIMSE() {
        final Mutex sync = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject c = (sync.newCondition());
        try {
            sync.getWaitQueueLength(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitingThreadsIAE() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = (sync.newCondition());
        final Mutex sync2 = new Mutex();        
        try {
            sync2.getWaitingThreads(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitingThreadsIMSE() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = (sync.newCondition());
        try {
            sync.getWaitingThreads(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testHasWaiters() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        threadAssertFalse(sync.hasWaiters(c));
                        threadAssertEquals(0, sync.getWaitQueueLength(c));
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertTrue(sync.hasWaiters(c));
            assertEquals(1, sync.getWaitQueueLength(c));
            c.signal();
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertEquals(0, sync.getWaitQueueLength(c));
            sync.release(1);
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitQueueLength() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t1 = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        threadAssertFalse(sync.hasWaiters(c));
                        threadAssertEquals(0, sync.getWaitQueueLength(c));
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        threadAssertTrue(sync.hasWaiters(c));
                        threadAssertEquals(1, sync.getWaitQueueLength(c));
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertTrue(sync.hasWaiters(c));
            assertEquals(2, sync.getWaitQueueLength(c));
            c.signalAll();
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertEquals(0, sync.getWaitQueueLength(c));
            sync.release(1);
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testGetWaitingThreads() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t1 = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        threadAssertTrue(sync.getWaitingThreads(c).isEmpty());
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        threadAssertFalse(sync.getWaitingThreads(c).isEmpty());
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        try {
            sync.acquire(1);
            assertTrue(sync.getWaitingThreads(c).isEmpty());
            sync.release(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertTrue(sync.hasWaiters(c));
            assertTrue(sync.getWaitingThreads(c).contains(t1));
            assertTrue(sync.getWaitingThreads(c).contains(t2));
            c.signalAll();
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertTrue(sync.getWaitingThreads(c).isEmpty());
            sync.release(1);
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwaitUninterruptibly() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t = new Thread(new Runnable() { 
                public void run() {
                    sync.acquire(1);
                    c.awaitUninterruptibly();
                    sync.release(1);
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            sync.acquire(1);
            c.signal();
            sync.release(1);
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwait_Interrupt() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        c.await();
                        sync.release(1);
                        threadShouldThrow();
                    }
                    catch(InterruptedException success) {
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwaitNanos_Interrupt() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        c.awaitNanos(1000 * 1000 * 1000); 
                        sync.release(1);
                        threadShouldThrow();
                    }
                    catch(InterruptedException success) {
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testAwaitUntil_Interrupt() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        java.util.Date d = new java.util.Date();
                        c.awaitUntil(new java.util.Date(d.getTime() + 10000));
                        sync.release(1);
                        threadShouldThrow();
                    }
                    catch(InterruptedException success) {
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS); 
            t.interrupt();
            t.join(SMALL_DELAY_MS);       
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testSignalAll() {
        final Mutex sync = new Mutex();        
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        Thread t1 = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() { 
                public void run() {
                    try {
                        sync.acquire(1);
                        c.await();
                        sync.release(1);
                    }
                    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            c.signalAll();
            sync.release(1);
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testToString() {
        Mutex sync = new Mutex();
        String us = sync.toString();
        assertTrue(us.indexOf("State = 0") >= 0);
        sync.acquire(1);
        String ls = sync.toString();
        assertTrue(ls.indexOf("State = 1") >= 0);
    }
    public void testSerialization() {
        Mutex l = new Mutex();
        l.acquire(1);
        assertTrue(l.isHeldExclusively());
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            Mutex r = (Mutex) in.readObject();
            assertTrue(r.isHeldExclusively());
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    public void testGetStateWithReleaseShared() {
        final BooleanLatch l = new BooleanLatch();
        assertFalse(l.isSignalled());
        l.releaseShared(0);
        assertTrue(l.isSignalled());
    }
    public void testReleaseShared() {
        final BooleanLatch l = new BooleanLatch();
        assertFalse(l.isSignalled());
        l.releaseShared(0);
        assertTrue(l.isSignalled());
        l.releaseShared(0);
        assertTrue(l.isSignalled());
    }
    public void testAcquireSharedInterruptibly() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        l.acquireSharedInterruptibly(0);
                        threadAssertTrue(l.isSignalled());
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            assertFalse(l.isSignalled());
            Thread.sleep(SHORT_DELAY_MS);
            l.releaseShared(0);
            assertTrue(l.isSignalled());
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    public void testAsquireSharedTimed() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        threadAssertTrue(l.tryAcquireSharedNanos(0, MEDIUM_DELAY_MS* 1000 * 1000));
                        threadAssertTrue(l.isSignalled());
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            assertFalse(l.isSignalled());
            Thread.sleep(SHORT_DELAY_MS);
            l.releaseShared(0);
            assertTrue(l.isSignalled());
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    public void testAcquireSharedInterruptibly_InterruptedException() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        l.acquireSharedInterruptibly(0);
                        threadShouldThrow();
                    } catch(InterruptedException success){}
                }
            });
        t.start();
        try {
            assertFalse(l.isSignalled());
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    public void testAcquireSharedNanos_InterruptedException() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        l.tryAcquireSharedNanos(0, SMALL_DELAY_MS* 1000 * 1000);
                        threadShouldThrow();                        
                    } catch(InterruptedException success){}
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(l.isSignalled());
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    public void testAcquireSharedNanos_Timeout() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        threadAssertFalse(l.tryAcquireSharedNanos(0, SMALL_DELAY_MS* 1000 * 1000));
                    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(l.isSignalled());
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
}

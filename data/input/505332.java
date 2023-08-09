@TestTargetClass(Reference.class) 
public class ReferenceTest extends junit.framework.TestCase {
    Object tmpA, tmpB, tmpC, obj;
    volatile Reference r;
    static TestWeakReference twr;
    static AssertionFailedError error;
    static boolean testObjectFinalized;
    static class TestWeakReference<T> extends WeakReference<T> {
        public volatile boolean clearSeen = false;
        public volatile boolean enqueueSeen = false;
        public TestWeakReference(T referent) {
            super(referent);
        }
        public TestWeakReference(T referent, ReferenceQueue<? super T> q) {
            super(referent, q);
        }
        public void clear() {
            clearSeen = true;
            if (testObjectFinalized) {
                error = new AssertionFailedError("Clear should happen " +
                        "before finalization.");
                throw error;
            }
            if (enqueueSeen) {
                error = new AssertionFailedError("Clear should happen " +
                        "before enqueue.");
                throw error;
            }            
            super.clear();
        }
        public boolean enqueue() {
            enqueueSeen = true;
            if (!clearSeen) {
                error = new AssertionFailedError("Clear should happen " +
                        "before enqueue.");
                throw error;
            }
            return super.enqueue();
        }
    }
    protected void doneSuite() {
        tmpA = tmpB = obj = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        tmpA = new Object();
        tmpB = new Object();
        tmpC = new Object();
        SoftReference sr = new SoftReference(tmpA, new ReferenceQueue());
        WeakReference wr = new WeakReference(tmpB, new ReferenceQueue());
        PhantomReference pr = new PhantomReference(tmpC, new ReferenceQueue());
        assertTrue("Start: Object not cleared.", (sr.get() != null)
                && (wr.get() != null));
        assertNull("Referent is not null.", pr.get());
        sr.clear();
        wr.clear();
        pr.clear();
        assertTrue("End: Object cleared.", (sr.get() == null)
                && (wr.get() == null));
        assertNull("Referent is not null.", pr.get());
        assertTrue("should always pass", tmpA != sr.get() && tmpB != wr.get());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "enqueue",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isEnqueued",
            args = {}
        )
    })
    public void test_enqueue() {
        ReferenceQueue rq = new ReferenceQueue();
        obj = new Object();
        Reference ref = new SoftReference(obj, rq);
        assertTrue("Enqueue failed.", (!ref.isEnqueued())
                && ((ref.enqueue()) && (ref.isEnqueued())));
        assertTrue("Not properly enqueued.", rq.poll().get() == obj);
        assertTrue("Should remain enqueued.", !ref.isEnqueued());
        assertTrue("Can not enqueue twice.", (!ref.enqueue())
                && (rq.poll() == null));
        rq = new ReferenceQueue();
        obj = new Object();
        ref = new WeakReference(obj, rq);
        assertTrue("Enqueue failed2.", (!ref.isEnqueued())
                && ((ref.enqueue()) && (ref.isEnqueued())));
        assertTrue("Not properly enqueued2.", rq.poll().get() == obj);
        assertTrue("Should remain enqueued2.", !ref.isEnqueued()); 
        assertTrue("Can not enqueue twice2.", (!ref.enqueue())
                && (rq.poll() == null));
        ref = new PhantomReference(obj, rq);
        assertTrue("Enqueue failed3.", (!ref.isEnqueued())
                && ((ref.enqueue()) && (ref.isEnqueued())));
        assertNull("Not properly enqueued3.", rq.poll().get());
        assertTrue("Should remain enqueued3.", !ref.isEnqueued()); 
        assertTrue("Can not enqueue twice3.", (!ref.enqueue())
                && (rq.poll() == null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality for WeakReference.",
        method = "get",
        args = {}
    )
    public void test_get_WeakReference() {
        class TestObject {
            public boolean finalized;
            public TestObject() {
                finalized = false;
            }
            protected void finalize() {
                finalized = true;
            }
        }
        final ReferenceQueue rq = new ReferenceQueue();
        class TestThread extends Thread {
            public void run() {
                Object testObj = new TestObject();
                r = new WeakReference(testObj, rq);
                testObj = null;
            }
        }
        Reference ref;
        try {
            TestThread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            ref = rq.remove();
            assertNotNull("Object not garbage collected1.", ref);
            assertTrue("Unexpected ref1", ref == r);
            assertNull("Object could not be reclaimed1.", r.get());
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }
        try {
            TestThread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            ref = rq.poll();
            assertNotNull("Object not garbage collected.", ref);
            assertTrue("Unexpected ref2", ref == r);
            assertNull("Object could not be reclaimed.", ref.get());
            assertNull("Object could not be reclaimed.", r.get());
        } catch (Exception e) {
            fail("Exception : " + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Makes sure that overridden versions of clear() and enqueue()  " +
                    "get called, and that clear/enqueue/finalize happen in the  " +
                    "right order for WeakReferences.",
            method = "clear",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Makes sure that overridden versions of clear() and enqueue()  " +
                    "get called, and that clear/enqueue/finalize happen in the  " +
                    "right order for WeakReferences.",
            method = "enqueue",
            args = {}
        )
    })
    public void test_subclass() {
        error = null;
        testObjectFinalized = false;
        twr = null;
        class TestObject {
            public TestWeakReference testWeakReference = null;
            public void setTestWeakReference(TestWeakReference twr) {
                testWeakReference = twr;
            }
            protected void finalize() {
                testObjectFinalized = true;
            }
        }
        final ReferenceQueue rq = new ReferenceQueue();
        class TestThread extends Thread {
            public void run() {
                TestObject testObj = new TestObject();
                twr = new TestWeakReference(testObj, rq);
                testObj.setTestWeakReference(twr);
                testObj = null;
            }
        }
        Reference ref;
        try {
            Thread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            ref = rq.remove(5000L);    
            assertNotNull("Object not garbage collected.", ref);
            assertTrue("Unexpected reference.", ref == twr);
            assertNull("Object could not be reclaimed.", twr.get());
            assertTrue("finalize() should have been called.",
                    testObjectFinalized);
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't check that get() can return null.",
        method = "get",
        args = {}
    )
    public void test_get() {
        Vector<StringBuffer> vec = new Vector<StringBuffer>();        
        WeakReference ref = new WeakReference(vec, new ReferenceQueue());
        assertTrue("Get succeeded.", ref.get() == vec);
        Runtime rt =  Runtime.getRuntime();
        long beforeTest = rt.freeMemory();
        while(rt.freeMemory() < beforeTest * 2/3) {
            vec.add(new StringBuffer(1000));
        }
        vec = null;
        System.gc();
        System.runFinalization();
        assertNull("get() doesn't return null after gc for WeakReference", 
                    ref.get());
        obj = new Object();
        ref = new WeakReference(obj, new ReferenceQueue());
        ref.clear();
        assertNull("get() doesn't return null after clear for WeakReference", 
                ref.get());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEnqueued",
        args = {}
    )
    public void test_isEnqueued() {
        ReferenceQueue rq = new ReferenceQueue();
        obj = new Object();
        Reference ref = new SoftReference(obj, rq);
        assertTrue("Should start off not enqueued.", !ref.isEnqueued());
        ref.enqueue();
        assertTrue("Should now be enqueued.", ref.isEnqueued());
        ref.enqueue();
        assertTrue("Should still be enqueued.", ref.isEnqueued());
        rq.poll();
        assertTrue("Should now be not enqueued.", !ref.isEnqueued());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Contrives a situation where the only reference to a string  " +
                "is a WeakReference from an object that is being finalized.  " +
                "Checks to make sure that the referent of the WeakReference  " +
                "is still pointing to a valid object.",
        method = "get",
        args = {}
    )
    public void test_finalizeReferenceInteraction() {
        error = null;
        testObjectFinalized = false;
        class TestObject {
            WeakReference<String> stringRef;
            public TestObject(String referent) {
                stringRef = new WeakReference<String>(referent);
            }
            protected void finalize() {
                try {
                    String s = stringRef.get();
                    System.gc();
                    testObjectFinalized = true;
                } catch (Throwable t) {
                    error = new AssertionFailedError("something threw '" + t +
                            "' in finalize()");
                }
            }
        }
        class TestThread extends Thread {
            public void run() {
                TestObject testObj = new TestObject(new String("sup /b/"));
            }
        }
        try {
            Thread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            Thread.sleep(1000);
            if (error != null) {
                throw error;
            }
            assertTrue("finalize() should have been called.",
                    testObjectFinalized);
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

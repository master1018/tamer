@TestTargetClass(PhantomReference.class) 
public class PhantomReferenceTest extends junit.framework.TestCase {
    static Boolean bool;
    public boolean isCalled = false;
    protected void doneSuite() {
        bool = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {}
    )
    public void test_get() {
        ReferenceQueue rq = new ReferenceQueue();
        bool = new Boolean(false);
        PhantomReference pr = new PhantomReference(bool, rq);
        assertNull("get() should return null.", pr.get());
        pr.enqueue();
        assertNull("get() should return null.", pr.get());
        pr.clear();
        assertNull("get() should return null.", pr.get());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Interaction test with Runtime.getRuntime().gc().",
        method = "get",
        args = {}
    )
    public void test_gcInteraction() {
        class TestPhantomReference<T> extends PhantomReference<T> {
            public TestPhantomReference(T referent,
                    ReferenceQueue<? super T> q) {
                super(referent, q);
            }
            public boolean enqueue() {
                Runtime.getRuntime().gc();
                return super.enqueue();
            }
        }
        final ReferenceQueue rq = new ReferenceQueue();
        final PhantomReference[] tprs = new PhantomReference[4];
        class TestThread extends Thread {
            public void run() {
                Object obj = new Object();
                tprs[0] = new TestPhantomReference(obj, rq);
                tprs[1] = new TestPhantomReference(obj, rq);
                tprs[2] = new TestPhantomReference(obj, rq);
                tprs[3] = new TestPhantomReference(obj, rq);
            }
        }
        try {
            Thread t = new TestThread();
            t.start();
            t.join();
            System.gc();
            System.runFinalization();
            assertNull("get() should return null.", tprs[0].get());
            assertNull("get() should return null.", tprs[1].get());
            assertNull("get() should return null.", tprs[2].get());
            assertNull("get() should return null.", tprs[3].get());
            for (int i = 0; i < 4; i++) {
                Reference r = rq.remove(100L);
                assertNotNull("Reference should have been enqueued.", r);
            }
            assertNull("get() should return null.", tprs[0].get());
            assertNull("get() should return null.", tprs[1].get());
            assertNull("get() should return null.", tprs[2].get());
            assertNull("get() should return null.", tprs[3].get());
        } catch (InterruptedException e) {
            fail("InterruptedException : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PhantomReference",
        args = {java.lang.Object.class, java.lang.ref.ReferenceQueue.class}
    )
    public void test_ConstructorLjava_lang_ObjectLjava_lang_ref_ReferenceQueue() {
        ReferenceQueue rq = new ReferenceQueue();
        bool = new Boolean(true);
        try {
            PhantomReference pr = new PhantomReference(bool, rq);
            Thread.sleep(1000);
            assertTrue("Initialization failed.", !pr.isEnqueued());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
        assertTrue("should always pass", bool.booleanValue());
        boolean exception = false;
        try {
            new PhantomReference(bool, null);
        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue("Should not throw NullPointerException", !exception);
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

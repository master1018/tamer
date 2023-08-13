@TestTargetClass(WeakReference.class) 
public class WeakReferenceTest extends junit.framework.TestCase {
    static Boolean bool;
    protected void doneSuite() {
        bool = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "WeakReference",
        args = {java.lang.Object.class, java.lang.ref.ReferenceQueue.class}
    )
    public void test_ConstructorLjava_lang_ObjectLjava_lang_ref_ReferenceQueue() {
        ReferenceQueue rq = new ReferenceQueue();
        bool = new Boolean(true);
        try {
            WeakReference wr = new WeakReference(bool, rq);
            assertTrue("Initialization failed.", ((Boolean) wr.get())
                    .booleanValue());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
        assertTrue("should always pass", bool.booleanValue());
        boolean exception = false;
        try {
            new WeakReference(bool, null);
        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue("Should not throw NullPointerException", !exception);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "WeakReference",
        args = {java.lang.Object.class}
    )
    public void test_ConstructorLjava_lang_Object() {
        bool = new Boolean(true);
        try {
            WeakReference wr = new WeakReference(bool);
            Thread.sleep(1000);
            assertTrue("Initialization failed.", ((Boolean) wr.get())
                    .booleanValue());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
        assertTrue("should always pass", bool.booleanValue());
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}

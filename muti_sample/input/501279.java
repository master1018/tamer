@TestTargetClass(Unsafe.class)
public class UnsafeTest extends TestCase {
    @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "",
            method = "getUnsafe",
            args = {}
    )
    public void test_getUnsafeForbidden() {
        try {
            Unsafe.getUnsafe();
            fail();
        } catch (SecurityException expected) {
        }
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "",
            method = "getUnsafe",
            args = {}
    )
    public void test_getUnsafeForbiddenWithSystemCaller() throws Exception {
        Callable<Object> callable = Executors.callable(new Runnable() {
            public void run() {
                Unsafe.getUnsafe();
            }
        });
        try {
            callable.call();
            fail();
        } catch (SecurityException expected) {
        }
    }
}

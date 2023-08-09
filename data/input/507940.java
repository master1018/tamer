public class LockSupport {
    private LockSupport() {} 
    private static final Unsafe unsafe = UnsafeAccess.THE_ONE;
    public static void unpark(Thread thread) {
        if (thread != null)
            unsafe.unpark(thread);
    }
    public static void park() {
        unsafe.park(false, 0L);
    }
    public static void parkNanos(long nanos) {
        if (nanos > 0)
            unsafe.park(false, nanos);
    }
    public static void parkUntil(long deadline) {
        unsafe.park(true, deadline);
    }
}

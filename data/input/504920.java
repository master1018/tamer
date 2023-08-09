public class DdmVmInternal {
    private DdmVmInternal() {}
    native public static void threadNotify(boolean enable);
    native public static boolean heapInfoNotify(int when);
    native public static boolean heapSegmentNotify(int when, int what,
        boolean isNative);
    native public static byte[] getThreadStats();
    native public static StackTraceElement[] getStackTraceById(int threadId);
    native public static void enableRecentAllocations(boolean enable);
    native public static boolean getRecentAllocationStatus();
    native public static byte[] getRecentAllocations();
}

public final class VMDebug {
    static public final String DEFAULT_METHOD_TRACE_FILE_NAME = "/sdcard/dmtrace.trace";
    public static final int TRACE_COUNT_ALLOCS = 1;
    private static final int KIND_ALLOCATED_OBJECTS     = 1<<0;
    private static final int KIND_ALLOCATED_BYTES       = 1<<1;
    private static final int KIND_FREED_OBJECTS         = 1<<2;
    private static final int KIND_FREED_BYTES           = 1<<3;
    private static final int KIND_GC_INVOCATIONS        = 1<<4;
    private static final int KIND_CLASS_INIT_COUNT      = 1<<5;
    private static final int KIND_CLASS_INIT_TIME       = 1<<6;
    private static final int KIND_EXT_ALLOCATED_OBJECTS = 1<<12;
    private static final int KIND_EXT_ALLOCATED_BYTES   = 1<<13;
    private static final int KIND_EXT_FREED_OBJECTS     = 1<<14;
    private static final int KIND_EXT_FREED_BYTES       = 1<<15;
    public static final int KIND_GLOBAL_ALLOCATED_OBJECTS =
        KIND_ALLOCATED_OBJECTS;
    public static final int KIND_GLOBAL_ALLOCATED_BYTES =
        KIND_ALLOCATED_BYTES;
    public static final int KIND_GLOBAL_FREED_OBJECTS =
        KIND_FREED_OBJECTS;
    public static final int KIND_GLOBAL_FREED_BYTES =
        KIND_FREED_BYTES;
    public static final int KIND_GLOBAL_GC_INVOCATIONS =
        KIND_GC_INVOCATIONS;
    public static final int KIND_GLOBAL_CLASS_INIT_COUNT =
        KIND_CLASS_INIT_COUNT;
    public static final int KIND_GLOBAL_CLASS_INIT_TIME =
        KIND_CLASS_INIT_TIME;
    public static final int KIND_GLOBAL_EXT_ALLOCATED_OBJECTS =
        KIND_EXT_ALLOCATED_OBJECTS;
    public static final int KIND_GLOBAL_EXT_ALLOCATED_BYTES =
        KIND_EXT_ALLOCATED_BYTES;
    public static final int KIND_GLOBAL_EXT_FREED_OBJECTS =
        KIND_EXT_FREED_OBJECTS;
    public static final int KIND_GLOBAL_EXT_FREED_BYTES =
        KIND_EXT_FREED_BYTES;
    public static final int KIND_THREAD_ALLOCATED_OBJECTS =
        KIND_ALLOCATED_OBJECTS << 16;
    public static final int KIND_THREAD_ALLOCATED_BYTES =
        KIND_ALLOCATED_BYTES << 16;
    public static final int KIND_THREAD_FREED_OBJECTS =
        KIND_FREED_OBJECTS << 16;
    public static final int KIND_THREAD_FREED_BYTES =
        KIND_FREED_BYTES << 16;
    public static final int KIND_THREAD_GC_INVOCATIONS =
        KIND_GC_INVOCATIONS << 16;
    public static final int KIND_THREAD_CLASS_INIT_COUNT =
        KIND_CLASS_INIT_COUNT << 16;
    public static final int KIND_THREAD_CLASS_INIT_TIME =
        KIND_CLASS_INIT_TIME << 16;
    public static final int KIND_THREAD_EXT_ALLOCATED_OBJECTS =
        KIND_EXT_ALLOCATED_OBJECTS << 16;
    public static final int KIND_THREAD_EXT_ALLOCATED_BYTES =
        KIND_EXT_ALLOCATED_BYTES << 16;
    public static final int KIND_THREAD_EXT_FREED_OBJECTS =
        KIND_EXT_FREED_OBJECTS << 16;
    public static final int KIND_THREAD_EXT_FREED_BYTES =
        KIND_EXT_FREED_BYTES << 16;
    public static final int KIND_ALL_COUNTS = 0xffffffff;
    private VMDebug() {}
    public static native long lastDebuggerActivity();
    public static native boolean isDebuggingEnabled();
    public static native boolean isDebuggerConnected();
    public static native String[] getVmFeatureList();
    public static void startMethodTracing() {
        startMethodTracing(DEFAULT_METHOD_TRACE_FILE_NAME, 0, 0);
    }
    public static void startMethodTracing(String traceFileName,
        int bufferSize, int flags) {
        if (traceFileName == null) {
            throw new NullPointerException();
        }
        startMethodTracingNative(traceFileName, null, bufferSize, flags);
    }
    public static void startMethodTracing(String traceFileName,
        FileDescriptor fd, int bufferSize, int flags)
    {
        if (traceFileName == null || fd == null) {
            throw new NullPointerException();
        }
        startMethodTracingNative(traceFileName, fd, bufferSize, flags);
    }
    public static void startMethodTracingDdms(int bufferSize, int flags) {
        startMethodTracingNative(null, null, bufferSize, flags);
    }
    private static native void startMethodTracingNative(String traceFileName,
        FileDescriptor fd, int bufferSize, int flags);
    public static native boolean isMethodTracingActive();
    public static native void stopMethodTracing();
    public static native void startEmulatorTracing();
    public static native void stopEmulatorTracing();
    public static native long threadCpuTimeNanos();
    public static native void startAllocCounting();
    public static native void stopAllocCounting();
    public static native int getAllocCount(int kind);
    public static native void resetAllocCount(int kinds);
    public static native int setAllocationLimit(int limit);
    public static native int setGlobalAllocationLimit(int limit);
    public static native void startInstructionCounting();
    public static native void stopInstructionCounting();
    public static native void getInstructionCount(int[] counts);
    public static native void resetInstructionCount();
    public static native void printLoadedClasses(int flags);
    public static native int getLoadedClassCount();
    public static native void dumpHprofData(String fileName) throws IOException;
    public static native void dumpHprofDataDdms();
    public static native boolean cacheRegisterMap(String classAndMethodDesc);
    public static native void dumpReferenceTables();
    public static native void crash();
    public static native void infopoint(int id);
    private static void startGC() {}
    private static void startClassPrep() {}
}

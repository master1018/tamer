public final class Debug
{
    private static final String TAG = "Debug";
    public static final int TRACE_COUNT_ALLOCS  = VMDebug.TRACE_COUNT_ALLOCS;
    public static final int SHOW_FULL_DETAIL    = 1;
    public static final int SHOW_CLASSLOADER    = (1 << 1);
    public static final int SHOW_INITIALIZED    = (1 << 2);
    private static volatile boolean mWaiting = false;
    private Debug() {}
    private static final int MIN_DEBUGGER_IDLE = 1300;      
    private static final int SPIN_DELAY = 200;              
    private static final String DEFAULT_TRACE_PATH_PREFIX = "/sdcard/";
    private static final String DEFAULT_TRACE_BODY = "dmtrace";
    private static final String DEFAULT_TRACE_EXTENSION = ".trace";
    private static final String DEFAULT_TRACE_FILE_PATH =
        DEFAULT_TRACE_PATH_PREFIX + DEFAULT_TRACE_BODY
        + DEFAULT_TRACE_EXTENSION;
    public static class MemoryInfo implements Parcelable {
        public int dalvikPss;
        public int dalvikPrivateDirty;
        public int dalvikSharedDirty;
        public int nativePss;
        public int nativePrivateDirty;
        public int nativeSharedDirty;
        public int otherPss;
        public int otherPrivateDirty;
        public int otherSharedDirty;
        public MemoryInfo() {
        }
        public int getTotalPss() {
            return dalvikPss + nativePss + otherPss;
        }
        public int getTotalPrivateDirty() {
            return dalvikPrivateDirty + nativePrivateDirty + otherPrivateDirty;
        }
        public int getTotalSharedDirty() {
            return dalvikSharedDirty + nativeSharedDirty + otherSharedDirty;
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(dalvikPss);
            dest.writeInt(dalvikPrivateDirty);
            dest.writeInt(dalvikSharedDirty);
            dest.writeInt(nativePss);
            dest.writeInt(nativePrivateDirty);
            dest.writeInt(nativeSharedDirty);
            dest.writeInt(otherPss);
            dest.writeInt(otherPrivateDirty);
            dest.writeInt(otherSharedDirty);
        }
        public void readFromParcel(Parcel source) {
            dalvikPss = source.readInt();
            dalvikPrivateDirty = source.readInt();
            dalvikSharedDirty = source.readInt();
            nativePss = source.readInt();
            nativePrivateDirty = source.readInt();
            nativeSharedDirty = source.readInt();
            otherPss = source.readInt();
            otherPrivateDirty = source.readInt();
            otherSharedDirty = source.readInt();
        }
        public static final Creator<MemoryInfo> CREATOR = new Creator<MemoryInfo>() {
            public MemoryInfo createFromParcel(Parcel source) {
                return new MemoryInfo(source);
            }
            public MemoryInfo[] newArray(int size) {
                return new MemoryInfo[size];
            }
        };
        private MemoryInfo(Parcel source) {
            readFromParcel(source);
        }
    }
    public static void waitForDebugger() {
        if (!VMDebug.isDebuggingEnabled()) {
            return;
        }
        if (isDebuggerConnected())
            return;
        System.out.println("Sending WAIT chunk");
        byte[] data = new byte[] { 0 };     
        Chunk waitChunk = new Chunk(ChunkHandler.type("WAIT"), data, 0, 1);
        DdmServer.sendChunk(waitChunk);
        mWaiting = true;
        while (!isDebuggerConnected()) {
            try { Thread.sleep(SPIN_DELAY); }
            catch (InterruptedException ie) {}
        }
        mWaiting = false;
        System.out.println("Debugger has connected");
        while (true) {
            long delta = VMDebug.lastDebuggerActivity();
            if (delta < 0) {
                System.out.println("debugger detached?");
                break;
            }
            if (delta < MIN_DEBUGGER_IDLE) {
                System.out.println("waiting for debugger to settle...");
                try { Thread.sleep(SPIN_DELAY); }
                catch (InterruptedException ie) {}
            } else {
                System.out.println("debugger has settled (" + delta + ")");
                break;
            }
        }
    }
    public static boolean waitingForDebugger() {
        return mWaiting;
    }
    public static boolean isDebuggerConnected() {
        return VMDebug.isDebuggerConnected();
    }
    public static String[] getVmFeatureList() {
        return VMDebug.getVmFeatureList();
    }
    @Deprecated
    public static void changeDebugPort(int port) {}
    private static final String SYSFS_QEMU_TRACE_STATE = "/sys/qemu_trace/state";
    public static void startNativeTracing() {
        PrintWriter outStream = null;
        try {
            FileOutputStream fos = new FileOutputStream(SYSFS_QEMU_TRACE_STATE);
            outStream = new PrintWriter(new OutputStreamWriter(fos));
            outStream.println("1");
        } catch (Exception e) {
        } finally {
            if (outStream != null)
                outStream.close();
        }
        VMDebug.startEmulatorTracing();
    }
    public static void stopNativeTracing() {
        VMDebug.stopEmulatorTracing();
        PrintWriter outStream = null;
        try {
            FileOutputStream fos = new FileOutputStream(SYSFS_QEMU_TRACE_STATE);
            outStream = new PrintWriter(new OutputStreamWriter(fos));
            outStream.println("0");
        } catch (Exception e) {
        } finally {
            if (outStream != null)
                outStream.close();
        }
    }
    public static void enableEmulatorTraceOutput() {
        VMDebug.startEmulatorTracing();
    }
    public static void startMethodTracing() {
        VMDebug.startMethodTracing(DEFAULT_TRACE_FILE_PATH, 0, 0);
    }
    public static void startMethodTracing(String traceName) {
        startMethodTracing(traceName, 0, 0);
    }
    public static void startMethodTracing(String traceName, int bufferSize) {
        startMethodTracing(traceName, bufferSize, 0);
    }
    public static void startMethodTracing(String traceName, int bufferSize,
        int flags) {
        String pathName = traceName;
        if (pathName.charAt(0) != '/')
            pathName = DEFAULT_TRACE_PATH_PREFIX + pathName;
        if (!pathName.endsWith(DEFAULT_TRACE_EXTENSION))
            pathName = pathName + DEFAULT_TRACE_EXTENSION;
        VMDebug.startMethodTracing(pathName, bufferSize, flags);
    }
    public static void startMethodTracing(String traceName, FileDescriptor fd,
        int bufferSize, int flags) {
        VMDebug.startMethodTracing(traceName, fd, bufferSize, flags);
    }
    public static void startMethodTracingDdms(int bufferSize, int flags) {
        VMDebug.startMethodTracingDdms(bufferSize, flags);
    }
    public static boolean isMethodTracingActive() {
        return VMDebug.isMethodTracingActive();
    }
    public static void stopMethodTracing() {
        VMDebug.stopMethodTracing();
    }
    public static long threadCpuTimeNanos() {
        return VMDebug.threadCpuTimeNanos();
    }
    public static void startAllocCounting() {
        VMDebug.startAllocCounting();
    }
    public static void stopAllocCounting() {
        VMDebug.stopAllocCounting();
    }
    public static int getGlobalAllocCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_ALLOCATED_OBJECTS);
    }
    public static int getGlobalAllocSize() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_ALLOCATED_BYTES);
    }
    public static int getGlobalFreedCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_FREED_OBJECTS);
    }
    public static int getGlobalFreedSize() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_FREED_BYTES);
    }
    public static int getGlobalClassInitCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_CLASS_INIT_COUNT);
    }
    public static int getGlobalClassInitTime() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_CLASS_INIT_TIME);
    }
    public static int getGlobalExternalAllocCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS);
    }
    public static int getGlobalExternalAllocSize() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_EXT_ALLOCATED_BYTES);
    }
    public static int getGlobalExternalFreedCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_EXT_FREED_OBJECTS);
    }
    public static int getGlobalExternalFreedSize() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_EXT_FREED_BYTES);
    }
    public static int getGlobalGcInvocationCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_GLOBAL_GC_INVOCATIONS);
    }
    public static int getThreadAllocCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_THREAD_ALLOCATED_OBJECTS);
    }
    public static int getThreadAllocSize() {
        return VMDebug.getAllocCount(VMDebug.KIND_THREAD_ALLOCATED_BYTES);
    }
    public static int getThreadExternalAllocCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_THREAD_EXT_ALLOCATED_OBJECTS);
    }
    public static int getThreadExternalAllocSize() {
        return VMDebug.getAllocCount(VMDebug.KIND_THREAD_EXT_ALLOCATED_BYTES);
    }
    public static int getThreadGcInvocationCount() {
        return VMDebug.getAllocCount(VMDebug.KIND_THREAD_GC_INVOCATIONS);
    }
    public static void resetGlobalAllocCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_ALLOCATED_OBJECTS);
    }
    public static void resetGlobalAllocSize() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_ALLOCATED_BYTES);
    }
    public static void resetGlobalFreedCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_FREED_OBJECTS);
    }
    public static void resetGlobalFreedSize() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_FREED_BYTES);
    }
    public static void resetGlobalClassInitCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_CLASS_INIT_COUNT);
    }
    public static void resetGlobalClassInitTime() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_CLASS_INIT_TIME);
    }
    public static void resetGlobalExternalAllocCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS);
    }
    public static void resetGlobalExternalAllocSize() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_EXT_ALLOCATED_BYTES);
    }
    public static void resetGlobalExternalFreedCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_EXT_FREED_OBJECTS);
    }
    public static void resetGlobalExternalFreedSize() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_EXT_FREED_BYTES);
    }
    public static void resetGlobalGcInvocationCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_GLOBAL_GC_INVOCATIONS);
    }
    public static void resetThreadAllocCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_THREAD_ALLOCATED_OBJECTS);
    }
    public static void resetThreadAllocSize() {
        VMDebug.resetAllocCount(VMDebug.KIND_THREAD_ALLOCATED_BYTES);
    }
    public static void resetThreadExternalAllocCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_THREAD_EXT_ALLOCATED_OBJECTS);
    }
    public static void resetThreadExternalAllocSize() {
        VMDebug.resetAllocCount(VMDebug.KIND_THREAD_EXT_ALLOCATED_BYTES);
    }
    public static void resetThreadGcInvocationCount() {
        VMDebug.resetAllocCount(VMDebug.KIND_THREAD_GC_INVOCATIONS);
    }
    public static void resetAllCounts() {
        VMDebug.resetAllocCount(VMDebug.KIND_ALL_COUNTS);
    }
    public static native long getNativeHeapSize();
    public static native long getNativeHeapAllocatedSize();
    public static native long getNativeHeapFreeSize();
    public static native void getMemoryInfo(MemoryInfo memoryInfo);
    public static native void getMemoryInfo(int pid, MemoryInfo memoryInfo);
    public static int setAllocationLimit(int limit) {
        return VMDebug.setAllocationLimit(limit);
    }
    public static int setGlobalAllocationLimit(int limit) {
        if (limit != 0 && limit != -1)
            throw new IllegalArgumentException("limit must be 0 or -1");
        return VMDebug.setGlobalAllocationLimit(limit);
    }
    public static void printLoadedClasses(int flags) {
        VMDebug.printLoadedClasses(flags);
    }
    public static int getLoadedClassCount() {
        return VMDebug.getLoadedClassCount();
    }
    public static void dumpHprofData(String fileName) throws IOException {
        VMDebug.dumpHprofData(fileName);
    }
    public static void dumpHprofDataDdms() {
        VMDebug.dumpHprofDataDdms();
    }
    public static native int getBinderSentTransactions();
    public static native int getBinderReceivedTransactions();
    public static final native int getBinderLocalObjectCount();
    public static final native int getBinderProxyObjectCount();
    public static final native int getBinderDeathObjectCount();
    public static final boolean cacheRegisterMap(String classAndMethodDesc) {
        return VMDebug.cacheRegisterMap(classAndMethodDesc);
    }
    public static final void dumpReferenceTables() {
        VMDebug.dumpReferenceTables();
    }
    public static class InstructionCount {
        private static final int NUM_INSTR = 256;
        private int[] mCounts;
        public InstructionCount() {
            mCounts = new int[NUM_INSTR];
        }
        public boolean resetAndStart() {
            try {
                VMDebug.startInstructionCounting();
                VMDebug.resetInstructionCount();
            } catch (UnsupportedOperationException uoe) {
                return false;
            }
            return true;
        }
        public boolean collect() {
            try {
                VMDebug.stopInstructionCounting();
                VMDebug.getInstructionCount(mCounts);
            } catch (UnsupportedOperationException uoe) {
                return false;
            }
            return true;
        }
        public int globalTotal() {
            int count = 0;
            for (int i = 0; i < NUM_INSTR; i++)
                count += mCounts[i];
            return count;
        }
        public int globalMethodInvocations() {
            int count = 0;
            count += mCounts[Opcodes.OP_INVOKE_VIRTUAL];
            count += mCounts[Opcodes.OP_INVOKE_SUPER];
            count += mCounts[Opcodes.OP_INVOKE_DIRECT];
            count += mCounts[Opcodes.OP_INVOKE_STATIC];
            count += mCounts[Opcodes.OP_INVOKE_INTERFACE];
            count += mCounts[Opcodes.OP_INVOKE_VIRTUAL_RANGE];
            count += mCounts[Opcodes.OP_INVOKE_SUPER_RANGE];
            count += mCounts[Opcodes.OP_INVOKE_DIRECT_RANGE];
            count += mCounts[Opcodes.OP_INVOKE_STATIC_RANGE];
            count += mCounts[Opcodes.OP_INVOKE_INTERFACE_RANGE];
            count += mCounts[Opcodes.OP_INVOKE_VIRTUAL_QUICK];
            count += mCounts[Opcodes.OP_INVOKE_VIRTUAL_QUICK_RANGE];
            count += mCounts[Opcodes.OP_INVOKE_SUPER_QUICK];
            count += mCounts[Opcodes.OP_INVOKE_SUPER_QUICK_RANGE];
            return count;
        }
    }
    private static final TypedProperties debugProperties;
    static {
        if (Config.DEBUG) {
            final String TAG = "DebugProperties";
            final String[] files = { "/system/debug.prop", "/debug.prop", "/data/debug.prop" };
            final TypedProperties tp = new TypedProperties();
            for (String file : files) {
                Reader r;
                try {
                    r = new FileReader(file);
                } catch (FileNotFoundException ex) {
                    continue;
                }
                try {
                    tp.load(r);
                } catch (Exception ex) {
                    throw new RuntimeException("Problem loading " + file, ex);
                } finally {
                    try {
                        r.close();
                    } catch (IOException ex) {
                    }
                }
            }
            debugProperties = tp.isEmpty() ? null : tp;
        } else {
            debugProperties = null;
        }
    }
    private static boolean fieldTypeMatches(Field field, Class<?> cl) {
        Class<?> fieldClass = field.getType();
        if (fieldClass == cl) {
            return true;
        }
        Field primitiveTypeField;
        try {
            primitiveTypeField = cl.getField("TYPE");
        } catch (NoSuchFieldException ex) {
            return false;
        }
        try {
            return fieldClass == (Class<?>) primitiveTypeField.get(null);
        } catch (IllegalAccessException ex) {
            return false;
        }
    }
    private static void modifyFieldIfSet(final Field field, final TypedProperties properties,
                                         final String propertyName) {
        if (field.getType() == java.lang.String.class) {
            int stringInfo = properties.getStringInfo(propertyName);
            switch (stringInfo) {
                case TypedProperties.STRING_SET:
                    break;
                case TypedProperties.STRING_NULL:
                    try {
                        field.set(null, null);  
                    } catch (IllegalAccessException ex) {
                        throw new IllegalArgumentException(
                            "Cannot set field for " + propertyName, ex);
                    }
                    return;
                case TypedProperties.STRING_NOT_SET:
                    return;
                case TypedProperties.STRING_TYPE_MISMATCH:
                    throw new IllegalArgumentException(
                        "Type of " + propertyName + " " +
                        " does not match field type (" + field.getType() + ")");
                default:
                    throw new IllegalStateException(
                        "Unexpected getStringInfo(" + propertyName + ") return value " +
                        stringInfo);
            }
        }
        Object value = properties.get(propertyName);
        if (value != null) {
            if (!fieldTypeMatches(field, value.getClass())) {
                throw new IllegalArgumentException(
                    "Type of " + propertyName + " (" + value.getClass() + ") " +
                    " does not match field type (" + field.getType() + ")");
            }
            try {
                field.set(null, value);  
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(
                    "Cannot set field for " + propertyName, ex);
            }
        }
    }
    public static void setFieldsOn(Class<?> cl) {
        setFieldsOn(cl, false);
    }
    public static void setFieldsOn(Class<?> cl, boolean partial) {
        if (Config.DEBUG) {
            if (debugProperties != null) {
                for (Field field : cl.getDeclaredFields()) {
                    if (!partial || field.getAnnotation(DebugProperty.class) != null) {
                        final String propertyName = cl.getName() + "." + field.getName();
                        boolean isStatic = Modifier.isStatic(field.getModifiers());
                        boolean isFinal = Modifier.isFinal(field.getModifiers());
                        if (!isStatic || isFinal) {
                            throw new IllegalArgumentException(propertyName +
                                " must be static and non-final");
                        }
                        modifyFieldIfSet(field, debugProperties, propertyName);
                    }
                }
            }
        } else {
            Log.wtf(TAG,
                  "setFieldsOn(" + (cl == null ? "null" : cl.getName()) +
                  ") called in non-DEBUG build");
        }
    }
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DebugProperty {
    }
    public static boolean dumpService(String name, FileDescriptor fd, String[] args) {
        IBinder service = ServiceManager.getService(name);
        if (service == null) {
            Log.e(TAG, "Can't find service to dump: " + name);
            return false;
        }
        try {
            service.dump(fd, args);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Can't dump service: " + name, e);
            return false;
        }
    }
}

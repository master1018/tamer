public class ClientData {
    private final static String PRE_INITIALIZED = "<pre-initialized>"; 
    public static enum DebuggerStatus {
        DEFAULT,
        WAITING,
        ATTACHED,
        ERROR;
    }
    public static enum AllocationTrackingStatus {
        UNKNOWN,
        OFF,
        ON;
    }
    public static enum MethodProfilingStatus {
        UNKNOWN,
        OFF,
        ON;
    }
    public final static String HEAP_MAX_SIZE_BYTES = "maxSizeInBytes"; 
    public final static String HEAP_SIZE_BYTES = "sizeInBytes"; 
    public final static String HEAP_BYTES_ALLOCATED = "bytesAllocated"; 
    public final static String HEAP_OBJECTS_ALLOCATED = "objectsAllocated"; 
    public final static String FEATURE_PROFILING = "method-trace-profiling"; 
    public final static String FEATURE_PROFILING_STREAMING = "method-trace-profiling-streaming"; 
    public final static String FEATURE_HPROF = "hprof-heap-dump"; 
    public final static String FEATURE_HPROF_STREAMING = "hprof-heap-dump-streaming"; 
    private static IHprofDumpHandler sHprofDumpHandler;
    private static IMethodProfilingHandler sMethodProfilingHandler;
    private boolean mIsDdmAware;
    private final int mPid;
    private String mVmIdentifier;
    private String mClientDescription;
    private DebuggerStatus mDebuggerInterest;
    private final HashSet<String> mFeatures = new HashSet<String>();
    private TreeMap<Integer,ThreadInfo> mThreadMap;
    private final HeapData mHeapData = new HeapData();
    private final HeapData mNativeHeapData = new HeapData();
    private HashMap<Integer, HashMap<String, Long>> mHeapInfoMap =
            new HashMap<Integer, HashMap<String, Long>>();
    private ArrayList<NativeLibraryMapInfo> mNativeLibMapInfo =
        new ArrayList<NativeLibraryMapInfo>();
    private ArrayList<NativeAllocationInfo> mNativeAllocationList =
        new ArrayList<NativeAllocationInfo>();
    private int mNativeTotalMemory;
    private AllocationInfo[] mAllocations;
    private AllocationTrackingStatus mAllocationStatus = AllocationTrackingStatus.UNKNOWN;
    private String mPendingHprofDump;
    private MethodProfilingStatus mProfilingStatus = MethodProfilingStatus.UNKNOWN;
    private String mPendingMethodProfiling;
    public static class HeapData {
        private TreeSet<HeapSegment> mHeapSegments = new TreeSet<HeapSegment>();
        private boolean mHeapDataComplete = false;
        private byte[] mProcessedHeapData;
        private Map<Integer, ArrayList<HeapSegmentElement>> mProcessedHeapMap;
        public synchronized void clearHeapData() {
            mHeapSegments = new TreeSet<HeapSegment>();
            mHeapDataComplete = false;
        }
        synchronized void addHeapData(ByteBuffer data) {
            HeapSegment hs;
            if (mHeapDataComplete) {
                clearHeapData();
            }
            try {
                hs = new HeapSegment(data);
            } catch (BufferUnderflowException e) {
                System.err.println("Discarding short HPSG data (length " + data.limit() + ")");
                return;
            }
            mHeapSegments.add(hs);
        }
        synchronized void sealHeapData() {
            mHeapDataComplete = true;
        }
        public boolean isHeapDataComplete() {
            return mHeapDataComplete;
        }
        public Collection<HeapSegment> getHeapSegments() {
            if (isHeapDataComplete()) {
                return mHeapSegments;
            }
            return null;
        }
        public void setProcessedHeapData(byte[] heapData) {
            mProcessedHeapData = heapData;
        }
        public byte[] getProcessedHeapData() {
            return mProcessedHeapData;
        }
        public void setProcessedHeapMap(Map<Integer, ArrayList<HeapSegmentElement>> heapMap) {
            mProcessedHeapMap = heapMap;
        }
        public Map<Integer, ArrayList<HeapSegmentElement>> getProcessedHeapMap() {
            return mProcessedHeapMap;
        }
    }
    public interface IHprofDumpHandler {
        void onSuccess(String remoteFilePath, Client client);
        void onSuccess(byte[] data, Client client);
        void onEndFailure(Client client, String message);
    }
    public interface IMethodProfilingHandler {
        void onSuccess(String remoteFilePath, Client client);
        void onSuccess(byte[] data, Client client);
        void onStartFailure(Client client, String message);
        void onEndFailure(Client client, String message);
    }
    public static void setHprofDumpHandler(IHprofDumpHandler handler) {
        sHprofDumpHandler = handler;
    }
    static IHprofDumpHandler getHprofDumpHandler() {
        return sHprofDumpHandler;
    }
    public static void setMethodProfilingHandler(IMethodProfilingHandler handler) {
        sMethodProfilingHandler = handler;
    }
    static IMethodProfilingHandler getMethodProfilingHandler() {
        return sMethodProfilingHandler;
    }
    ClientData(int pid) {
        mPid = pid;
        mDebuggerInterest = DebuggerStatus.DEFAULT;
        mThreadMap = new TreeMap<Integer,ThreadInfo>();
    }
    public boolean isDdmAware() {
        return mIsDdmAware;
    }
    void isDdmAware(boolean aware) {
        mIsDdmAware = aware;
    }
    public int getPid() {
        return mPid;
    }
    public String getVmIdentifier() {
        return mVmIdentifier;
    }
    void setVmIdentifier(String ident) {
        mVmIdentifier = ident;
    }
    public String getClientDescription() {
        return mClientDescription;
    }
    void setClientDescription(String description) {
        if (mClientDescription == null && description.length() > 0) {
            if (PRE_INITIALIZED.equals(description) == false) {
                mClientDescription = description;
            }
        }
    }
    public DebuggerStatus getDebuggerConnectionStatus() {
        return mDebuggerInterest;
    }
    void setDebuggerConnectionStatus(DebuggerStatus status) {
        mDebuggerInterest = status;
    }
    synchronized void setHeapInfo(int heapId, long maxSizeInBytes,
            long sizeInBytes, long bytesAllocated, long objectsAllocated) {
        HashMap<String, Long> heapInfo = new HashMap<String, Long>();
        heapInfo.put(HEAP_MAX_SIZE_BYTES, maxSizeInBytes);
        heapInfo.put(HEAP_SIZE_BYTES, sizeInBytes);
        heapInfo.put(HEAP_BYTES_ALLOCATED, bytesAllocated);
        heapInfo.put(HEAP_OBJECTS_ALLOCATED, objectsAllocated);
        mHeapInfoMap.put(heapId, heapInfo);
    }
    public HeapData getVmHeapData() {
        return mHeapData;
    }
    HeapData getNativeHeapData() {
        return mNativeHeapData;
    }
    public synchronized Iterator<Integer> getVmHeapIds() {
        return mHeapInfoMap.keySet().iterator();
    }
    public synchronized Map<String, Long> getVmHeapInfo(int heapId) {
        return mHeapInfoMap.get(heapId);
    }
    synchronized void addThread(int threadId, String threadName) {
        ThreadInfo attr = new ThreadInfo(threadId, threadName);
        mThreadMap.put(threadId, attr);
    }
    synchronized void removeThread(int threadId) {
        mThreadMap.remove(threadId);
    }
    public synchronized ThreadInfo[] getThreads() {
        Collection<ThreadInfo> threads = mThreadMap.values();
        return threads.toArray(new ThreadInfo[threads.size()]);
    }
    synchronized ThreadInfo getThread(int threadId) {
        return mThreadMap.get(threadId);
    }
    synchronized void clearThreads() {
        mThreadMap.clear();
    }
    public synchronized List<NativeAllocationInfo> getNativeAllocationList() {
        return Collections.unmodifiableList(mNativeAllocationList);
    }
    synchronized void addNativeAllocation(NativeAllocationInfo allocInfo) {
        mNativeAllocationList.add(allocInfo);
    }
    synchronized void clearNativeAllocationInfo() {
        mNativeAllocationList.clear();
    }
    public synchronized int getTotalNativeMemory() {
        return mNativeTotalMemory;
    }
    synchronized void setTotalNativeMemory(int totalMemory) {
        mNativeTotalMemory = totalMemory;
    }
    synchronized void addNativeLibraryMapInfo(long startAddr, long endAddr, String library) {
        mNativeLibMapInfo.add(new NativeLibraryMapInfo(startAddr, endAddr, library));
    }
    public synchronized Iterator<NativeLibraryMapInfo> getNativeLibraryMapInfo() {
        return mNativeLibMapInfo.iterator();
    }
    synchronized void setAllocationStatus(AllocationTrackingStatus status) {
        mAllocationStatus = status;
    }
    public synchronized AllocationTrackingStatus getAllocationStatus() {
        return mAllocationStatus;
    }
    synchronized void setAllocations(AllocationInfo[] allocs) {
        mAllocations = allocs;
    }
    public synchronized AllocationInfo[] getAllocations() {
        return mAllocations;
    }
    void addFeature(String feature) {
        mFeatures.add(feature);
    }
    public boolean hasFeature(String feature) {
        return mFeatures.contains(feature);
    }
    void setPendingHprofDump(String pendingHprofDump) {
        mPendingHprofDump = pendingHprofDump;
    }
    String getPendingHprofDump() {
        return mPendingHprofDump;
    }
    public boolean hasPendingHprofDump() {
        return mPendingHprofDump != null;
    }
    synchronized void setMethodProfilingStatus(MethodProfilingStatus status) {
        mProfilingStatus = status;
    }
    public synchronized MethodProfilingStatus getMethodProfilingStatus() {
        return mProfilingStatus;
    }
    void setPendingMethodProfiling(String pendingMethodProfiling) {
        mPendingMethodProfiling = pendingMethodProfiling;
    }
    String getPendingMethodProfiling() {
        return mPendingMethodProfiling;
    }
}

public class AllocationInfo implements Comparable<AllocationInfo>, IStackTraceInfo {
    private String mAllocatedClass;
    private int mAllocationSize;
    private short mThreadId;
    private StackTraceElement[] mStackTrace;
    AllocationInfo(String allocatedClass, int allocationSize,
        short threadId, StackTraceElement[] stackTrace) {
        mAllocatedClass = allocatedClass;
        mAllocationSize = allocationSize;
        mThreadId = threadId;
        mStackTrace = stackTrace;
    }
    public String getAllocatedClass() {
        return mAllocatedClass;
    }
    public int getSize() {
        return mAllocationSize;
    }
    public short getThreadId() {
        return mThreadId;
    }
    public StackTraceElement[] getStackTrace() {
        return mStackTrace;
    }
    public int compareTo(AllocationInfo otherAlloc) {
        return otherAlloc.mAllocationSize - mAllocationSize;
    }
}

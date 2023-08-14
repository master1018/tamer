public final class NativeAllocationInfo {
    private static final int FLAG_ZYGOTE_CHILD  = (1<<31);
    private static final int FLAG_MASK          = (FLAG_ZYGOTE_CHILD);
    private static ArrayList<String> sAllocFunctionFilter;
    static {
        sAllocFunctionFilter = new ArrayList<String>();
        sAllocFunctionFilter.add("malloc"); 
        sAllocFunctionFilter.add("calloc"); 
        sAllocFunctionFilter.add("realloc"); 
        sAllocFunctionFilter.add("get_backtrace"); 
        sAllocFunctionFilter.add("get_hash"); 
        sAllocFunctionFilter.add("??"); 
        sAllocFunctionFilter.add("internal_free"); 
        sAllocFunctionFilter.add("operator new"); 
        sAllocFunctionFilter.add("leak_free"); 
        sAllocFunctionFilter.add("chk_free"); 
        sAllocFunctionFilter.add("chk_memalign"); 
        sAllocFunctionFilter.add("Malloc"); 
    }
    private final int mSize;
    private final boolean mIsZygoteChild;
    private final int mAllocations;
    private final ArrayList<Long> mStackCallAddresses = new ArrayList<Long>();
    private ArrayList<NativeStackCallInfo> mResolvedStackCall = null;
    private boolean mIsStackCallResolved = false;
    NativeAllocationInfo(int size, int allocations) {
        this.mSize = size & ~FLAG_MASK;
        this.mIsZygoteChild = ((size & FLAG_ZYGOTE_CHILD) != 0);
        this.mAllocations = allocations;
    }
    void addStackCallAddress(long address) {
        mStackCallAddresses.add(address);
    }
    public int getSize() {
        return mSize;
    }
    public boolean isZygoteChild() {
        return mIsZygoteChild;
    }
    public int getAllocationCount() {
        return mAllocations;
    }
    public boolean isStackCallResolved() {
        return mIsStackCallResolved;
    }
    public Long[] getStackCallAddresses() {
        return mStackCallAddresses.toArray(new Long[mStackCallAddresses.size()]);
    }
    public synchronized void setResolvedStackCall(List<NativeStackCallInfo> resolvedStackCall) {
        if (mResolvedStackCall == null) {
            mResolvedStackCall = new ArrayList<NativeStackCallInfo>();
        } else {
            mResolvedStackCall.clear();
        }
        mResolvedStackCall.addAll(resolvedStackCall);
        mIsStackCallResolved = mResolvedStackCall.size() != 0;
    }
    public synchronized NativeStackCallInfo[] getResolvedStackCall() {
        if (mIsStackCallResolved) {
            return mResolvedStackCall.toArray(new NativeStackCallInfo[mResolvedStackCall.size()]);
        }
        return null;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof NativeAllocationInfo) {
            NativeAllocationInfo mi = (NativeAllocationInfo)obj;
            if (mSize != mi.mSize || mAllocations != mi.mAllocations ||
                    mStackCallAddresses.size() != mi.mStackCallAddresses.size()) {
                return false;
            }
            int count = mStackCallAddresses.size();
            for (int i = 0 ; i < count ; i++) {
                long a = mStackCallAddresses.get(i);
                long b = mi.mStackCallAddresses.get(i);
                if (a != b) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Allocations: ");
        buffer.append(mAllocations);
        buffer.append("\n"); 
        buffer.append("Size: ");
        buffer.append(mSize);
        buffer.append("\n"); 
        buffer.append("Total Size: ");
        buffer.append(mSize * mAllocations);
        buffer.append("\n"); 
        Iterator<Long> addrIterator = mStackCallAddresses.iterator();
        Iterator<NativeStackCallInfo> sourceIterator = mResolvedStackCall.iterator();
        while (sourceIterator.hasNext()) {
            long addr = addrIterator.next();
            NativeStackCallInfo source = sourceIterator.next();
            if (addr == 0)
                continue;
            if (source.getLineNumber() != -1) {
                buffer.append(String.format("\t%1$08x\t%2$s --- %3$s --- %4$s:%5$d\n", addr,
                        source.getLibraryName(), source.getMethodName(),
                        source.getSourceFile(), source.getLineNumber()));
            } else {
                buffer.append(String.format("\t%1$08x\t%2$s --- %3$s --- %4$s\n", addr,
                        source.getLibraryName(), source.getMethodName(), source.getSourceFile()));
            }
        }
        return buffer.toString();
    }
    public synchronized NativeStackCallInfo getRelevantStackCallInfo() {
        if (mIsStackCallResolved && mResolvedStackCall != null) {
            Iterator<NativeStackCallInfo> sourceIterator = mResolvedStackCall.iterator();
            Iterator<Long> addrIterator = mStackCallAddresses.iterator();
            while (sourceIterator.hasNext() && addrIterator.hasNext()) {
                long addr = addrIterator.next();
                NativeStackCallInfo info = sourceIterator.next();
                if (addr != 0 && info != null) {
                    if (isRelevant(info.getMethodName())) {
                        return info;
                    }
                }
            }
            if (mResolvedStackCall.size() > 0)
                return mResolvedStackCall.get(0);
        }
        return null;
    }
    private boolean isRelevant(String methodName) {
        for (String filter : sAllocFunctionFilter) {
            if (methodName.contains(filter)) {
                return false;
            }
        }
        return true;
    }
}

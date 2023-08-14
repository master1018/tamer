public final class NativeLibraryMapInfo {
    private long mStartAddr;
    private long mEndAddr;
    private String mLibrary;
    NativeLibraryMapInfo(long startAddr, long endAddr, String library) {
        this.mStartAddr = startAddr;
        this.mEndAddr = endAddr;
        this.mLibrary = library;
    }
    public String getLibraryName() {
        return mLibrary;
    }
    public long getStartAddress() {
        return mStartAddr;
    }
    public long getEndAddress() {
        return mEndAddr;
    }
    public boolean isWithinLibrary(long address) {
        return address >= mStartAddr && address <= mEndAddr;
    }
}

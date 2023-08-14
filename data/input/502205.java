public class StatFs {
    public StatFs(String path) { native_setup(path); }
    public void restat(String path) { native_restat(path); }
    @Override
    protected void finalize() { native_finalize(); }
    public native int getBlockSize();
    public native int getBlockCount();
    public native int getFreeBlocks();
    public native int getAvailableBlocks();    
    private int mNativeContext;
    private native void native_restat(String path);
    private native void native_setup(String path);
    private native void native_finalize();
}

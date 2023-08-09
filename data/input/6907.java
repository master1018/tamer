public class DefaultDisposerRecord implements DisposerRecord {
    private long dataPointer;
    private long disposerMethodPointer;
    public DefaultDisposerRecord(long  disposerMethodPointer, long dataPointer) {
        this.disposerMethodPointer = disposerMethodPointer;
        this.dataPointer = dataPointer;
    }
    public void dispose() {
        invokeNativeDispose(disposerMethodPointer,
                            dataPointer);
    }
    public long getDataPointer() {
        return dataPointer;
    }
    public long getDisposerMethodPointer() {
        return disposerMethodPointer;
    }
    public static native void invokeNativeDispose(long disposerMethodPointer,
                                                  long dataPointer);
}

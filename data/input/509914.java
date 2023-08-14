public final class VMRuntime {
    private static final VMRuntime THE_ONE = new VMRuntime();
    private VMRuntime() {
    }
    public static VMRuntime getRuntime() {
        return THE_ONE;
    }
    public native float getTargetHeapUtilization();
    public float setTargetHeapUtilization(float newTarget) {
        if (newTarget <= 0.0 || newTarget >= 1.0) {
            throw new IllegalArgumentException(newTarget +
                    " out of range (0,1)");
        }
        synchronized (this) {
            float oldTarget = getTargetHeapUtilization();
            nativeSetTargetHeapUtilization(newTarget);
            return oldTarget;
        }
    }
    public long getMinimumHeapSize() {
        return nativeMinimumHeapSize(0, false);
    }
    public synchronized long setMinimumHeapSize(long size) {
        return nativeMinimumHeapSize(size, true);
    }
    private native long nativeMinimumHeapSize(long size, boolean set);
    public native void gcSoftReferences();
    public native void runFinalizationSync();
    private native void nativeSetTargetHeapUtilization(float newTarget);
    public native boolean trackExternalAllocation(long size);
    public native void trackExternalFree(long size);
    public native long getExternalBytesAllocated();
    public native void startJitCompilation();
    public native void disableJitCompilation();
}

class AllocatedNativeObject                             
    extends NativeObject
{
    AllocatedNativeObject(int size, boolean pageAligned) {
        super(size, pageAligned);
    }
    synchronized void free() {
        if (allocationAddress != 0) {
            unsafe.freeMemory(allocationAddress);
            allocationAddress = 0;
        }
    }
}

class NativeBuffer {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long address;
    private final int size;
    private final Cleaner cleaner;
    private Object owner;
    private static class Deallocator implements Runnable {
        private final long address;
        Deallocator(long address) {
            this.address = address;
        }
        public void run() {
            unsafe.freeMemory(address);
        }
    }
    NativeBuffer(int size) {
        this.address = unsafe.allocateMemory(size);
        this.size = size;
        this.cleaner = Cleaner.create(this, new Deallocator(address));
    }
    void release() {
        NativeBuffers.releaseNativeBuffer(this);
    }
    long address() {
        return address;
    }
    int size() {
        return size;
    }
    Cleaner cleaner() {
        return cleaner;
    }
    void setOwner(Object owner) {
        this.owner = owner;
    }
    Object owner() {
        return owner;
    }
}

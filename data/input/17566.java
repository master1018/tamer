class NativeBuffers {
    private NativeBuffers() { }
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int TEMP_BUF_POOL_SIZE = 3;
    private static ThreadLocal<NativeBuffer[]> threadLocal =
        new ThreadLocal<NativeBuffer[]>();
    static NativeBuffer allocNativeBuffer(int size) {
        if (size < 2048) size = 2048;
        return new NativeBuffer(size);
    }
    static NativeBuffer getNativeBufferFromCache(int size) {
        NativeBuffer[] buffers = threadLocal.get();
        if (buffers != null) {
            for (int i=0; i<TEMP_BUF_POOL_SIZE; i++) {
                NativeBuffer buffer = buffers[i];
                if (buffer != null && buffer.size() >= size) {
                    buffers[i] = null;
                    return buffer;
                }
            }
        }
        return null;
    }
    static NativeBuffer getNativeBuffer(int size) {
        NativeBuffer buffer = getNativeBufferFromCache(size);
        if (buffer != null) {
            buffer.setOwner(null);
            return buffer;
        } else {
            return allocNativeBuffer(size);
        }
    }
    static void releaseNativeBuffer(NativeBuffer buffer) {
        NativeBuffer[] buffers = threadLocal.get();
        if (buffers == null) {
            buffers = new NativeBuffer[TEMP_BUF_POOL_SIZE];
            buffers[0] = buffer;
            threadLocal.set(buffers);
            return;
        }
        for (int i=0; i<TEMP_BUF_POOL_SIZE; i++) {
            if (buffers[i] == null) {
                buffers[i] = buffer;
                return;
            }
        }
        for (int i=0; i<TEMP_BUF_POOL_SIZE; i++) {
            NativeBuffer existing = buffers[i];
            if (existing.size() < buffer.size()) {
                existing.cleaner().clean();
                buffers[i] = buffer;
                return;
            }
        }
        buffer.cleaner().clean();
    }
    static void copyCStringToNativeBuffer(byte[] cstr, NativeBuffer buffer) {
        long offset = Unsafe.ARRAY_BYTE_BASE_OFFSET;
        long len = cstr.length;
        assert buffer.size() >= (len + 1);
        unsafe.copyMemory(cstr, offset, null, buffer.address(), len);
        unsafe.putByte(buffer.address() + len, (byte)0);
    }
    static NativeBuffer asNativeBuffer(byte[] cstr) {
        NativeBuffer buffer = getNativeBuffer(cstr.length+1);
        copyCStringToNativeBuffer(cstr, buffer);
        return buffer;
    }
}

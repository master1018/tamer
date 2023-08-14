public class RenderBuffer {
    protected static final long SIZEOF_BYTE   = 1L;
    protected static final long SIZEOF_SHORT  = 2L;
    protected static final long SIZEOF_INT    = 4L;
    protected static final long SIZEOF_FLOAT  = 4L;
    protected static final long SIZEOF_LONG   = 8L;
    protected static final long SIZEOF_DOUBLE = 8L;
    private static final int COPY_FROM_ARRAY_THRESHOLD = 6;
    protected final Unsafe unsafe;
    protected final long baseAddress;
    protected final long endAddress;
    protected long curAddress;
    protected final int capacity;
    protected RenderBuffer(int numBytes) {
        unsafe = Unsafe.getUnsafe();
        curAddress = baseAddress = unsafe.allocateMemory(numBytes);
        endAddress = baseAddress + numBytes;
        capacity = numBytes;
    }
    public static RenderBuffer allocate(int numBytes) {
        return new RenderBuffer(numBytes);
    }
    public final long getAddress() {
        return baseAddress;
    }
    public final int capacity() {
        return capacity;
    }
    public final int remaining() {
        return (int)(endAddress - curAddress);
    }
    public final int position() {
        return (int)(curAddress - baseAddress);
    }
    public final void position(long numBytes) {
        curAddress = baseAddress + numBytes;
    }
    public final void clear() {
        curAddress = baseAddress;
    }
    public final RenderBuffer skip(long numBytes) {
        curAddress += numBytes;
        return this;
    }
    public final RenderBuffer putByte(byte x) {
        unsafe.putByte(curAddress, x);
        curAddress += SIZEOF_BYTE;
        return this;
    }
    public RenderBuffer put(byte[] x) {
        return put(x, 0, x.length);
    }
    public RenderBuffer put(byte[] x, int offset, int length) {
        if (length > COPY_FROM_ARRAY_THRESHOLD) {
            long offsetInBytes = offset * SIZEOF_BYTE + Unsafe.ARRAY_BYTE_BASE_OFFSET;
            long lengthInBytes = length * SIZEOF_BYTE;
            unsafe.copyMemory(x, offsetInBytes, null, curAddress, lengthInBytes);
            position(position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                putByte(x[i]);
            }
        }
        return this;
    }
    public final RenderBuffer putShort(short x) {
        unsafe.putShort(curAddress, x);
        curAddress += SIZEOF_SHORT;
        return this;
    }
    public RenderBuffer put(short[] x) {
        return put(x, 0, x.length);
    }
    public RenderBuffer put(short[] x, int offset, int length) {
        if (length > COPY_FROM_ARRAY_THRESHOLD) {
            long offsetInBytes = offset * SIZEOF_SHORT + Unsafe.ARRAY_SHORT_BASE_OFFSET;
            long lengthInBytes = length * SIZEOF_SHORT;
            unsafe.copyMemory(x, offsetInBytes, null, curAddress, lengthInBytes);
            position(position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                putShort(x[i]);
            }
        }
        return this;
    }
    public final RenderBuffer putInt(int pos, int x) {
        unsafe.putInt(baseAddress + pos, x);
        return this;
    }
    public final RenderBuffer putInt(int x) {
        unsafe.putInt(curAddress, x);
        curAddress += SIZEOF_INT;
        return this;
    }
    public RenderBuffer put(int[] x) {
        return put(x, 0, x.length);
    }
    public RenderBuffer put(int[] x, int offset, int length) {
        if (length > COPY_FROM_ARRAY_THRESHOLD) {
            long offsetInBytes = offset * SIZEOF_INT + Unsafe.ARRAY_INT_BASE_OFFSET;
            long lengthInBytes = length * SIZEOF_INT;
            unsafe.copyMemory(x, offsetInBytes, null, curAddress, lengthInBytes);
            position(position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                putInt(x[i]);
            }
        }
        return this;
    }
    public final RenderBuffer putFloat(float x) {
        unsafe.putFloat(curAddress, x);
        curAddress += SIZEOF_FLOAT;
        return this;
    }
    public RenderBuffer put(float[] x) {
        return put(x, 0, x.length);
    }
    public RenderBuffer put(float[] x, int offset, int length) {
        if (length > COPY_FROM_ARRAY_THRESHOLD) {
            long offsetInBytes = offset * SIZEOF_FLOAT + Unsafe.ARRAY_FLOAT_BASE_OFFSET;
            long lengthInBytes = length * SIZEOF_FLOAT;
            unsafe.copyMemory(x, offsetInBytes, null, curAddress, lengthInBytes);
            position(position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                putFloat(x[i]);
            }
        }
        return this;
    }
    public final RenderBuffer putLong(long x) {
        unsafe.putLong(curAddress, x);
        curAddress += SIZEOF_LONG;
        return this;
    }
    public RenderBuffer put(long[] x) {
        return put(x, 0, x.length);
    }
    public RenderBuffer put(long[] x, int offset, int length) {
        if (length > COPY_FROM_ARRAY_THRESHOLD) {
            long offsetInBytes = offset * SIZEOF_LONG + Unsafe.ARRAY_LONG_BASE_OFFSET;
            long lengthInBytes = length * SIZEOF_LONG;
            unsafe.copyMemory(x, offsetInBytes, null, curAddress, lengthInBytes);
            position(position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                putLong(x[i]);
            }
        }
        return this;
    }
    public final RenderBuffer putDouble(double x) {
        unsafe.putDouble(curAddress, x);
        curAddress += SIZEOF_DOUBLE;
        return this;
    }
}

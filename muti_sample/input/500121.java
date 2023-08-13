abstract class LongArrayBuffer extends LongBuffer {
    protected final long[] backingArray;
    protected final int offset;
    LongArrayBuffer(long[] array) {
        this(array.length, array, 0);
    }
    LongArrayBuffer(int capacity) {
        this(capacity, new long[capacity], 0);
    }
    LongArrayBuffer(int capacity, long[] backingArray, int offset) {
        super(capacity);
        this.backingArray = backingArray;
        this.offset = offset;
    }
    @Override
    public final long get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return backingArray[offset + position++];
    }
    @Override
    public final long get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return backingArray[offset + index];
    }
    @Override
    public final LongBuffer get(long[] dest, int off, int len) {
        int length = dest.length;
        if (off < 0 || len < 0 || (long) len + (long) off > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(backingArray, offset + position, dest, off, len);
        position += len;
        return this;
    }
    @Override
    public final boolean isDirect() {
        return false;
    }
    @Override
    public final ByteOrder order() {
        return ByteOrder.nativeOrder();
    }
}

abstract class ShortArrayBuffer extends ShortBuffer {
    protected final short[] backingArray;
    protected final int offset;
    ShortArrayBuffer(short[] array) {
        this(array.length, array, 0);
    }
    ShortArrayBuffer(int capacity) {
        this(capacity, new short[capacity], 0);
    }
    ShortArrayBuffer(int capacity, short[] backingArray, int offset) {
        super(capacity);
        this.backingArray = backingArray;
        this.offset = offset;
    }
    @Override
    public final short get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return backingArray[offset + position++];
    }
    @Override
    public final short get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return backingArray[offset + index];
    }
    @Override
    public final ShortBuffer get(short[] dest, int off, int len) {
        int length = dest.length;
        if (off < 0 || len < 0 || (long) off + (long) len > length) {
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

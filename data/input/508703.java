abstract class FloatArrayBuffer extends FloatBuffer {
    protected final float[] backingArray;
    protected final int offset;
    FloatArrayBuffer(float[] array) {
        this(array.length, array, 0);
    }
    FloatArrayBuffer(int capacity) {
        this(capacity, new float[capacity], 0);
    }
    FloatArrayBuffer(int capacity, float[] backingArray, int offset) {
        super(capacity);
        this.backingArray = backingArray;
        this.offset = offset;
    }
    @Override
    public final float get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return backingArray[offset + position++];
    }
    @Override
    public final float get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return backingArray[offset + index];
    }
    @Override
    public final FloatBuffer get(float[] dest, int off, int len) {
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

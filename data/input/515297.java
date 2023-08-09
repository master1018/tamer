final class ReadOnlyHeapByteBuffer extends HeapByteBuffer {
    static ReadOnlyHeapByteBuffer copy(HeapByteBuffer other, int markOfOther) {
        ReadOnlyHeapByteBuffer buf = new ReadOnlyHeapByteBuffer(
                other.backingArray, other.capacity(), other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        buf.order(other.order());
        return buf;
    }
    ReadOnlyHeapByteBuffer(byte[] backingArray, int capacity, int arrayOffset) {
        super(backingArray, capacity, arrayOffset);
    }
    @Override
    public ByteBuffer asReadOnlyBuffer() {
        return copy(this, mark);
    }
    @Override
    public ByteBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    protected byte[] protectedArray() {
        throw new ReadOnlyBufferException();
    }
    @Override
    protected int protectedArrayOffset() {
        throw new ReadOnlyBufferException();
    }
    @Override
    protected boolean protectedHasArray() {
        return false;
    }
    @Override
    public ByteBuffer put(byte b) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer put(int index, byte b) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer put(byte[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putDouble(double value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putDouble(int index, double value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putFloat(float value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putFloat(int index, float value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putInt(int value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putInt(int index, int value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putLong(int index, long value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putLong(long value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putShort(int index, short value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer putShort(short value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer put(ByteBuffer buf) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer slice() {
        ReadOnlyHeapByteBuffer slice = new ReadOnlyHeapByteBuffer(backingArray,
                remaining(), offset + position);
        slice.order = order;
        return slice;
    }
}

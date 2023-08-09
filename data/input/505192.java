final class ReadOnlyLongArrayBuffer extends LongArrayBuffer {
    static ReadOnlyLongArrayBuffer copy(LongArrayBuffer other, int markOfOther) {
        ReadOnlyLongArrayBuffer buf = new ReadOnlyLongArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }
    ReadOnlyLongArrayBuffer(int capacity, long[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }
    @Override
    public LongBuffer asReadOnlyBuffer() {
        return duplicate();
    }
    @Override
    public LongBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    @Override
    public LongBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    protected long[] protectedArray() {
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
    public LongBuffer put(long c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public LongBuffer put(int index, long c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public LongBuffer put(LongBuffer buf) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final LongBuffer put(long[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public LongBuffer slice() {
        return new ReadOnlyLongArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}

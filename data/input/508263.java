final class ReadOnlyShortArrayBuffer extends ShortArrayBuffer {
    static ReadOnlyShortArrayBuffer copy(ShortArrayBuffer other, int markOfOther) {
        ReadOnlyShortArrayBuffer buf = new ReadOnlyShortArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }
    ReadOnlyShortArrayBuffer(int capacity, short[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }
    @Override
    public ShortBuffer asReadOnlyBuffer() {
        return duplicate();
    }
    @Override
    public ShortBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ShortBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    protected short[] protectedArray() {
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
    public ShortBuffer put(ShortBuffer buf) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ShortBuffer put(short c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ShortBuffer put(int index, short c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final ShortBuffer put(short[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ShortBuffer slice() {
        return new ReadOnlyShortArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}

final class ReadOnlyDoubleArrayBuffer extends DoubleArrayBuffer {
    static ReadOnlyDoubleArrayBuffer copy(DoubleArrayBuffer other,
            int markOfOther) {
        ReadOnlyDoubleArrayBuffer buf = new ReadOnlyDoubleArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }
    ReadOnlyDoubleArrayBuffer(int capacity, double[] backingArray,
            int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }
    @Override
    public DoubleBuffer asReadOnlyBuffer() {
        return duplicate();
    }
    @Override
    public DoubleBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    @Override
    public DoubleBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    protected double[] protectedArray() {
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
    public DoubleBuffer put(double c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public DoubleBuffer put(int index, double c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final DoubleBuffer put(double[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final DoubleBuffer put(DoubleBuffer buf) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public DoubleBuffer slice() {
        return new ReadOnlyDoubleArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}

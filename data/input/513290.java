final class ReadOnlyIntArrayBuffer extends IntArrayBuffer {
    static ReadOnlyIntArrayBuffer copy(IntArrayBuffer other, int markOfOther) {
        ReadOnlyIntArrayBuffer buf = new ReadOnlyIntArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }
    ReadOnlyIntArrayBuffer(int capacity, int[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }
    @Override
    public IntBuffer asReadOnlyBuffer() {
        return duplicate();
    }
    @Override
    public IntBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    @Override
    public IntBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    protected int[] protectedArray() {
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
    public IntBuffer put(int c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public IntBuffer put(int index, int c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public IntBuffer put(IntBuffer buf) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final IntBuffer put(int[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public IntBuffer slice() {
        return new ReadOnlyIntArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}

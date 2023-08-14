final class ReadOnlyFloatArrayBuffer extends FloatArrayBuffer {
    static ReadOnlyFloatArrayBuffer copy(FloatArrayBuffer other, int markOfOther) {
        ReadOnlyFloatArrayBuffer buf = new ReadOnlyFloatArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }
    ReadOnlyFloatArrayBuffer(int capacity, float[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }
    @Override
    public FloatBuffer asReadOnlyBuffer() {
        return duplicate();
    }
    @Override
    public FloatBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    @Override
    public FloatBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    protected float[] protectedArray() {
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
    public FloatBuffer put(float c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public FloatBuffer put(int index, float c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public FloatBuffer put(FloatBuffer buf) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final FloatBuffer put(float[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public FloatBuffer slice() {
        return new ReadOnlyFloatArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}

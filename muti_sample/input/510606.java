final class ReadWriteIntArrayBuffer extends IntArrayBuffer {
    static ReadWriteIntArrayBuffer copy(IntArrayBuffer other, int markOfOther) {
        ReadWriteIntArrayBuffer buf = new ReadWriteIntArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }
    ReadWriteIntArrayBuffer(int[] array) {
        super(array);
    }
    ReadWriteIntArrayBuffer(int capacity) {
        super(capacity);
    }
    ReadWriteIntArrayBuffer(int capacity, int[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }
    @Override
    public IntBuffer asReadOnlyBuffer() {
        return ReadOnlyIntArrayBuffer.copy(this, mark);
    }
    @Override
    public IntBuffer compact() {
        System.arraycopy(backingArray, position + offset, backingArray, offset,
                remaining());
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }
    @Override
    public IntBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return false;
    }
    @Override
    protected int[] protectedArray() {
        return backingArray;
    }
    @Override
    protected int protectedArrayOffset() {
        return offset;
    }
    @Override
    protected boolean protectedHasArray() {
        return true;
    }
    @Override
    public IntBuffer put(int c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        backingArray[offset + position++] = c;
        return this;
    }
    @Override
    public IntBuffer put(int index, int c) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        backingArray[offset + index] = c;
        return this;
    }
    @Override
    public IntBuffer put(int[] src, int off, int len) {
        int length = src.length;
        if (off < 0 || len < 0 || (long) off + (long) len > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferOverflowException();
        }
        System.arraycopy(src, off, backingArray, offset + position, len);
        position += len;
        return this;
    }
    @Override
    public IntBuffer slice() {
        return new ReadWriteIntArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}

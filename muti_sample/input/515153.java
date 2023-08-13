final class ReadOnlyCharArrayBuffer extends CharArrayBuffer {
    static ReadOnlyCharArrayBuffer copy(CharArrayBuffer other, int markOfOther) {
        ReadOnlyCharArrayBuffer buf = new ReadOnlyCharArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }
    ReadOnlyCharArrayBuffer(int capacity, char[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }
    @Override
    public CharBuffer asReadOnlyBuffer() {
        return duplicate();
    }
    @Override
    public CharBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    @Override
    public CharBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    protected char[] protectedArray() {
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
    public CharBuffer put(char c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public CharBuffer put(int index, char c) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final CharBuffer put(char[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public final CharBuffer put(CharBuffer src) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public CharBuffer put(String src, int start, int end) {
        if ((start < 0) || (end < 0)
                || (long) start + (long) end > src.length()) {
            throw new IndexOutOfBoundsException();
        }
        throw new ReadOnlyBufferException();
    }
    @Override
    public CharBuffer slice() {
        return new ReadOnlyCharArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}

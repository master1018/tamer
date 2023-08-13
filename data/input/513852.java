abstract class BaseByteBuffer extends ByteBuffer {
    protected BaseByteBuffer(int capacity) {
        super(capacity);
    }
    @Override
    public final CharBuffer asCharBuffer() {
        return CharToByteBufferAdapter.wrap(this);
    }
    @Override
    public final DoubleBuffer asDoubleBuffer() {
        return DoubleToByteBufferAdapter.wrap(this);
    }
    @Override
    public final FloatBuffer asFloatBuffer() {
        return FloatToByteBufferAdapter.wrap(this);
    }
    @Override
    public final IntBuffer asIntBuffer() {
        return IntToByteBufferAdapter.wrap(this);
    }
    @Override
    public final LongBuffer asLongBuffer() {
        return LongToByteBufferAdapter.wrap(this);
    }
    @Override
    public final ShortBuffer asShortBuffer() {
        return ShortToByteBufferAdapter.wrap(this);
    }
    @Override
    public final char getChar() {
        return (char) getShort();
    }
    @Override
    public final char getChar(int index) {
        return (char) getShort(index);
    }
    @Override
    public final ByteBuffer putChar(char value) {
        return putShort((short) value);
    }
    @Override
    public final ByteBuffer putChar(int index, char value) {
        return putShort(index, (short) value);
    }
}

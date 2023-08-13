final class ReadOnlyDirectByteBuffer extends DirectByteBuffer {
    static ReadOnlyDirectByteBuffer copy(DirectByteBuffer other, int markOfOther) {
        ReadOnlyDirectByteBuffer buf = new ReadOnlyDirectByteBuffer(
                other.safeAddress, other.capacity(), other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        buf.order(other.order());
        return buf;
    }
    protected ReadOnlyDirectByteBuffer(SafeAddress address, int capacity,
            int offset) {
        super(address, capacity, offset);
    }
    protected ReadOnlyDirectByteBuffer(PlatformAddress address, int capacity,
            int offset) {
        super(new SafeAddress(address), capacity, offset);
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
    public ByteBuffer put(byte value) {
        throw new ReadOnlyBufferException();
    }
    @Override
    public ByteBuffer put(int index, byte value) {
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
        ReadOnlyDirectByteBuffer buf = new ReadOnlyDirectByteBuffer(
                safeAddress, remaining(), offset + position);
        buf.order = order;
        return buf;
    }
}

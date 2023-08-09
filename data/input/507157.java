final class ShortToByteBufferAdapter extends ShortBuffer implements
        DirectBuffer {
    static ShortBuffer wrap(ByteBuffer byteBuffer) {
        return new ShortToByteBufferAdapter(byteBuffer.slice());
    }
    private final ByteBuffer byteBuffer;
    ShortToByteBufferAdapter(ByteBuffer byteBuffer) {
        super((byteBuffer.capacity() >> 1));
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
    }
    public int getByteCapacity() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer) byteBuffer).getByteCapacity();
        }
        assert false : byteBuffer;
        return -1;
    }
    public PlatformAddress getEffectiveAddress() {
        if (byteBuffer instanceof DirectBuffer) {
            PlatformAddress addr = ((DirectBuffer)byteBuffer).getEffectiveAddress();
            effectiveDirectAddress = addr.toInt();
            return addr;
        }
        assert false : byteBuffer;
        return null;
    }
    public PlatformAddress getBaseAddress() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer) byteBuffer).getBaseAddress();
        }
        assert false : byteBuffer;
        return null;
    }
    public boolean isAddressValid() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer) byteBuffer).isAddressValid();
        }
        assert false : byteBuffer;
        return false;
    }
    public void addressValidityCheck() {
        if (byteBuffer instanceof DirectBuffer) {
            ((DirectBuffer) byteBuffer).addressValidityCheck();
        } else {
            assert false : byteBuffer;
        }
    }
    public void free() {
        if (byteBuffer instanceof DirectBuffer) {
            ((DirectBuffer) byteBuffer).free();
        } else {
            assert false : byteBuffer;
        }
    }
    @Override
    public ShortBuffer asReadOnlyBuffer() {
        ShortToByteBufferAdapter buf = new ShortToByteBufferAdapter(byteBuffer
                .asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public ShortBuffer compact() {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        byteBuffer.limit(limit << 1);
        byteBuffer.position(position << 1);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }
    @Override
    public ShortBuffer duplicate() {
        ShortToByteBufferAdapter buf = new ShortToByteBufferAdapter(byteBuffer
                .duplicate());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public short get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return byteBuffer.getShort(position++ << 1);
    }
    @Override
    public short get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return byteBuffer.getShort(index << 1);
    }
    @Override
    public boolean isDirect() {
        return byteBuffer.isDirect();
    }
    @Override
    public boolean isReadOnly() {
        return byteBuffer.isReadOnly();
    }
    @Override
    public ByteOrder order() {
        return byteBuffer.order();
    }
    @Override
    protected short[] protectedArray() {
        throw new UnsupportedOperationException();
    }
    @Override
    protected int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }
    @Override
    protected boolean protectedHasArray() {
        return false;
    }
    @Override
    public ShortBuffer put(short c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        byteBuffer.putShort(position++ << 1, c);
        return this;
    }
    @Override
    public ShortBuffer put(int index, short c) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        byteBuffer.putShort(index << 1, c);
        return this;
    }
    @Override
    public ShortBuffer put(short[] s, int off, int len) {
        if (byteBuffer instanceof ReadWriteDirectByteBuffer) {
            byteBuffer.limit(limit << 1);
            byteBuffer.position(position << 1);
            ((ReadWriteDirectByteBuffer) byteBuffer).put(s, off, len);
            this.position += len;
            return this;
        } else {
            return super.put(s, off, len);
        }
    }
    @Override
    public ShortBuffer slice() {
        byteBuffer.limit(limit << 1);
        byteBuffer.position(position << 1);
        ShortBuffer result = new ShortToByteBufferAdapter(byteBuffer.slice());
        byteBuffer.clear();
        return result;
    }
}

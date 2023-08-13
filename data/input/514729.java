final class IntToByteBufferAdapter extends IntBuffer implements DirectBuffer {
    static IntBuffer wrap(ByteBuffer byteBuffer) {
        return new IntToByteBufferAdapter(byteBuffer.slice());
    }
    private final ByteBuffer byteBuffer;
    IntToByteBufferAdapter(ByteBuffer byteBuffer) {
        super((byteBuffer.capacity() >> 2));
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
    public IntBuffer asReadOnlyBuffer() {
        IntToByteBufferAdapter buf = new IntToByteBufferAdapter(byteBuffer
                .asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public IntBuffer compact() {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        byteBuffer.limit(limit << 2);
        byteBuffer.position(position << 2);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }
    @Override
    public IntBuffer duplicate() {
        IntToByteBufferAdapter buf = new IntToByteBufferAdapter(byteBuffer
                .duplicate());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }
    @Override
    public int get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return byteBuffer.getInt(position++ << 2);
    }
    @Override
    public int get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return byteBuffer.getInt(index << 2);
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
    protected int[] protectedArray() {
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
    public IntBuffer put(int c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        byteBuffer.putInt(position++ << 2, c);
        return this;
    }
    @Override
    public IntBuffer put(int index, int c) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        byteBuffer.putInt(index << 2, c);
        return this;
    }
    @Override
    public IntBuffer put(int[] i, int off, int len) {
        if (byteBuffer instanceof ReadWriteDirectByteBuffer) {
            byteBuffer.limit(limit << 2);
            byteBuffer.position(position << 2);
            ((ReadWriteDirectByteBuffer) byteBuffer).put(i, off, len);
            this.position += len;
            return this;
        } else {
            return super.put(i, off, len);
        }
    }
    @Override
    public IntBuffer slice() {
        byteBuffer.limit(limit << 2);
        byteBuffer.position(position << 2);
        IntBuffer result = new IntToByteBufferAdapter(byteBuffer.slice());
        byteBuffer.clear();
        return result;
    }
}

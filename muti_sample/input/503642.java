final class ReadWriteDirectByteBuffer extends DirectByteBuffer {
    static ReadWriteDirectByteBuffer copy(DirectByteBuffer other,
            int markOfOther) {
        ReadWriteDirectByteBuffer buf = new ReadWriteDirectByteBuffer(
                other.safeAddress, other.capacity(), other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        buf.order(other.order());
        return buf;
    }
    ReadWriteDirectByteBuffer(int capacity) {
        super(capacity);
    }
    ReadWriteDirectByteBuffer(int pointer, int capacity) {
        this(PlatformAddressFactory.on(pointer, capacity),capacity,0);
    }
    ReadWriteDirectByteBuffer(SafeAddress address, int capacity, int offset) {
        super(address, capacity, offset);
    }
    ReadWriteDirectByteBuffer(PlatformAddress address, int aCapacity,
            int anOffset) {
        super(new SafeAddress(address), aCapacity, anOffset);
    }
    int getAddress() {
        return this.safeAddress.address.toInt();
    }
    @Override
    public ByteBuffer asReadOnlyBuffer() {
        return ReadOnlyDirectByteBuffer.copy(this, mark);
    }
    @Override
    public ByteBuffer compact() {
        PlatformAddress effectiveAddress = getEffectiveAddress();
        effectiveAddress.offsetBytes(position).moveTo(effectiveAddress,
                remaining());
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }
    @Override
    public ByteBuffer duplicate() {
        return copy(this, mark);
    }
    @Override
    public boolean isReadOnly() {
        return false;
    }
    @Override
    public ByteBuffer put(byte value) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        getBaseAddress().setByte(offset + position++, value);
        return this;
    }
    @Override
    public ByteBuffer put(int index, byte value) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        getBaseAddress().setByte(offset + index, value);
        return this;
    }
    @Override
    public ByteBuffer put(byte[] src, int off, int len) {
        int length = src.length;
        if (off < 0 || len < 0 || (long) off + (long) len > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferOverflowException();
        }
        getBaseAddress().setByteArray(offset + position, src, off, len);
        position += len;
        return this;
    }
    ByteBuffer put(short[] src, int off, int len) {
        int length = src.length;
        if (off < 0 || len < 0 || (long)off + (long)len > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len << 1 > remaining()) {
            throw new BufferOverflowException();
        }
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        boolean swap = order() != ByteOrder.nativeOrder();
        getBaseAddress().setShortArray(offset + position, src, off, len, swap);
        position += len << 1;
        return this;
    }
    ByteBuffer put(int[] src, int off, int len) {
        int length = src.length;
        if (off < 0 || len < 0 || (long)off + (long)len > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len << 2 > remaining()) {
            throw new BufferOverflowException();
        }
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        boolean swap = order() != ByteOrder.nativeOrder();
        getBaseAddress().setIntArray(offset + position, src, off, len, swap);
        position += len << 2;
        return this;
    }
    @Override
    public ByteBuffer putDouble(double value) {
        int newPosition = position + 8;
        if (newPosition > limit) {
            throw new BufferOverflowException();
        }
        getBaseAddress().setDouble(offset + position, value, order);
        position = newPosition;
        return this;
    }
    @Override
    public ByteBuffer putDouble(int index, double value) {
        if (index < 0 || (long) index + 8 > limit) {
            throw new IndexOutOfBoundsException();
        }
        getBaseAddress().setDouble(offset + index, value, order);
        return this;
    }
    @Override
    public ByteBuffer putFloat(float value) {
        int newPosition = position + 4;
        if (newPosition > limit) {
            throw new BufferOverflowException();
        }
        getBaseAddress().setFloat(offset + position, value, order);
        position = newPosition;
        return this;
    }
    @Override
    public ByteBuffer putFloat(int index, float value) {
        if (index < 0 || (long) index + 4 > limit) {
            throw new IndexOutOfBoundsException();
        }
        getBaseAddress().setFloat(offset + index, value, order);
        return this;
    }
    @Override
    public ByteBuffer putInt(int value) {
        int newPosition = position + 4;
        if (newPosition > limit) {
            throw new BufferOverflowException();
        }
        getBaseAddress().setInt(offset + position, value, order);
        position = newPosition;
        return this;
    }
    @Override
    public ByteBuffer putInt(int index, int value) {
        if (index < 0 || (long) index + 4 > limit) {
            throw new IndexOutOfBoundsException();
        }
        getBaseAddress().setInt(offset + index, value, order);
        return this;
    }
    @Override
    public ByteBuffer putLong(long value) {
        int newPosition = position + 8;
        if (newPosition > limit) {
            throw new BufferOverflowException();
        }
        getBaseAddress().setLong(offset + position, value, order);
        position = newPosition;
        return this;
    }
    @Override
    public ByteBuffer putLong(int index, long value) {
        if (index < 0 || (long) index + 8 > limit) {
            throw new IndexOutOfBoundsException();
        }
        getBaseAddress().setLong(offset + index, value, order);
        return this;
    }
    @Override
    public ByteBuffer putShort(short value) {
        int newPosition = position + 2;
        if (newPosition > limit) {
            throw new BufferOverflowException();
        }
        getBaseAddress().setShort(offset + position, value, order);
        position = newPosition;
        return this;
    }
    @Override
    public ByteBuffer putShort(int index, short value) {
        if (index < 0 || (long) index + 2 > limit) {
            throw new IndexOutOfBoundsException();
        }
        getBaseAddress().setShort(offset + index, value, order);
        return this;
    }
    @Override
    public ByteBuffer slice() {
        ReadWriteDirectByteBuffer buf = new ReadWriteDirectByteBuffer(
                safeAddress, remaining(), offset + position);
        buf.order = order;
        return buf;
    }
}

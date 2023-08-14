abstract class DirectByteBuffer extends BaseByteBuffer implements DirectBuffer {
    static final class SafeAddress {
        protected volatile boolean isValid = true;
        protected final PlatformAddress address;
        protected SafeAddress(PlatformAddress address) {
            super();
            this.address = address;
        }
    }
    protected final SafeAddress safeAddress;
    protected final int offset;
    DirectByteBuffer(int capacity) {
        this(new SafeAddress(PlatformAddressFactory.alloc(capacity, (byte) 0)),
                capacity, 0);
        safeAddress.address.autoFree();
    }
    DirectByteBuffer(SafeAddress address, int capacity, int offset) {
        super(capacity);
        PlatformAddress baseAddress = address.address;
        long baseSize = baseAddress.getSize();
        if ((baseSize >= 0) && ((offset + capacity) > baseSize)) {
            throw new IllegalArgumentException("slice out of range");
        }
        this.safeAddress = address;
        this.offset = offset;
    }
    @Override
    public final ByteBuffer get(byte[] dest, int off, int len) {
        int length = dest.length;
        if ((off < 0) || (len < 0) || (long) off + (long) len > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferUnderflowException();
        }
        getBaseAddress().getByteArray(offset + position, dest, off, len);
        position += len;
        return this;
    }
    @Override
    public final byte get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return getBaseAddress().getByte(offset + position++);
    }
    @Override
    public final byte get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return getBaseAddress().getByte(offset + index);
    }
    @Override
    public final double getDouble() {
        int newPosition = position + 8;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        double result = getBaseAddress().getDouble(offset + position, order);
        position = newPosition;
        return result;
    }
    @Override
    public final double getDouble(int index) {
        if (index < 0 || (long) index + 8 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return getBaseAddress().getDouble(offset + index, order);
    }
    @Override
    public final float getFloat() {
        int newPosition = position + 4;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        float result = getBaseAddress().getFloat(offset + position, order);
        position = newPosition;
        return result;
    }
    @Override
    public final float getFloat(int index) {
        if (index < 0 || (long) index + 4 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return getBaseAddress().getFloat(offset + index, order);
    }
    @Override
    public final int getInt() {
        int newPosition = position + 4;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        int result = getBaseAddress().getInt(offset + position, order);
        position = newPosition;
        return result;
    }
    @Override
    public final int getInt(int index) {
        if (index < 0 || (long) index + 4 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return getBaseAddress().getInt(offset + index, order);
    }
    @Override
    public final long getLong() {
        int newPosition = position + 8;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        long result = getBaseAddress().getLong(offset + position, order);
        position = newPosition;
        return result;
    }
    @Override
    public final long getLong(int index) {
        if (index < 0 || (long) index + 8 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return getBaseAddress().getLong(offset + index, order);
    }
    @Override
    public final short getShort() {
        int newPosition = position + 2;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        short result = getBaseAddress().getShort(offset + position, order);
        position = newPosition;
        return result;
    }
    @Override
    public final short getShort(int index) {
        if (index < 0 || (long) index + 2 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return getBaseAddress().getShort(offset + index, order);
    }
    @Override
    public final boolean isDirect() {
        return true;
    }
    public final boolean isAddressValid() {
        return safeAddress.isValid;
    }
    public final void addressValidityCheck() {
        if (!isAddressValid()) {
            throw new IllegalStateException(Messages.getString("nio.08")); 
        }
    }
    private void markAddressInvalid() {
        safeAddress.isValid = false;
    }
    public final PlatformAddress getBaseAddress() {
        addressValidityCheck();
        return safeAddress.address;
    }
    public final PlatformAddress getEffectiveAddress() {
        PlatformAddress addr = getBaseAddress().offsetBytes(offset);
        effectiveDirectAddress = addr.toInt();
        return addr;
    }
    public final void free() {
        if (isAddressValid()) {
            markAddressInvalid();
            safeAddress.address.free();
        }
    }
    @Override
    final protected byte[] protectedArray() {
        throw new UnsupportedOperationException();
    }
    @Override
    final protected int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }
    @Override
    final protected boolean protectedHasArray() {
        return false;
    }
    public final int getByteCapacity() {
        return capacity;
    }
}

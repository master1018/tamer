class NativeObject {                                    
    protected static final Unsafe unsafe = Unsafe.getUnsafe();
    protected long allocationAddress;
    private final long address;
    NativeObject(long address) {
        this.allocationAddress = address;
        this.address = address;
    }
    NativeObject(long address, long offset) {
        this.allocationAddress = address;
        this.address = address + offset;
    }
    protected NativeObject(int size, boolean pageAligned) {
        if (!pageAligned) {
            this.allocationAddress = unsafe.allocateMemory(size);
            this.address = this.allocationAddress;
        } else {
            int ps = pageSize();
            long a = unsafe.allocateMemory(size + ps);
            this.allocationAddress = a;
            this.address = a + ps - (a & (ps - 1));
        }
    }
    long address() {
        return address;
    }
    long allocationAddress() {
        return allocationAddress;
    }
    NativeObject subObject(int offset) {
        return new NativeObject(offset + address);
    }
    NativeObject getObject(int offset) {
        long newAddress = 0L;
        switch (addressSize()) {
            case 8:
                newAddress = unsafe.getLong(offset + address);
                break;
            case 4:
                newAddress = unsafe.getInt(offset + address) & 0x00000000FFFFFFFF;
                break;
            default:
                throw new InternalError("Address size not supported");
        }
        return new NativeObject(newAddress);
    }
    void putObject(int offset, NativeObject ob) {
        switch (addressSize()) {
            case 8:
                putLong(offset, ob.address);
                break;
            case 4:
                putInt(offset, (int)(ob.address & 0x00000000FFFFFFFF));
                break;
            default:
                throw new InternalError("Address size not supported");
        }
    }
    final byte getByte(int offset) {
        return unsafe.getByte(offset + address);
    }
    final void putByte(int offset, byte value) {
        unsafe.putByte(offset + address,  value);
    }
    final short getShort(int offset) {
        return unsafe.getShort(offset + address);
    }
    final void putShort(int offset, short value) {
        unsafe.putShort(offset + address,  value);
    }
    final char getChar(int offset) {
        return unsafe.getChar(offset + address);
    }
    final void putChar(int offset, char value) {
        unsafe.putChar(offset + address,  value);
    }
    final int getInt(int offset) {
        return unsafe.getInt(offset + address);
    }
    final void putInt(int offset, int value) {
        unsafe.putInt(offset + address, value);
    }
    final long getLong(int offset) {
        return unsafe.getLong(offset + address);
    }
    final void putLong(int offset, long value) {
        unsafe.putLong(offset + address, value);
    }
    final float getFloat(int offset) {
        return unsafe.getFloat(offset + address);
    }
    final void putFloat(int offset, float value) {
        unsafe.putFloat(offset + address, value);
    }
    final double getDouble(int offset) {
        return unsafe.getDouble(offset + address);
    }
    final void putDouble(int offset, double value) {
        unsafe.putDouble(offset + address, value);
    }
    static int addressSize() {
        return unsafe.addressSize();
    }
    private static ByteOrder byteOrder = null;
    static ByteOrder byteOrder() {
        if (byteOrder != null)
            return byteOrder;
        long a = unsafe.allocateMemory(8);
        try {
            unsafe.putLong(a, 0x0102030405060708L);
            byte b = unsafe.getByte(a);
            switch (b) {
            case 0x01: byteOrder = ByteOrder.BIG_ENDIAN;     break;
            case 0x08: byteOrder = ByteOrder.LITTLE_ENDIAN;  break;
            default:
                assert false;
            }
        } finally {
            unsafe.freeMemory(a);
        }
        return byteOrder;
    }
    private static int pageSize = -1;
    static int pageSize() {
        if (pageSize == -1)
            pageSize = unsafe.pageSize();
        return pageSize;
    }
}

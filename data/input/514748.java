public class PlatformAddress implements ICommonDataTypes, Comparable {
    static final int UNKNOWN = -1;
    static final int SIZEOF = Platform.getMemorySystem().getPointerSize();
    public static final PlatformAddress NULL = new PlatformAddress(0, 0);
    public static final PlatformAddress INVALID =
            new PlatformAddress(UNKNOWN, UNKNOWN);
    public static final IMemorySpy memorySpy = new RuntimeMemorySpy();
    static final IMemorySystem osMemory = Platform.getMemorySystem();
    final int osaddr;
    final long size;
    PlatformAddress(int address, long size) {
        super();
        osaddr = address;
        this.size = size;
    }
    public final void autoFree() {
        memorySpy.autoFree(this);
    }
    public PlatformAddress duplicate() {
        return PlatformAddressFactory.on(osaddr, size);
    }
    public PlatformAddress offsetBytes(int offset) {
        return PlatformAddressFactory.on(osaddr + offset, size - offset);
    }
    public final void moveTo(PlatformAddress dest, long numBytes) {
        osMemory.memmove(dest.osaddr, osaddr, numBytes);
    }
    public final boolean equals(Object other) {
        return (other instanceof PlatformAddress)
                && (((PlatformAddress) other).osaddr == osaddr);
    }
    public final int hashCode() {
        return (int) osaddr;
    }
    public final boolean isNULL() {
        return this == NULL;
    }
    public void free() {
        if (memorySpy.free(this)) {
            osMemory.free(osaddr);
        }
    }
    public final void setAddress(int offset, PlatformAddress address) {
        osMemory.setAddress(osaddr + offset, address.osaddr);
    }
    public final PlatformAddress getAddress(int offset) {
        int addr = getInt(offset);
        return PlatformAddressFactory.on(addr);
    }
    public final void setByte(int offset, byte value) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JBYTE);
        osMemory.setByte(osaddr + offset, value);
    }
    public final void setByteArray(int offset, byte[] bytes, int bytesOffset,
            int length) {
        memorySpy.rangeCheck(this, offset, length * SIZEOF_JBYTE);
        osMemory.setByteArray(osaddr + offset, bytes, bytesOffset, length);
    }
    public final void setShortArray(int offset, short[] shorts,
            int shortsOffset, int length, boolean swap) {
        memorySpy.rangeCheck(this, offset, length * SIZEOF_JSHORT);
        osMemory.setShortArray(osaddr + offset, shorts, shortsOffset, length,
            swap);
    }
    public final void setIntArray(int offset, int[] ints,
            int intsOffset, int length, boolean swap) {
        memorySpy.rangeCheck(this, offset, length * SIZEOF_JINT);
        osMemory.setIntArray(osaddr + offset, ints, intsOffset, length, swap);
    }
    public final byte getByte(int offset) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JBYTE);
        return osMemory.getByte(osaddr + offset);
    }
    public final void getByteArray(int offset, byte[] bytes, int bytesOffset,
            int length) {
        memorySpy.rangeCheck(this, offset, length * SIZEOF_JBYTE);
        osMemory.getByteArray(osaddr + offset, bytes, bytesOffset, length);
    }
    public final void setShort(int offset, short value, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
        osMemory.setShort(osaddr + offset, value, order);
    }
    public final void setShort(int offset, short value) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
        osMemory.setShort(osaddr + offset, value);
    }
    public final short getShort(int offset, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
        return osMemory.getShort(osaddr + offset, order);
    }
    public final short getShort(int offset) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
        return osMemory.getShort(osaddr + offset);
    }
    public final void setInt(int offset, int value, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
        osMemory.setInt(osaddr + offset, value, order);
    }
    public final void setInt(int offset, int value) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
        osMemory.setInt(osaddr + offset, value);
    }
    public final int getInt(int offset, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
        return osMemory.getInt(osaddr + offset, order);
    }
    public final int getInt(int offset) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
        return osMemory.getInt(osaddr + offset);
    }
    public final void setLong(int offset, long value, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
        osMemory.setLong(osaddr + offset, value, order);
    }
    public final void setLong(int offset, long value) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
        osMemory.setLong(osaddr + offset, value);
    }
    public final long getLong(int offset, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
        return osMemory.getLong(osaddr + offset, order);
    }
    public final long getLong(int offset) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
        return osMemory.getLong(osaddr + offset);
    }
    public final void setFloat(int offset, float value, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
        osMemory.setFloat(osaddr + offset, value, order);
    }
    public final void setFloat(int offset, float value) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
        osMemory.setFloat(osaddr + offset, value);
    }
    public final float getFloat(int offset, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
        return osMemory.getFloat(osaddr + offset, order);
    }
    public final float getFloat(int offset) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
        return osMemory.getFloat(osaddr + offset);
    }
    public final void setDouble(int offset, double value, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
        osMemory.setDouble(osaddr + offset, value, order);
    }
    public final void setDouble(int offset, double value) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
        osMemory.setDouble(osaddr + offset, value);
    }
    public final double getDouble(int offset, Endianness order) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
        return osMemory.getDouble(osaddr + offset, order);
    }
    public final double getDouble(int offset) {
        memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
        return osMemory.getDouble(osaddr + offset);
    }
    public final int toInt() {
        return osaddr;
    }
    public final long toLong() {
        return osaddr;
    }
    public final String toString() {
        return "PlatformAddress[" + osaddr + "]"; 
    }
    public final long getSize() {
        return size;
    }
    public final int compareTo(Object other) {
        if (other == null) {
            throw new NullPointerException(); 
        }
        if (other instanceof PlatformAddress) {
            int otherPA = ((PlatformAddress) other).osaddr;
            if (osaddr == otherPA) {
                return 0;
            }
            return osaddr < otherPA ? -1 : 1;
        }
        throw new ClassCastException(); 
    }
}

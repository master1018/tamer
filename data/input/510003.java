final class OSMemory implements IMemorySystem {
    public static final int POINTER_SIZE;
    public static final Endianness NATIVE_ORDER;
    private static final OSMemory singleton = new OSMemory();
    static {
        POINTER_SIZE = getPointerSizeImpl();
        if (isLittleEndianImpl()) {
            NATIVE_ORDER = Endianness.LITTLE_ENDIAN;
        } else {
            NATIVE_ORDER = Endianness.BIG_ENDIAN;
        }
    }
    public static OSMemory getOSMemory() {
        return singleton;
    }
    private static native boolean isLittleEndianImpl();
	private OSMemory() {
		super();
	}
	public boolean isLittleEndian() {
		return NATIVE_ORDER == Endianness.LITTLE_ENDIAN;
	}
	public Endianness getNativeOrder() {
		return NATIVE_ORDER;
	}
	private static native int getPointerSizeImpl();
	public int getPointerSize() {
		return POINTER_SIZE;
	}
    public native int malloc(int length) throws OutOfMemoryError;
    public native void free(int address);
    public native void memset(int address, byte value, long length);
    public native void memmove(int destAddress, int srcAddress, long length);
    public native void getByteArray(int address, byte[] bytes, int offset,
            int length) throws NullPointerException, IndexOutOfBoundsException;
    public native void setByteArray(int address, byte[] bytes, int offset,
            int length) throws NullPointerException, IndexOutOfBoundsException;
    public native void setShortArray(int address, short[] shorts, int offset,
            int length, boolean swap) throws NullPointerException,
            IndexOutOfBoundsException;
    public native void setIntArray(int address, int[] ints, int offset,
            int length, boolean swap) throws NullPointerException,
            IndexOutOfBoundsException;
    public native byte getByte(int address);
    public native void setByte(int address, byte value);
    public native short getShort(int address);
    public short getShort(int address, Endianness endianness) {
        return (endianness == NATIVE_ORDER) ? getShort(address)
                : swap(getShort(address));
    }
    public native void setShort(int address, short value);
    public void setShort(int address, short value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setShort(address, value);
        } else {
            setShort(address, swap(value));
        }
    }
    public native int getInt(int address);
    public int getInt(int address, Endianness endianness) {
        return (endianness == NATIVE_ORDER) ? getInt(address)
                : swap(getInt(address));
    }
    public native void setInt(int address, int value);
    public void setInt(int address, int value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setInt(address, value);
        } else {
            setInt(address, swap(value));
        }
    }
    public native long getLong(int address);
    public long getLong(int address, Endianness endianness) {
        return (endianness == NATIVE_ORDER) ? getLong(address)
                : swap(getLong(address));
    }
    public native void setLong(int address, long value);
    public void setLong(int address, long value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setLong(address, value);
        } else {
            setLong(address, swap(value));
        }
    }
    public native float getFloat(int address);
    public float getFloat(int address, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            return getFloat(address);
        }
        int floatBits = swap(getInt(address));
        return Float.intBitsToFloat(floatBits);
    }
    public native void setFloat(int address, float value);
    public void setFloat(int address, float value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setFloat(address, value);
        } else {
            int floatBits = Float.floatToIntBits(value);
            setInt(address, swap(floatBits));
        }
    }
    public native double getDouble(int address);
    public double getDouble(int address, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            return getDouble(address);
        }
        long doubleBits = swap(getLong(address));
        return Double.longBitsToDouble(doubleBits);
    }
    public native void setDouble(int address, double value);
    public void setDouble(int address, double value, Endianness endianness) {
        if (endianness == NATIVE_ORDER) {
            setDouble(address, value);
        } else {
            long doubleBits = Double.doubleToLongBits(value);
            setLong(address, swap(doubleBits));
        }
    }
    public native int getAddress(int address);
    public native void setAddress(int address, int value);
    private native int mmapImpl(int fd, long offset, long size, int mapMode);
    public int mmap(int fd, long offset, long size, int mapMode) throws IOException {
        if (offset < 0 || size < 0 || offset > Integer.MAX_VALUE || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("offset=" + offset + " size=" + size);
        }
        return mmapImpl(fd, offset, size, mapMode);
    }
    private native void unmapImpl(int addr, long size);
    public void unmap(int addr, long size) {
        unmapImpl(addr, size);
    }
    public void load(int addr, long size) {
        loadImpl(addr, size);
    }
    private native int loadImpl(int l, long size);
    public boolean isLoaded(int addr, long size) {
		return size == 0 ? true : isLoadedImpl(addr, size);
	}
    private native boolean isLoadedImpl(int l, long size);
    public void flush(int addr, long size) {
		flushImpl(addr, size);
	}
    private native int flushImpl(int l, long size);
	private short swap(short value) {
		int topEnd = value << 8;
		int btmEnd = (value >> 8) & 0xFF;
		return (short) (topEnd | btmEnd);
	}
	private int swap(int value) {
		short left = (short) (value >> 16);
		short right = (short) value;
		int topEnd = swap(right) << 16;
		int btmEnd = swap(left) & 0xFFFF;
		return topEnd | btmEnd;
	}
	private long swap(long value) {
		int left = (int) (value >> 32);
		int right = (int) value;
		long topEnd = ((long) swap(right)) << 32;
		long btmEnd = swap(left) & 0xFFFFFFFFL;
		return topEnd | btmEnd;
	}
}

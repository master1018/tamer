public final class Unsafe {
    private static native void registerNatives();
    static {
        registerNatives();
        sun.reflect.Reflection.registerMethodsToFilter(Unsafe.class, "getUnsafe");
    }
    private Unsafe() {}
    private static final Unsafe theUnsafe = new Unsafe();
    public static Unsafe getUnsafe() {
        Class cc = sun.reflect.Reflection.getCallerClass(2);
        if (cc.getClassLoader() != null)
            throw new SecurityException("Unsafe");
        return theUnsafe;
    }
    public native int getInt(Object o, long offset);
    public native void putInt(Object o, long offset, int x);
    public native Object getObject(Object o, long offset);
    public native void putObject(Object o, long offset, Object x);
    public native boolean getBoolean(Object o, long offset);
    public native void    putBoolean(Object o, long offset, boolean x);
    public native byte    getByte(Object o, long offset);
    public native void    putByte(Object o, long offset, byte x);
    public native short   getShort(Object o, long offset);
    public native void    putShort(Object o, long offset, short x);
    public native char    getChar(Object o, long offset);
    public native void    putChar(Object o, long offset, char x);
    public native long    getLong(Object o, long offset);
    public native void    putLong(Object o, long offset, long x);
    public native float   getFloat(Object o, long offset);
    public native void    putFloat(Object o, long offset, float x);
    public native double  getDouble(Object o, long offset);
    public native void    putDouble(Object o, long offset, double x);
    @Deprecated
    public int getInt(Object o, int offset) {
        return getInt(o, (long)offset);
    }
    @Deprecated
    public void putInt(Object o, int offset, int x) {
        putInt(o, (long)offset, x);
    }
    @Deprecated
    public Object getObject(Object o, int offset) {
        return getObject(o, (long)offset);
    }
    @Deprecated
    public void putObject(Object o, int offset, Object x) {
        putObject(o, (long)offset, x);
    }
    @Deprecated
    public boolean getBoolean(Object o, int offset) {
        return getBoolean(o, (long)offset);
    }
    @Deprecated
    public void putBoolean(Object o, int offset, boolean x) {
        putBoolean(o, (long)offset, x);
    }
    @Deprecated
    public byte getByte(Object o, int offset) {
        return getByte(o, (long)offset);
    }
    @Deprecated
    public void putByte(Object o, int offset, byte x) {
        putByte(o, (long)offset, x);
    }
    @Deprecated
    public short getShort(Object o, int offset) {
        return getShort(o, (long)offset);
    }
    @Deprecated
    public void putShort(Object o, int offset, short x) {
        putShort(o, (long)offset, x);
    }
    @Deprecated
    public char getChar(Object o, int offset) {
        return getChar(o, (long)offset);
    }
    @Deprecated
    public void putChar(Object o, int offset, char x) {
        putChar(o, (long)offset, x);
    }
    @Deprecated
    public long getLong(Object o, int offset) {
        return getLong(o, (long)offset);
    }
    @Deprecated
    public void putLong(Object o, int offset, long x) {
        putLong(o, (long)offset, x);
    }
    @Deprecated
    public float getFloat(Object o, int offset) {
        return getFloat(o, (long)offset);
    }
    @Deprecated
    public void putFloat(Object o, int offset, float x) {
        putFloat(o, (long)offset, x);
    }
    @Deprecated
    public double getDouble(Object o, int offset) {
        return getDouble(o, (long)offset);
    }
    @Deprecated
    public void putDouble(Object o, int offset, double x) {
        putDouble(o, (long)offset, x);
    }
    public native byte    getByte(long address);
    public native void    putByte(long address, byte x);
    public native short   getShort(long address);
    public native void    putShort(long address, short x);
    public native char    getChar(long address);
    public native void    putChar(long address, char x);
    public native int     getInt(long address);
    public native void    putInt(long address, int x);
    public native long    getLong(long address);
    public native void    putLong(long address, long x);
    public native float   getFloat(long address);
    public native void    putFloat(long address, float x);
    public native double  getDouble(long address);
    public native void    putDouble(long address, double x);
    public native long getAddress(long address);
    public native void putAddress(long address, long x);
    public native long allocateMemory(long bytes);
    public native long reallocateMemory(long address, long bytes);
    public native void setMemory(Object o, long offset, long bytes, byte value);
    public void setMemory(long address, long bytes, byte value) {
        setMemory(null, address, bytes, value);
    }
    public native void copyMemory(Object srcBase, long srcOffset,
                                  Object destBase, long destOffset,
                                  long bytes);
    public void copyMemory(long srcAddress, long destAddress, long bytes) {
        copyMemory(null, srcAddress, null, destAddress, bytes);
    }
    public native void freeMemory(long address);
    public static final int INVALID_FIELD_OFFSET   = -1;
    @Deprecated
    public int fieldOffset(Field f) {
        if (Modifier.isStatic(f.getModifiers()))
            return (int) staticFieldOffset(f);
        else
            return (int) objectFieldOffset(f);
    }
    @Deprecated
    public Object staticFieldBase(Class c) {
        Field[] fields = c.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic(fields[i].getModifiers())) {
                return staticFieldBase(fields[i]);
            }
        }
        return null;
    }
    public native long staticFieldOffset(Field f);
    public native long objectFieldOffset(Field f);
    public native Object staticFieldBase(Field f);
    public native void ensureClassInitialized(Class c);
    public native int arrayBaseOffset(Class arrayClass);
    public static final int ARRAY_BOOLEAN_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(boolean[].class);
    public static final int ARRAY_BYTE_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(byte[].class);
    public static final int ARRAY_SHORT_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(short[].class);
    public static final int ARRAY_CHAR_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(char[].class);
    public static final int ARRAY_INT_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(int[].class);
    public static final int ARRAY_LONG_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(long[].class);
    public static final int ARRAY_FLOAT_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(float[].class);
    public static final int ARRAY_DOUBLE_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(double[].class);
    public static final int ARRAY_OBJECT_BASE_OFFSET
            = theUnsafe.arrayBaseOffset(Object[].class);
    public native int arrayIndexScale(Class arrayClass);
    public static final int ARRAY_BOOLEAN_INDEX_SCALE
            = theUnsafe.arrayIndexScale(boolean[].class);
    public static final int ARRAY_BYTE_INDEX_SCALE
            = theUnsafe.arrayIndexScale(byte[].class);
    public static final int ARRAY_SHORT_INDEX_SCALE
            = theUnsafe.arrayIndexScale(short[].class);
    public static final int ARRAY_CHAR_INDEX_SCALE
            = theUnsafe.arrayIndexScale(char[].class);
    public static final int ARRAY_INT_INDEX_SCALE
            = theUnsafe.arrayIndexScale(int[].class);
    public static final int ARRAY_LONG_INDEX_SCALE
            = theUnsafe.arrayIndexScale(long[].class);
    public static final int ARRAY_FLOAT_INDEX_SCALE
            = theUnsafe.arrayIndexScale(float[].class);
    public static final int ARRAY_DOUBLE_INDEX_SCALE
            = theUnsafe.arrayIndexScale(double[].class);
    public static final int ARRAY_OBJECT_INDEX_SCALE
            = theUnsafe.arrayIndexScale(Object[].class);
    public native int addressSize();
    public static final int ADDRESS_SIZE = theUnsafe.addressSize();
    public native int pageSize();
    public native Class defineClass(String name, byte[] b, int off, int len,
                                    ClassLoader loader,
                                    ProtectionDomain protectionDomain);
    public native Class defineClass(String name, byte[] b, int off, int len);
    public native Class defineAnonymousClass(Class hostClass, byte[] data, Object[] cpPatches);
    public native Object allocateInstance(Class cls)
        throws InstantiationException;
    public native void monitorEnter(Object o);
    public native void monitorExit(Object o);
    public native boolean tryMonitorEnter(Object o);
    public native void throwException(Throwable ee);
    public final native boolean compareAndSwapObject(Object o, long offset,
                                                     Object expected,
                                                     Object x);
    public final native boolean compareAndSwapInt(Object o, long offset,
                                                  int expected,
                                                  int x);
    public final native boolean compareAndSwapLong(Object o, long offset,
                                                   long expected,
                                                   long x);
    public native Object getObjectVolatile(Object o, long offset);
    public native void    putObjectVolatile(Object o, long offset, Object x);
    public native int     getIntVolatile(Object o, long offset);
    public native void    putIntVolatile(Object o, long offset, int x);
    public native boolean getBooleanVolatile(Object o, long offset);
    public native void    putBooleanVolatile(Object o, long offset, boolean x);
    public native byte    getByteVolatile(Object o, long offset);
    public native void    putByteVolatile(Object o, long offset, byte x);
    public native short   getShortVolatile(Object o, long offset);
    public native void    putShortVolatile(Object o, long offset, short x);
    public native char    getCharVolatile(Object o, long offset);
    public native void    putCharVolatile(Object o, long offset, char x);
    public native long    getLongVolatile(Object o, long offset);
    public native void    putLongVolatile(Object o, long offset, long x);
    public native float   getFloatVolatile(Object o, long offset);
    public native void    putFloatVolatile(Object o, long offset, float x);
    public native double  getDoubleVolatile(Object o, long offset);
    public native void    putDoubleVolatile(Object o, long offset, double x);
    public native void    putOrderedObject(Object o, long offset, Object x);
    public native void    putOrderedInt(Object o, long offset, int x);
    public native void    putOrderedLong(Object o, long offset, long x);
    public native void unpark(Object thread);
    public native void park(boolean isAbsolute, long time);
    public native int getLoadAverage(double[] loadavg, int nelems);
}

class Native {
    private static Unsafe unsafe = XlibWrapper.unsafe;
    static int longSize;
    static int dataModel;
    static {
        String dataModelProp = (String)AccessController.
            doPrivileged(
                         new PrivilegedAction() {
                                 public Object run() {
                                     return System.getProperty("sun.arch.data.model");
                                 }
                             });
        try {
            dataModel = Integer.parseInt(dataModelProp);
        } catch (Exception e) {
            dataModel = 32;
        }
        if (dataModel == 32) {
            longSize = 4;
        } else {
            longSize = 8;
        }
    }
    static boolean getBool(long ptr) { return getInt(ptr) != 0; }
    static boolean getBool(long ptr, int index) { return getInt(ptr, index) != 0; }
    static void putBool(long ptr, boolean data) { putInt(ptr, (data)?(1):(0)); }
    static void putBool(long ptr, int index, boolean data) { putInt(ptr, index, (data)?(1):(0)); }
    static int getByteSize() { return 1; }
    static byte getByte(long ptr) { return unsafe.getByte(ptr); }
    static byte getByte(long ptr, int index) {
        return getByte(ptr+index);
    }
    static void putByte(long ptr, byte data) { unsafe.putByte(ptr, data); }
    static void putByte(long ptr, int index, byte data) {
        putByte(ptr+index, data);
    }
    static byte[] toBytes(long data, int length) {
        if (data == 0) {
            return null;
        }
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++, data++) {
            res[i] = getByte(data);
        }
        return res;
    }
    static long toData(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        long res = XlibWrapper.unsafe.allocateMemory(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            putByte(res+i, bytes[i]);
        }
        return res;
    }
    static int getUByteSize() { return 1; }
    static short getUByte(long ptr) { return (short)(0xFF & unsafe.getByte(ptr));  }
    static short getUByte(long ptr, int index) {
        return getUByte(ptr+index);
    }
    static void putUByte(long ptr, short data) { unsafe.putByte(ptr, (byte)data); }
    static void putUByte(long ptr, int index, short data) {
        putUByte(ptr+index, data);
    }
    static short[] toUBytes(long data, int length) {
        if (data == 0) {
            return null;
        }
        short[] res = new short[length];
        for (int i = 0; i < length; i++, data++) {
            res[i] = getUByte(data);
        }
        return res;
    }
    static long toUData(short[] bytes) {
        if (bytes == null) {
            return 0;
        }
        long res = XlibWrapper.unsafe.allocateMemory(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            putUByte(res+i, bytes[i]);
        }
        return res;
    }
    static int getShortSize() { return 2; }
    static short getShort(long ptr) { return unsafe.getShort(ptr); }
    static void putShort(long ptr, short data) { unsafe.putShort(ptr, data); }
    static void putShort(long ptr, int index, short data) {
        putShort(ptr + index*getShortSize(), data);
    }
    static long toData(short[] shorts) {
        if (shorts == null) {
            return 0;
        }
        long res = XlibWrapper.unsafe.allocateMemory(shorts.length*getShortSize());
        for (int i = 0; i < shorts.length; i++) {
            putShort(res, i, shorts[i]);
        }
        return res;
    }
    static int getUShortSize() { return 2; }
    static int getUShort(long ptr) { return 0xFFFF & unsafe.getShort(ptr); }
    static void putUShort(long ptr, int data) { unsafe.putShort(ptr, (short)data); }
    static void putUShort(long ptr, int index, int data) {
        putUShort(ptr + index*getShortSize(), data);
    }
    static long toUData(int[] shorts) {
        if (shorts == null) {
            return 0;
        }
        long res = XlibWrapper.unsafe.allocateMemory(shorts.length*getShortSize());
        for (int i = 0; i < shorts.length; i++) {
            putUShort(res, i, shorts[i]);
        }
        return res;
    }
    static int getIntSize() { return 4; }
    static int getInt(long ptr) { return unsafe.getInt(ptr); }
    static int getInt(long ptr, int index) { return getInt(ptr +getIntSize()*index); }
    static void putInt(long ptr, int data) { unsafe.putInt(ptr, data); }
    static void putInt(long ptr, int index, int data) {
        putInt(ptr + index*getIntSize(), data);
    }
    static long toData(int[] ints) {
        if (ints == null) {
            return 0;
        }
        long res = XlibWrapper.unsafe.allocateMemory(ints.length*getIntSize());
        for (int i = 0; i < ints.length; i++) {
            putInt(res, i, ints[i]);
        }
        return res;
    }
    static int getUIntSize() { return 4; }
    static long getUInt(long ptr) { return 0xFFFFFFFFL & unsafe.getInt(ptr); }
    static long getUInt(long ptr, int index) { return getUInt(ptr +getIntSize()*index); }
    static void putUInt(long ptr, long data) { unsafe.putInt(ptr, (int)data); }
    static void putUInt(long ptr, int index, long data) {
        putUInt(ptr + index*getIntSize(), data);
    }
    static long toUData(long[] ints) {
        if (ints == null) {
            return 0;
        }
        long res = XlibWrapper.unsafe.allocateMemory(ints.length*getIntSize());
        for (int i = 0; i < ints.length; i++) {
            putUInt(res, i, ints[i]);
        }
        return res;
    }
    static int getLongSize() {
        return longSize;
    }
    static long getLong(long ptr) {
        if (XlibWrapper.dataModel == 32) {
            return unsafe.getInt(ptr);
        } else {
            return unsafe.getLong(ptr);
        }
    }
    static void putLong(long ptr, long data) {
        if (XlibWrapper.dataModel == 32) {
            unsafe.putInt(ptr, (int)data);
        } else {
            unsafe.putLong(ptr, data);
        }
    }
    static void putLong(long ptr, int index, long data) {
        putLong(ptr+index*getLongSize(), data);
    }
    static long getLong(long ptr, int index) {
        return getLong(ptr + index*getLongSize());
    }
    static void put(long ptr, long[] arr) {
        for (int i = 0; i < arr.length; i ++, ptr += getLongSize()) {
            putLong(ptr, arr[i]);
        }
    }
    static void putLong(long ptr, Vector arr) {
        for (int i = 0; i < arr.size(); i ++, ptr += getLongSize()) {
            putLong(ptr, ((Long)arr.elementAt(i)).longValue());
        }
    }
    static void putLongReverse(long ptr, Vector arr) {
        for (int i = arr.size()-1; i >= 0; i--, ptr += getLongSize()) {
            putLong(ptr, ((Long)arr.elementAt(i)).longValue());
        }
    }
    static long[] toLongs(long data, int length) {
        if (data == 0) {
            return null;
        }
        long[] res = new long[length];
        for (int i = 0; i < length; i++, data += getLongSize()) {
            res[i] = getLong(data);
        }
        return res;
    }
    static long toData(long[] longs) {
        if (longs == null) {
            return 0;
        }
        long res = XlibWrapper.unsafe.allocateMemory(longs.length*getLongSize());
        for (int i = 0; i < longs.length; i++) {
            putLong(res, i, longs[i]);
        }
        return res;
    }
    static long getULong(long ptr) {
        if (XlibWrapper.dataModel == 32) {
            return ((long)unsafe.getInt(ptr)) & 0xFFFFFFFFL;
        } else {
            return unsafe.getLong(ptr);
        }
    }
    static void putULong(long ptr, long value) {
        putLong(ptr, value);
    }
    static long allocateLongArray(int length) {
        return unsafe.allocateMemory(getLongSize() * length);
    }
    static long getWindow(long ptr) {
        return getLong(ptr);
    }
    static long getWindow(long ptr, int index) {
        return getLong(ptr + getWindowSize()*index);
    }
    static void putWindow(long ptr, long window) {
        putLong(ptr, window);
    }
    static void putWindow(long ptr, int index, long window) {
        putLong(ptr, index, window);
    }
    static int getWindowSize() {
        return getLongSize();
    }
    static long getCard32(long ptr) {
        return getLong(ptr);
    }
    static void putCard32(long ptr, long value) {
        putLong(ptr, value);
    }
    static long getCard32(long ptr, int index) {
        return getLong(ptr, index);
    }
    static void putCard32(long ptr, int index, long value) {
        putLong(ptr, index, value);
    }
    static int getCard32Size() {
        return getLongSize();
    }
    static long[] card32ToArray(long ptr, int length) {
        return toLongs(ptr, length);
    }
    static long card32ToData(long[] arr) {
        return toData(arr);
    }
}

public final class Unsafe {
    private static final Unsafe THE_ONE = new Unsafe();
    private final LangAccess lang;
    private Unsafe() {
        lang = LangAccess.getInstance();
    }
    public static Unsafe getUnsafe() {
        ClassLoader calling = VMStack.getCallingClassLoader();
        if ((calling != null) && (calling != Unsafe.class.getClassLoader())) {
            throw new SecurityException("Unsafe access denied");
        }
        return THE_ONE;
    }
    public long objectFieldOffset(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException(
                    "valid for instance fields only");
        }
        return objectFieldOffset0(field);
    }
    private static native long objectFieldOffset0(Field field);
    public int arrayBaseOffset(Class clazz) {
        if (! clazz.isArray()) {
            throw new IllegalArgumentException(
                    "valid for array classes only");
        }
        return arrayBaseOffset0(clazz);
    }
    private static native int arrayBaseOffset0(Class clazz);
    public int arrayIndexScale(Class clazz) {
        if (! clazz.isArray()) {
            throw new IllegalArgumentException(
                    "valid for array classes only");
        }
        return arrayIndexScale0(clazz);
    }
    private static native int arrayIndexScale0(Class clazz);
    public native boolean compareAndSwapInt(Object obj, long offset,
            int expectedValue, int newValue);
    public native boolean compareAndSwapLong(Object obj, long offset,
            long expectedValue, long newValue);
    public native boolean compareAndSwapObject(Object obj, long offset,
            Object expectedValue, Object newValue);
    public native int getIntVolatile(Object obj, long offset);
    public native void putIntVolatile(Object obj, long offset, int newValue);
    public native long getLongVolatile(Object obj, long offset);
    public native void putLongVolatile(Object obj, long offset, long newValue);
    public native Object getObjectVolatile(Object obj, long offset);
    public native void putObjectVolatile(Object obj, long offset,
            Object newValue);
    public native int getInt(Object obj, long offset);
    public native void putInt(Object obj, long offset, int newValue);
    public native long getLong(Object obj, long offset);
    public native void putLong(Object obj, long offset, long newValue);
    public native Object getObject(Object obj, long offset);
    public native void putObject(Object obj, long offset, Object newValue);
    public void park(boolean absolute, long time) {
        if (absolute) {
            lang.parkUntil(time);
        } else {
            lang.parkFor(time);
        }
    }
    public void unpark(Object obj) {
        if (obj instanceof Thread) {
            lang.unpark((Thread) obj);
        } else {
            throw new IllegalArgumentException("valid for Threads only");
        }
    }
}
